package com.fooddelivery.common.infrastructure.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Kafka configuration providing producers, consumers, admin, and topics.
 * Supports both plaintext and SASL-authenticated connections.
 */
@Slf4j
@EnableKafka
@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    private final KafkaConfigData kafkaConfigData;

    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigData.getBootstrapServers());
        configs.put(AdminClientConfig.SECURITY_PROTOCOL_CONFIG, kafkaConfigData.getSecurityProtocol());
        addSaslConfig(configs);
        log.info("Creating KafkaAdmin with bootstrap servers: {}", kafkaConfigData.getBootstrapServers());
        return new KafkaAdmin(configs);
    }

    @Bean
    public <T> ProducerFactory<String, T> producerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigData.getBootstrapServers());
        configs.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configs.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        configs.put(ProducerConfig.ACKS_CONFIG, "all");
        configs.put(ProducerConfig.RETRIES_CONFIG, kafkaConfigData.getRetryCount());
        configs.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        configs.put(ProducerConfig.LINGER_MS_CONFIG, 5);
        configs.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384 * 100);
        configs.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, kafkaConfigData.getRequestTimeoutMs());
        configs.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, kafkaConfigData.getRetryBackoffMs());
        configs.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        configs.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, 5);
        configs.put(JsonSerializer.ADD_TYPE_INFO_HEADERS, false);
        configs.put(ProducerConfig.SECURITY_PROTOCOL_CONFIG, kafkaConfigData.getSecurityProtocol());
        addSaslConfig(configs);
        return new DefaultKafkaProducerFactory<>(configs);
    }

    @Bean
    public <T> KafkaTemplate<String, T> kafkaTemplate(ProducerFactory<String, T> producerFactory) {
        KafkaTemplate<String, T> kafkaTemplate = new KafkaTemplate<>(producerFactory);
        kafkaTemplate.setObservationEnabled(true);
        return kafkaTemplate;
    }

    @Bean
    public <T> ConsumerFactory<String, T> consumerFactory() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfigData.getBootstrapServers());
        configs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ErrorHandlingDeserializer.class);
        configs.put(ErrorHandlingDeserializer.KEY_DESERIALIZER_CLASS, StringDeserializer.class);
        configs.put(ErrorHandlingDeserializer.VALUE_DESERIALIZER_CLASS, JsonDeserializer.class);
        configs.put(JsonDeserializer.TRUSTED_PACKAGES, "com.fooddelivery.*");
        configs.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        configs.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, kafkaConfigData.getAutoOffsetReset());
        configs.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, kafkaConfigData.getSessionTimeoutMs());
        configs.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, kafkaConfigData.getHeartbeatIntervalMs());
        configs.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, kafkaConfigData.getMaxPollIntervals());
        configs.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, kafkaConfigData.getMaxPollRecords());
        configs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        configs.put(ConsumerConfig.SECURITY_PROTOCOL_CONFIG, kafkaConfigData.getSecurityProtocol());
        addSaslConfig(configs);
        return new DefaultKafkaConsumerFactory<>(configs);
    }

    @Bean
    public <T> ConcurrentKafkaListenerContainerFactory<String, T> kafkaListenerContainerFactory(
            ConsumerFactory<String, T> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, T> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        factory.setConcurrency(3);
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.getContainerProperties().setObservationEnabled(true);
        factory.setCommonErrorHandler(defaultErrorHandler());
        return factory;
    }

    @Bean
    public DefaultErrorHandler defaultErrorHandler() {
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                (record, exception) -> log.error("Failed to process Kafka record after retries. Topic: {}, Partition: {}, Offset: {}, Exception: {}",
                        record.topic(), record.partition(), record.offset(), exception.getMessage(), exception),
                new FixedBackOff(1000L, 3L)
        );
        errorHandler.addNotRetryableExceptions(
                IllegalArgumentException.class,
                NullPointerException.class
        );
        return errorHandler;
    }

    @Bean
    public List<NewTopic> kafkaTopics() {
        return kafkaConfigData.getTopicNamesToCreate().stream()
                .map(topicName -> TopicBuilder.name(topicName)
                        .partitions(kafkaConfigData.getNumPartitions())
                        .replicas(kafkaConfigData.getReplicationFactor())
                        .config("retention.ms", "604800000") // 7 days
                        .config("min.insync.replicas", "1")
                        .build())
                .toList();
    }

    private void addSaslConfig(Map<String, Object> configs) {
        if (kafkaConfigData.getSaslMechanism() != null && !kafkaConfigData.getSaslMechanism().isBlank()) {
            configs.put(SaslConfigs.SASL_MECHANISM, kafkaConfigData.getSaslMechanism());
            if (kafkaConfigData.getSaslJaasConfig() != null) {
                configs.put(SaslConfigs.SASL_JAAS_CONFIG, kafkaConfigData.getSaslJaasConfig());
            }
        }
    }
}

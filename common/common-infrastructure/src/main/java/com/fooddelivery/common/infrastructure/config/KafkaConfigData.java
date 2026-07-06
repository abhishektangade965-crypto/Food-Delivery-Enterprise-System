package com.fooddelivery.common.infrastructure.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * Kafka configuration properties loaded from application configuration.
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "kafka-config")
public class KafkaConfigData {

    private String bootstrapServers = "localhost:9092";
    private String schemaRegistryUrl;
    private String securityProtocol = "PLAINTEXT";
    private String saslMechanism;
    private String saslJaasConfig;
    private Integer numPartitions = 3;
    private Short replicationFactor = 1;
    private Integer requestTimeoutMs = 30000;
    private Integer retryCount = 3;
    private Long retryBackoffMs = 1000L;
    private String autoOffsetReset = "earliest";
    private String sessionTimeoutMs = "10000";
    private String heartbeatIntervalMs = "3000";
    private Integer maxPollIntervals = 300000;
    private Integer maxPollRecords = 500;
    private Integer maxPartitionFetchBytesDefault = 1048576;
    private Integer maxPartitionFetchBytesBoostFactor = 1;
    private List<String> topicNamesToCreate = List.of();

    public ProducerConfig producerConfig() {
        return new ProducerConfig();
    }

    public ConsumerConfig consumerConfig() {
        return new ConsumerConfig();
    }

    @Data
    public static class ProducerConfig {
        private String keySerializerClass = "org.apache.kafka.common.serialization.StringSerializer";
        private String valueSerializerClass = "org.springframework.kafka.support.serializer.JsonSerializer";
        private String compressionType = "snappy";
        private String acks = "all";
        private Integer batchSize = 16384;
        private Integer batchSizeBoostFactor = 100;
        private Long lingerMs = 5L;
        private Integer requestTimeoutMs = 60000;
        private Integer retryCount = Integer.MAX_VALUE;
    }

    @Data
    public static class ConsumerConfig {
        private String keyDeserializerClass = "org.apache.kafka.common.serialization.StringDeserializer";
        private String valueDeserializerClass = "org.springframework.kafka.support.serializer.JsonDeserializer";
        private String groupId;
        private Boolean autoCommitEnabled = false;
        private String autoOffsetReset = "earliest";
        private Integer concurrencyLevel = 3;
        private Integer pollTimeoutMs = 150;
        private Integer maxPollRecords = 500;
    }
}

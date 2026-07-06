package com.fooddelivery.common.infrastructure.config;

import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    @Value("${spring.data.redis.cluster.nodes:}")
    private String clusterNodes;

    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        if (clusterNodes != null && !clusterNodes.isBlank()) {
            log.info("Configuring Redisson in Cluster mode pointing to nodes: {}", clusterNodes);
            var clusterConfig = config.useClusterServers()
                    .setScanInterval(2000);
            
            for (String node : clusterNodes.split(",")) {
                String formattedNode = node.startsWith("redis://") || node.startsWith("rediss://") 
                        ? node 
                        : "redis://" + node;
                clusterConfig.addNodeAddress(formattedNode);
            }
            if (redisPassword != null && !redisPassword.isBlank()) {
                clusterConfig.setPassword(redisPassword);
            }
        } else {
            log.info("Configuring Redisson in Single Server mode pointing to {}:{}", redisHost, redisPort);
            var singleConfig = config.useSingleServer()
                    .setAddress("redis://" + redisHost + ":" + redisPort)
                    .setDatabase(redisDatabase);
            if (redisPassword != null && !redisPassword.isBlank()) {
                singleConfig.setPassword(redisPassword);
            }
        }
        
        return Redisson.create(config);
    }
}

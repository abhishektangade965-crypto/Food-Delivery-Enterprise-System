package com.fooddelivery.order;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = {"com.fooddelivery.order.dataaccess", "com.fooddelivery.common.infrastructure"})
@EnableJpaRepositories(basePackages = {"com.fooddelivery.order.dataaccess", "com.fooddelivery.common.infrastructure"})
@SpringBootApplication(basePackages = "com.fooddelivery")
public class OrderServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderServiceApplication.class, args);
    }
}

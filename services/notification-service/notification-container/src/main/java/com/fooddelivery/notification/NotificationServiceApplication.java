package com.fooddelivery.notification;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = {"com.fooddelivery.notification.dataaccess", "com.fooddelivery.common.infrastructure"})
@EnableJpaRepositories(basePackages = {"com.fooddelivery.notification.dataaccess", "com.fooddelivery.common.infrastructure"})
@SpringBootApplication(scanBasePackages = "com.fooddelivery")
public class NotificationServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }
}

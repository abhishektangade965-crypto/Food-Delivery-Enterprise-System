package com.fooddelivery.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = {"com.fooddelivery.user.dataaccess", "com.fooddelivery.common.infrastructure"})
@EnableJpaRepositories(basePackages = {"com.fooddelivery.user.dataaccess", "com.fooddelivery.common.infrastructure"})
@SpringBootApplication(basePackages = "com.fooddelivery")
public class UserServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}

package com.fooddelivery.restaurant;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = {"com.fooddelivery.restaurant.dataaccess", "com.fooddelivery.common.infrastructure"})
@EnableJpaRepositories(basePackages = {"com.fooddelivery.restaurant.dataaccess", "com.fooddelivery.common.infrastructure"})
@SpringBootApplication(basePackages = "com.fooddelivery")
public class RestaurantServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantServiceApplication.class, args);
    }
}

package com.fooddelivery.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@EntityScan(basePackages = {"com.fooddelivery.payment.dataaccess", "com.fooddelivery.common.infrastructure"})
@EnableJpaRepositories(basePackages = {"com.fooddelivery.payment.dataaccess", "com.fooddelivery.common.infrastructure"})
@SpringBootApplication(basePackages = "com.fooddelivery")
public class PaymentServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}

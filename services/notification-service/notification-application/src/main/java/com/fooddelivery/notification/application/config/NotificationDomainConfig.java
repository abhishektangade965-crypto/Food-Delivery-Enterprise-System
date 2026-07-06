package com.fooddelivery.notification.application.config;

import com.fooddelivery.notification.domain.service.NotificationDomainService;
import com.fooddelivery.notification.domain.service.NotificationDomainServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class NotificationDomainConfig {

    @Bean
    public NotificationDomainService notificationDomainService() {
        return new NotificationDomainServiceImpl();
    }
}

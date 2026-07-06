package com.fooddelivery.notification.application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "notification-service")
public class NotificationTemplateConfig {

    private Map<String, Template> templates = new HashMap<>();

    @Getter
    @Setter
    public static class Template {
        private String title;
        private String body;
    }
}

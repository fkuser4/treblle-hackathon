package com.hackathon.sdk.config;

import com.hackathon.sdk.filter.MonitoringFilter;
import com.hackathon.sdk.service.MonitoringService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

/**
 * Auto-configuration for the monitoring SDK.
 * This will be automatically loaded by Spring Boot when @EnableMonitoring is used.
 */
@Slf4j
@Configuration
@EnableAsync
@EnableConfigurationProperties(MonitoringProperties.class)
@ComponentScan(basePackages = "com.hackathon.sdk")
@ConditionalOnProperty(prefix = "monitoring", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MonitoringAutoConfiguration {

    @Bean
    public RestTemplate monitoringRestTemplate(RestTemplateBuilder builder) {
        return builder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(5))
                .build();
    }

    @Bean
    public FilterRegistrationBean<MonitoringFilter> monitoringFilterRegistration(
            MonitoringService monitoringService,
            MonitoringProperties properties) {

        log.info("API Monitoring SDK Initialized");
        log.info("Project ID: {:<44} ", properties.getProjectId());
        log.info("Backend URL: {:<43} ", properties.getBackendUrl());
        log.info("Async Mode: {:<44} ", properties.isAsync());

        FilterRegistrationBean<MonitoringFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new MonitoringFilter(monitoringService, properties));
        registration.addUrlPatterns("/*");
        registration.setOrder(1);
        registration.setName("monitoringFilter");

        return registration;
    }
}
package com.hackathon.sdk.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "monitoring")
public class MonitoringProperties {

    /**
     * API key for authentication with monitoring backend
     */
    private String apiKey;

    /**
     * Project identifier for grouping requests
     */
    private String projectId;

    /**
     * Backend API base URL
     */
    private String backendUrl = "http://localhost:8080/api";

    /**
     * Enable/disable monitoring
     */
    private boolean enabled = true;

    /**
     * Async processing enabled
     */
    private boolean async = true;

    /**
     * Maximum request body size to capture (in bytes)
     */
    private int maxBodySize = 10000;

    /**
     * Maximum response body size to capture (in bytes)
     */
    private int maxResponseSize = 10000;
}
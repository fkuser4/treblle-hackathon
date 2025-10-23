package com.hackathon.sdk.service;

import com.hackathon.sdk.client.MonitoringApiClient;
import com.hackathon.sdk.config.MonitoringProperties;
import com.hackathon.sdk.model.ApiRequestPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service responsible for capturing and sending monitoring data.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MonitoringService {

    private final MonitoringApiClient apiClient;
    private final MonitoringProperties properties;

    /**
     * Capture API request data and send to backend.
     * Uses async or sync based on configuration.
     */
    public void captureRequest(ApiRequestPayload payload) {
        if (properties.isAsync()) {
            captureRequestAsync(payload);
        } else {
            sendRequest(payload);
        }
    }

    /**
     * Async method to send request data.
     */
    @Async
    public void captureRequestAsync(ApiRequestPayload payload) {
        sendRequest(payload);
    }

    /**
     * Send request data to backend API.
     * Fails silently to avoid breaking the main application.
     */
    private void sendRequest(ApiRequestPayload payload) {
        try {
            apiClient.sendRequest(payload);
            log.debug("Successfully sent monitoring data for {} {}",
                    payload.getMethod(), payload.getPath());
        } catch (Exception e) {
            log.error("Failed to send monitoring data: {}", e.getMessage());
            // Fail silently - monitoring should never break the application
        }
    }
}
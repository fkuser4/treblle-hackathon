package com.hackathon.sdk.client;

import com.hackathon.sdk.config.MonitoringProperties;
import com.hackathon.sdk.model.ApiRequestPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for communicating with the monitoring backend API.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MonitoringApiClient {

    private final RestTemplate restTemplate;
    private final MonitoringProperties properties;

    /**
     * Send captured request data to the backend API.
     */
    public void sendRequest(ApiRequestPayload payload) {
        String url = properties.getBackendUrl() + "/requests";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-API-Key", properties.getApiKey());

        HttpEntity<ApiRequestPayload> request = new HttpEntity<>(payload, headers);

        restTemplate.postForEntity(url, request, Void.class);

        log.trace("Sent monitoring data to: {}", url);
    }
}
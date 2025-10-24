package com.hackathon.sdk.client;

import com.hackathon.sdk.config.MonitoringProperties;
import com.hackathon.sdk.model.ApiRequestPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitoringApiClientTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private MonitoringProperties properties;

    @InjectMocks
    private MonitoringApiClient apiClient;

    @Captor
    private ArgumentCaptor<HttpEntity<ApiRequestPayload>> requestCaptor;

    private ApiRequestPayload testPayload;

    @BeforeEach
    void setUp() {
        testPayload = ApiRequestPayload.builder()
                .projectId("test-project")
                .method("GET")
                .path("/api/users")
                .responseStatus(200)
                .responseTime(150L)
                .createdAt(LocalDateTime.now())
                .build();

        when(properties.getBackendUrl()).thenReturn("http://localhost:8080/api");
        when(properties.getApiKey()).thenReturn("test-api-key");
    }

    @Test
    void sendRequest_shouldSendPayloadToCorrectUrl() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        apiClient.sendRequest(testPayload);

        verify(restTemplate).postForEntity(
                eq("http://localhost:8080/api/requests"),
                requestCaptor.capture(),
                eq(Void.class)
        );
    }

    @Test
    void sendRequest_shouldIncludeApiKeyInHeaders() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        apiClient.sendRequest(testPayload);

        verify(restTemplate).postForEntity(anyString(), requestCaptor.capture(), eq(Void.class));
        HttpEntity<ApiRequestPayload> capturedRequest = requestCaptor.getValue();

        assertThat(capturedRequest.getHeaders().get("X-API-Key"))
                .containsExactly("test-api-key");
    }

    @Test
    void sendRequest_shouldIncludePayloadInBody() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        apiClient.sendRequest(testPayload);

        verify(restTemplate).postForEntity(anyString(), requestCaptor.capture(), eq(Void.class));
        HttpEntity<ApiRequestPayload> capturedRequest = requestCaptor.getValue();

        assertThat(capturedRequest.getBody()).isEqualTo(testPayload);
    }

    @Test
    void sendRequest_shouldSetContentTypeHeader() {
        when(restTemplate.postForEntity(anyString(), any(), eq(Void.class)))
                .thenReturn(ResponseEntity.ok().build());

        apiClient.sendRequest(testPayload);

        verify(restTemplate).postForEntity(anyString(), requestCaptor.capture(), eq(Void.class));
        HttpEntity<ApiRequestPayload> capturedRequest = requestCaptor.getValue();

        assertThat(capturedRequest.getHeaders().getContentType().toString())
                .isEqualTo("application/json");
    }
}
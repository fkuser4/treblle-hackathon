package com.hackathon.sdk.service;

import com.hackathon.sdk.client.MonitoringApiClient;
import com.hackathon.sdk.config.MonitoringProperties;
import com.hackathon.sdk.model.ApiRequestPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringServiceTest {

    @Mock
    private MonitoringApiClient apiClient;

    @Mock
    private MonitoringProperties properties;

    @InjectMocks
    private MonitoringService monitoringService;

    private ApiRequestPayload testPayload;

    @BeforeEach
    void setUp() {
        testPayload = ApiRequestPayload.builder()
                .projectId("test-project")
                .method("GET")
                .path("/api/test")
                .responseStatus(200)
                .responseTime(100L)
                .build();
    }

    @Test
    void captureRequest_whenAsyncEnabled_shouldCallAsyncMethod() {
        // Given
        when(properties.isAsync()).thenReturn(true);

        // When
        monitoringService.captureRequest(testPayload);

        // Then
        verify(properties).isAsync();
        // Note: We can't easily verify async call, but we ensure no exception thrown
    }

    @Test
    void captureRequest_whenAsyncDisabled_shouldCallSyncMethod() {
        // Given
        when(properties.isAsync()).thenReturn(false);
        doNothing().when(apiClient).sendRequest(any(ApiRequestPayload.class));

        // When
        monitoringService.captureRequest(testPayload);

        // Then
        verify(properties).isAsync();
        verify(apiClient).sendRequest(testPayload);
    }

    @Test
    void captureRequest_whenApiClientThrowsException_shouldNotPropagateException() {
        // Given
        when(properties.isAsync()).thenReturn(false);
        doThrow(new RuntimeException("API Error")).when(apiClient).sendRequest(any());

        // When & Then - should not throw exception
        monitoringService.captureRequest(testPayload);

        // Verify it attempted to send
        verify(apiClient).sendRequest(testPayload);
    }
}
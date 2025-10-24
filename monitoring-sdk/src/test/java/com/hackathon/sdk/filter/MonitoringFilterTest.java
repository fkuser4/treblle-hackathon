package com.hackathon.sdk.filter;

import com.hackathon.sdk.config.MonitoringProperties;
import com.hackathon.sdk.model.ApiRequestPayload;
import com.hackathon.sdk.service.MonitoringService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MonitoringFilterTest {

    @Mock
    private MonitoringService monitoringService;

    @Mock
    private MonitoringProperties properties;

    @Mock
    private FilterChain filterChain;

    @Captor
    private ArgumentCaptor<ApiRequestPayload> payloadCaptor;

    private MonitoringFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        filter = new MonitoringFilter(monitoringService, properties);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();

        when(properties.isEnabled()).thenReturn(true);
        when(properties.getProjectId()).thenReturn("test-project");
        when(properties.getMaxBodySize()).thenReturn(10000);
        when(properties.getMaxResponseSize()).thenReturn(10000);
    }

    @Test
    void doFilterInternal_whenMonitoringDisabled_shouldSkipMonitoring() throws ServletException, IOException {
        when(properties.isEnabled()).thenReturn(false);
        request.setRequestURI("/api/users");
        request.setMethod("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(monitoringService, never()).captureRequest(any());
    }

    @Test
    void doFilterInternal_whenActuatorEndpoint_shouldSkipMonitoring() throws ServletException, IOException {
        request.setRequestURI("/actuator/health");
        request.setMethod("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(monitoringService, never()).captureRequest(any());
    }

    @Test
    void doFilterInternal_whenSwaggerEndpoint_shouldSkipMonitoring() throws ServletException, IOException {
        request.setRequestURI("/swagger-ui/index.html");
        request.setMethod("GET");

        filter.doFilterInternal(request, response, filterChain);

        verify(monitoringService, never()).captureRequest(any());
    }

    @Test
    void doFilterInternal_whenValidRequest_shouldCaptureMonitoringData() throws ServletException, IOException {
        request.setRequestURI("/api/users");
        request.setMethod("GET");
        request.setQueryString("page=1");
        response.setStatus(200);

        doAnswer(invocation -> {
            response.getWriter().write("test response");
            return null;
        }).when(filterChain).doFilter(any(), any());


        filter.doFilterInternal(request, response, filterChain);

        verify(monitoringService).captureRequest(payloadCaptor.capture());
        ApiRequestPayload capturedPayload = payloadCaptor.getValue();

        assertThat(capturedPayload.getProjectId()).isEqualTo("test-project");
        assertThat(capturedPayload.getMethod()).isEqualTo("GET");
        assertThat(capturedPayload.getPath()).isEqualTo("/api/users");
        assertThat(capturedPayload.getQueryString()).isEqualTo("page=1");
        assertThat(capturedPayload.getResponseStatus()).isEqualTo(200);
        assertThat(capturedPayload.getResponseTime()).isGreaterThanOrEqualTo(0L);
        assertThat(capturedPayload.getCreatedAt()).isNotNull();
    }

    @Test
    void doFilterInternal_shouldCaptureRequestHeaders() throws ServletException, IOException {
        request.setRequestURI("/api/users");
        request.setMethod("GET");
        request.addHeader("Content-Type", "application/json");
        request.addHeader("User-Agent", "TestAgent");

        filter.doFilterInternal(request, response, filterChain);

        verify(monitoringService).captureRequest(payloadCaptor.capture());
        ApiRequestPayload capturedPayload = payloadCaptor.getValue();

        assertThat(capturedPayload.getRequestHeaders()).containsEntry("content-type", "application/json");
        assertThat(capturedPayload.getRequestHeaders()).containsEntry("user-agent", "TestAgent");
    }

    @Test
    void doFilterInternal_shouldMeasureResponseTime() throws ServletException, IOException {
        request.setRequestURI("/api/users");
        request.setMethod("GET");

        doAnswer(invocation -> {
            Thread.sleep(50);
            return null;
        }).when(filterChain).doFilter(any(), any());

        filter.doFilterInternal(request, response, filterChain);

        verify(monitoringService).captureRequest(payloadCaptor.capture());
        ApiRequestPayload capturedPayload = payloadCaptor.getValue();

        assertThat(capturedPayload.getResponseTime()).isGreaterThanOrEqualTo(50L);
    }
}
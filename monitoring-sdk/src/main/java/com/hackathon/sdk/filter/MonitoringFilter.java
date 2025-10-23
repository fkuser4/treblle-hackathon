package com.hackathon.sdk.filter;

import com.hackathon.sdk.config.MonitoringProperties;
import com.hackathon.sdk.model.ApiRequestPayload;
import com.hackathon.sdk.service.MonitoringService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Filter that intercepts all HTTP requests and captures monitoring data.
 */
@Slf4j
@RequiredArgsConstructor
public class MonitoringFilter extends OncePerRequestFilter {

    private final MonitoringService monitoringService;
    private final MonitoringProperties properties;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (!properties.isEnabled()) {
            filterChain.doFilter(request, response);
            return;
        }

        // Skip monitoring endpoints to avoid infinite loops
        String requestUri = request.getRequestURI();
        if (requestUri.contains("/actuator") ||
                requestUri.contains("/monitoring") ||
                requestUri.contains("/swagger") ||
                requestUri.contains("/v3/api-docs")) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();

        // Wrap request and response to cache bodies
        CachedBodyHttpServletRequest cachedRequest = new CachedBodyHttpServletRequest(request);
        CachedBodyHttpServletResponse cachedResponse = new CachedBodyHttpServletResponse(response);

        try {
            // Continue filter chain
            filterChain.doFilter(cachedRequest, cachedResponse);
        } finally {
            long endTime = System.currentTimeMillis();
            long responseTime = endTime - startTime;

            // Build payload
            ApiRequestPayload payload = buildPayload(
                    cachedRequest,
                    cachedResponse,
                    responseTime
            );

            // Send to monitoring service
            monitoringService.captureRequest(payload);
        }
    }

    private ApiRequestPayload buildPayload(CachedBodyHttpServletRequest request,
                                           CachedBodyHttpServletResponse response,
                                           long responseTime) {
        return ApiRequestPayload.builder()
                .projectId(properties.getProjectId())
                .method(request.getMethod())
                .path(request.getRequestURI())
                .queryString(request.getQueryString())
                .responseStatus(response.getStatus())
                .responseTime(responseTime)
                .createdAt(LocalDateTime.now())
                .requestHeaders(extractHeaders(request))
                .requestBody(extractRequestBody(request))
                .responseBody(extractResponseBody(response))
                .build();
    }

    private Map<String, String> extractHeaders(HttpServletRequest request) {
        Map<String, String> headers = new HashMap<>();
        Collections.list(request.getHeaderNames()).forEach(headerName ->
                headers.put(headerName, request.getHeader(headerName))
        );
        return headers;
    }

    private String extractRequestBody(CachedBodyHttpServletRequest request) {
        try {
            byte[] body = request.getCachedBody();
            if (body.length == 0) {
                return null;
            }
            if (body.length > properties.getMaxBodySize()) {
                return new String(body, 0, properties.getMaxBodySize(), StandardCharsets.UTF_8) + "... [truncated]";
            }
            return new String(body, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed to extract request body", e);
            return null;
        }
    }

    private String extractResponseBody(CachedBodyHttpServletResponse response) {
        try {
            byte[] body = response.getCachedBody();
            if (body.length == 0) {
                return null;
            }
            if (body.length > properties.getMaxResponseSize()) {
                return new String(body, 0, properties.getMaxResponseSize(), StandardCharsets.UTF_8) + "... [truncated]";
            }
            return new String(body, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Failed to extract response body", e);
            return null;
        }
    }
}
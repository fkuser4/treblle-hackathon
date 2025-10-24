package com.hackathon.backend.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiKeyFilter extends OncePerRequestFilter {

    private static final String VALID_API_KEY = "hackathon-2025-super-secret-key";
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        if (requestUri.contains("/swagger") ||
                requestUri.contains("/api-docs") ||
                requestUri.contains("/h2-console")) {
            filterChain.doFilter(request, response);
            return;
        }

        if (!requestUri.startsWith("/api/requests") && !requestUri.startsWith("/api/health-metrics")) {
            filterChain.doFilter(request, response);
            return;
        }

        String apiKey = request.getHeader("X-API-Key");

        if (apiKey == null || apiKey.isEmpty()) {
            log.warn("Missing API key in request to: {}", requestUri);
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "API key is required");
            return;
        }

        if (!VALID_API_KEY.equals(apiKey)) {
            log.warn("Invalid API key attempted: {}", apiKey.substring(0, Math.min(8, apiKey.length())) + "...");
            sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Invalid API key");
            return;
        }

        log.debug("Valid API key - request authorized");
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        Map<String, Object> errorBody = Map.of(
                "error", message,
                "status", status,
                "timestamp", java.time.LocalDateTime.now().toString()
        );
        response.getWriter().write(objectMapper.writeValueAsString(errorBody));
    }
}
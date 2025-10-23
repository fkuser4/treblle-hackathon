package com.hackathon.sdk.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestPayload {

    private String projectId;

    private String method;

    private String path;

    private Integer responseStatus;

    private String responseBody;

    private Long responseTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Map<String, String> requestHeaders;

    private String requestBody;

    private String queryString;
}
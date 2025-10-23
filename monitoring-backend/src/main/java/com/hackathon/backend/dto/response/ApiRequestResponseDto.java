package com.hackathon.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequestResponseDto {

    private UUID id;
    private String projectId;
    private String method;
    private String path;
    private String queryString;
    private Integer responseStatus;
    private String responseBody;
    private Long responseTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Map<String, String> requestHeaders;
    private String requestBody;
}
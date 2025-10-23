package com.hackathon.backend.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class CreateApiRequestDto {

    @NotBlank(message = "Project ID is required")
    private String projectId;

    @NotBlank(message = "Method is required")
    private String method;

    @NotBlank(message = "Path is required")
    private String path;

    private String queryString;

    private Integer responseStatus;

    private String responseBody;

    @NotNull(message = "Response time is required")
    private Long responseTime;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    private Map<String, String> requestHeaders;

    private String requestBody;
}
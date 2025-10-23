package com.hackathon.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthMetricsResponseDto {

    private UUID id;
    private String projectId;
    private String endpoint;
    private Double avgResponseTime;
    private Long minResponseTime;
    private Long maxResponseTime;
    private Long totalRequests;
    private Long successCount;
    private Long errorCount;
    private Double successRate;
    private Integer healthScore;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime lastUpdated;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime firstSeen;
}
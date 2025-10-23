package com.hackathon.backend.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthMetricsFilterDto {

    private String projectId;
    private Integer minHealthScore;
    private Integer maxHealthScore;
    private Double minSuccessRate;
    private Double maxSuccessRate;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String search;
    private String sortBy = "lastUpdated"; // lastUpdated, healthScore, avgResponseTime, successRate
    private String sortDirection = "DESC"; // ASC or DESC
    private Integer page = 0;
    private Integer size = 20;
}
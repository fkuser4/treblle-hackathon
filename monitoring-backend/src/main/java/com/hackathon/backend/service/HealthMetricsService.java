package com.hackathon.backend.service;

import com.hackathon.backend.dto.request.HealthMetricsFilterDto;
import com.hackathon.backend.dto.response.HealthMetricsListItemDto;
import com.hackathon.backend.dto.response.HealthMetricsResponseDto;
import com.hackathon.backend.dto.response.PagedResponseDto;

import java.util.List;
import java.util.UUID;

public interface HealthMetricsService {

    /**
     * Update health metrics for a specific endpoint
     */
    void updateMetricsForEndpoint(String projectId, String endpoint);

    /**
     * Get health metrics by ID
     */
    HealthMetricsResponseDto getMetricsById(UUID id);

    /**
     * Get health metrics for specific endpoint
     */
    HealthMetricsResponseDto getMetricsByEndpoint(String projectId, String endpoint);

    /**
     * Get latest metrics per endpoint (List View)
     */
    List<HealthMetricsListItemDto> getListView(String projectId);

    /**
     * Get all metrics with pagination and filters (Table View - all historical snapshots)
     */
    PagedResponseDto<HealthMetricsResponseDto> getTableView(HealthMetricsFilterDto filter);

    /**
     * Search metrics by endpoint
     */
    PagedResponseDto<HealthMetricsResponseDto> searchMetrics(String projectId, String search, int page, int size);

    /**
     * Get total count for project
     */
    long getTotalCount(String projectId);

    /**
     * Recalculate all metrics for a project
     */
    void recalculateAllMetrics(String projectId);
}
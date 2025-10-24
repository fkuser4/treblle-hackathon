package com.hackathon.backend.controller;

import com.hackathon.backend.dto.request.HealthMetricsFilterDto;
import com.hackathon.backend.dto.response.HealthMetricsListItemDto;
import com.hackathon.backend.dto.response.HealthMetricsResponseDto;
import com.hackathon.backend.dto.response.PagedResponseDto;
import com.hackathon.backend.service.HealthMetricsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/health-metrics")
@RequiredArgsConstructor
@Tag(name = "Health Metrics", description = "Endpoints for endpoint health monitoring and analytics")
public class HealthMetricsController {

    private final HealthMetricsService service;

    @GetMapping("/{id}")
    @Operation(summary = "Get metrics by ID", description = "Retrieve health metrics by ID")
    public ResponseEntity<HealthMetricsResponseDto> getMetricsById(@PathVariable UUID id) {
        HealthMetricsResponseDto response = service.getMetricsById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/endpoint")
    @Operation(summary = "Get metrics by endpoint", description = "Retrieve health metrics for specific endpoint")
    public ResponseEntity<HealthMetricsResponseDto> getMetricsByEndpoint(
            @RequestParam String projectId,
            @RequestParam String endpoint) {
        HealthMetricsResponseDto response = service.getMetricsByEndpoint(projectId, endpoint);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    @Operation(summary = "Get list view", description = "Get latest metrics per endpoint")
    public ResponseEntity<List<HealthMetricsListItemDto>> getListView(
            @RequestParam String projectId) {
        List<HealthMetricsListItemDto> response = service.getListView(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/table")
    @Operation(summary = "Get table view", description = "Get all metrics with pagination and filters (historical snapshots)")
    public ResponseEntity<PagedResponseDto<HealthMetricsResponseDto>> getTableView(
            @RequestParam String projectId,
            @RequestParam(required = false) Integer minHealthScore,
            @RequestParam(required = false) Integer maxHealthScore,
            @RequestParam(required = false) Double minSuccessRate,
            @RequestParam(required = false) Double maxSuccessRate,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "lastUpdated") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        HealthMetricsFilterDto filter = HealthMetricsFilterDto.builder()
                .projectId(projectId)
                .minHealthScore(minHealthScore)
                .maxHealthScore(maxHealthScore)
                .minSuccessRate(minSuccessRate)
                .maxSuccessRate(maxSuccessRate)
                .startDate(startDate)
                .endDate(endDate)
                .search(search)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();

        PagedResponseDto<HealthMetricsResponseDto> response = service.getTableView(filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search metrics", description = "Search metrics by endpoint path")
    public ResponseEntity<PagedResponseDto<HealthMetricsResponseDto>> searchMetrics(
            @RequestParam String projectId,
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponseDto<HealthMetricsResponseDto> response = service.searchMetrics(projectId, search, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    @Operation(summary = "Get total count", description = "Get total metrics count for project")
    public ResponseEntity<Long> getTotalCount(@RequestParam String projectId) {
        long count = service.getTotalCount(projectId);
        return ResponseEntity.ok(count);
    }

    @PostMapping("/recalculate")
    @Operation(summary = "Recalculate metrics", description = "Recalculate all health metrics for a project")
    public ResponseEntity<String> recalculateMetrics(@RequestParam String projectId) {
        log.info("Recalculating metrics for project: {}", projectId);
        service.recalculateAllMetrics(projectId);
        return ResponseEntity.ok("Metrics recalculation started for project: " + projectId);
    }
}
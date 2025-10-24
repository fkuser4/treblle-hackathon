package com.hackathon.backend.service;

import com.hackathon.backend.dto.request.HealthMetricsFilterDto;
import com.hackathon.backend.dto.response.HealthMetricsListItemDto;
import com.hackathon.backend.dto.response.HealthMetricsResponseDto;
import com.hackathon.backend.dto.response.PagedResponseDto;
import com.hackathon.backend.entity.ApiRequest;
import com.hackathon.backend.entity.EndpointHealthMetrics;
import com.hackathon.backend.exception.ResourceNotFoundException;
import com.hackathon.backend.mapper.HealthMetricsMapper;
import com.hackathon.backend.repository.ApiRequestRepository;
import com.hackathon.backend.repository.EndpointHealthMetricsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.LongSummaryStatistics;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class HealthMetricsServiceImpl implements HealthMetricsService {

    private final EndpointHealthMetricsRepository metricsRepository;
    private final ApiRequestRepository requestRepository;
    private final HealthMetricsMapper mapper;

    @Override
    @Async
    @Transactional
    public void updateMetricsForEndpoint(String projectId, String endpoint) {
        log.debug("Updating metrics for endpoint: {} in project: {}", endpoint, projectId);

        List<ApiRequest> requests = requestRepository.findByProjectIdAndPath(projectId, endpoint);

        if (requests.isEmpty()) {
            log.debug("No requests found for endpoint: {}", endpoint);
            return;
        }

        EndpointHealthMetrics metrics = calculateMetrics(projectId, endpoint, requests);
        metricsRepository.save(metrics);

        log.debug("Successfully updated metrics for endpoint: {}", endpoint);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthMetricsResponseDto getMetricsById(UUID id) {
        EndpointHealthMetrics metrics = metricsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Health metrics not found with id: " + id));
        return mapper.toResponseDto(metrics);
    }

    @Override
    @Transactional(readOnly = true)
    public HealthMetricsResponseDto getMetricsByEndpoint(String projectId, String endpoint) {
        EndpointHealthMetrics metrics = metricsRepository.findByProjectIdAndEndpoint(projectId, endpoint)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Health metrics not found for endpoint: " + endpoint));
        return mapper.toResponseDto(metrics);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HealthMetricsListItemDto> getListView(String projectId) {
        log.debug("Getting metrics list view for project: {}", projectId);
        List<EndpointHealthMetrics> metrics = metricsRepository.findLatestMetricsPerEndpoint(projectId);
        return mapper.toListItemDtoList(metrics);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<HealthMetricsResponseDto> getTableView(HealthMetricsFilterDto filter) {
        log.debug("Getting metrics table view with filter: {}", filter);

        Pageable pageable = createPageable(filter.getPage(), filter.getSize(),
                filter.getSortBy(), filter.getSortDirection());

        Page<EndpointHealthMetrics> page = applyFilters(filter, pageable);

        return buildPagedResponse(page, mapper.toResponseDtoList(page.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<HealthMetricsResponseDto> searchMetrics(String projectId, String search,
                                                                    int page, int size) {
        log.debug("Searching metrics for project: {}, search: {}", projectId, search);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "lastUpdated"));
        Page<EndpointHealthMetrics> resultPage = metricsRepository.searchByEndpoint(projectId, search, pageable);

        return buildPagedResponse(resultPage, mapper.toResponseDtoList(resultPage.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalCount(String projectId) {
        return metricsRepository.countByProjectId(projectId);
    }

    @Override
    @Transactional
    public void recalculateAllMetrics(String projectId) {
        log.info("Recalculating all metrics for project: {}", projectId);

        List<String> uniquePaths = requestRepository.findByProjectId(projectId)
                .stream()
                .map(ApiRequest::getPath)
                .distinct()
                .toList();

        uniquePaths.forEach(path -> updateMetricsForEndpoint(projectId, path));

        log.info("Completed recalculation of {} endpoints for project: {}",
                uniquePaths.size(), projectId);
    }

    private EndpointHealthMetrics calculateMetrics(String projectId, String endpoint,
                                                   List<ApiRequest> requests) {
        LongSummaryStatistics stats = requests.stream()
                .mapToLong(ApiRequest::getResponseTime)
                .summaryStatistics();

        long successCount = requests.stream()
                .filter(r -> r.getResponseStatus() != null && r.getResponseStatus() >= 200 && r.getResponseStatus() < 300)
                .count();

        long errorCount = requests.stream()
                .filter(r -> r.getResponseStatus() != null && r.getResponseStatus() >= 400)
                .count();

        long totalRequests = requests.size();
        double successRate = totalRequests > 0 ? (successCount * 100.0 / totalRequests) : 0.0;

        int healthScore = calculateHealthScore(stats.getAverage(), successRate);

        LocalDateTime firstSeen = requests.stream()
                .map(ApiRequest::getCreatedAt)
                .min(LocalDateTime::compareTo)
                .orElse(LocalDateTime.now());

        return EndpointHealthMetrics.builder()
                .projectId(projectId)
                .endpoint(endpoint)
                .avgResponseTime(stats.getAverage())
                .minResponseTime(stats.getMin())
                .maxResponseTime(stats.getMax())
                .totalRequests(totalRequests)
                .successCount(successCount)
                .errorCount(errorCount)
                .successRate(successRate)
                .healthScore(healthScore)
                .lastUpdated(LocalDateTime.now())
                .firstSeen(firstSeen)
                .build();
    }

    private int calculateHealthScore(double avgResponseTime, double successRate) {
        double successScore = successRate;

        double responseScore;
        if (avgResponseTime < 100) {
            responseScore = 100;
        } else if (avgResponseTime < 300) {
            responseScore = 90;
        } else if (avgResponseTime < 500) {
            responseScore = 70;
        } else if (avgResponseTime < 1000) {
            responseScore = 50;
        } else if (avgResponseTime < 2000) {
            responseScore = 30;
        } else {
            responseScore = 10;
        }

        return (int) Math.round((successScore * 0.5) + (responseScore * 0.5));
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    private Page<EndpointHealthMetrics> applyFilters(HealthMetricsFilterDto filter, Pageable pageable) {
        String projectId = filter.getProjectId();

        if (filter.getMaxHealthScore() != null && filter.getMinHealthScore() != null) {
            return metricsRepository.findByProjectId(projectId, pageable);
        }

        if (filter.getMinSuccessRate() != null) {
            return metricsRepository.findByProjectIdAndSuccessRateLessThan(
                    projectId, filter.getMinSuccessRate(), pageable);
        }

        if (filter.getStartDate() != null && filter.getEndDate() != null) {
            return metricsRepository.findByProjectIdAndLastUpdatedBetween(
                    projectId, filter.getStartDate(), filter.getEndDate(), pageable);
        }

        if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
            return metricsRepository.searchByEndpoint(projectId, filter.getSearch(), pageable);
        }

        return metricsRepository.findByProjectId(projectId, pageable);
    }

    private <T> PagedResponseDto<T> buildPagedResponse(Page<?> page, List<T> content) {
        return PagedResponseDto.<T>builder()
                .content(content)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .first(page.isFirst())
                .last(page.isLast())
                .build();
    }
}
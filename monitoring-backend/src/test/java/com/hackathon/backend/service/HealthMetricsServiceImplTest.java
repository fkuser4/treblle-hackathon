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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthMetricsServiceImplTest {

    @Mock
    private EndpointHealthMetricsRepository metricsRepository;

    @Mock
    private ApiRequestRepository requestRepository;

    @Mock
    private HealthMetricsMapper mapper;

    @InjectMocks
    private HealthMetricsServiceImpl service;

    private EndpointHealthMetrics metrics;
    private HealthMetricsResponseDto responseDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        metrics = EndpointHealthMetrics.builder()
                .id(testId)
                .projectId("test-project")
                .endpoint("/api/users")
                .avgResponseTime(150.0)
                .minResponseTime(50L)
                .maxResponseTime(300L)
                .totalRequests(100L)
                .successCount(95L)
                .errorCount(5L)
                .successRate(95.0)
                .healthScore(85)
                .lastUpdated(LocalDateTime.now())
                .firstSeen(LocalDateTime.now().minusDays(7))
                .build();

        responseDto = HealthMetricsResponseDto.builder()
                .id(testId)
                .projectId("test-project")
                .endpoint("/api/users")
                .avgResponseTime(150.0)
                .totalRequests(100L)
                .successRate(95.0)
                .healthScore(85)
                .build();
    }

    @Test
    void updateMetricsForEndpoint_withExistingMetrics_shouldUpdate() {
        List<ApiRequest> requests = Arrays.asList(
                createApiRequest(200, 100L),
                createApiRequest(200, 150L),
                createApiRequest(500, 200L)
        );

        when(requestRepository.findByProjectIdAndPath("test-project", "/api/users"))
                .thenReturn(requests);
        when(metricsRepository.findByProjectIdAndEndpoint("test-project", "/api/users"))
                .thenReturn(Optional.of(metrics));
        when(metricsRepository.save(any(EndpointHealthMetrics.class))).thenReturn(metrics);

        service.updateMetricsForEndpoint("test-project", "/api/users");

        verify(metricsRepository).save(any(EndpointHealthMetrics.class));
    }

    @Test
    void updateMetricsForEndpoint_withNewEndpoint_shouldCreate() {
        List<ApiRequest> requests = Arrays.asList(createApiRequest(200, 100L));

        when(requestRepository.findByProjectIdAndPath("test-project", "/api/new"))
                .thenReturn(requests);
        when(metricsRepository.findByProjectIdAndEndpoint("test-project", "/api/new"))
                .thenReturn(Optional.empty());
        when(metricsRepository.save(any(EndpointHealthMetrics.class))).thenReturn(metrics);

        service.updateMetricsForEndpoint("test-project", "/api/new");

        verify(metricsRepository).save(any(EndpointHealthMetrics.class));
    }

    @Test
    void updateMetricsForEndpoint_withNoRequests_shouldNotSave() {
        when(requestRepository.findByProjectIdAndPath("test-project", "/api/empty"))
                .thenReturn(Arrays.asList());

        service.updateMetricsForEndpoint("test-project", "/api/empty");

        verify(metricsRepository, never()).save(any());
    }

    @Test
    void getMetricsById_whenExists_shouldReturnDto() {
        when(metricsRepository.findById(testId)).thenReturn(Optional.of(metrics));
        when(mapper.toResponseDto(metrics)).thenReturn(responseDto);

        HealthMetricsResponseDto result = service.getMetricsById(testId);

        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    void getMetricsById_whenNotExists_shouldThrowException() {
        when(metricsRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getMetricsById(testId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Health metrics not found");
    }

    @Test
    void getMetricsByEndpoint_whenExists_shouldReturnDto() {
        when(metricsRepository.findByProjectIdAndEndpoint("test-project", "/api/users"))
                .thenReturn(Optional.of(metrics));
        when(mapper.toResponseDto(metrics)).thenReturn(responseDto);

        HealthMetricsResponseDto result = service.getMetricsByEndpoint("test-project", "/api/users");

        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    void getListView_shouldReturnLatestMetrics() {
        List<EndpointHealthMetrics> metricsList = Arrays.asList(metrics);
        List<HealthMetricsListItemDto> dtos = Arrays.asList(
                HealthMetricsListItemDto.builder()
                        .id(testId)
                        .endpoint("/api/users")
                        .avgResponseTime(150.0)
                        .totalRequests(100L)
                        .successRate(95.0)
                        .healthScore(85)
                        .build()
        );

        when(metricsRepository.findLatestMetricsPerEndpoint("test-project")).thenReturn(metricsList);
        when(mapper.toListItemDtoList(metricsList)).thenReturn(dtos);

        List<HealthMetricsListItemDto> result = service.getListView("test-project");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEndpoint()).isEqualTo("/api/users");
    }

    @Test
    void getTableView_shouldReturnPagedResults() {
        HealthMetricsFilterDto filter = HealthMetricsFilterDto.builder()
                .projectId("test-project")
                .page(0)
                .size(10)
                .sortBy("lastUpdated")
                .sortDirection("DESC")
                .build();

        Page<EndpointHealthMetrics> page = new PageImpl<>(Arrays.asList(metrics));
        when(metricsRepository.findByProjectId(eq("test-project"), any(Pageable.class))).thenReturn(page);
        when(mapper.toResponseDtoList(any())).thenReturn(Arrays.asList(responseDto));

        PagedResponseDto<HealthMetricsResponseDto> result = service.getTableView(filter);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void getTotalCount_shouldReturnCount() {
        when(metricsRepository.countByProjectId("test-project")).thenReturn(15L);

        long result = service.getTotalCount("test-project");

        assertThat(result).isEqualTo(15L);
    }

    private ApiRequest createApiRequest(int status, long responseTime) {
        return ApiRequest.builder()
                .projectId("test-project")
                .method("GET")
                .path("/api/users")
                .responseStatus(status)
                .responseTime(responseTime)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
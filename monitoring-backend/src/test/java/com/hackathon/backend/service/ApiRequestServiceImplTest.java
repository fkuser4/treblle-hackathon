package com.hackathon.backend.service;

import com.hackathon.backend.dto.request.ApiRequestFilterDto;
import com.hackathon.backend.dto.request.CreateApiRequestDto;
import com.hackathon.backend.dto.response.ApiRequestListItemDto;
import com.hackathon.backend.dto.response.ApiRequestResponseDto;
import com.hackathon.backend.dto.response.PagedResponseDto;
import com.hackathon.backend.entity.ApiRequest;
import com.hackathon.backend.exception.ResourceNotFoundException;
import com.hackathon.backend.mapper.ApiRequestMapper;
import com.hackathon.backend.repository.ApiRequestRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApiRequestServiceImplTest {

    @Mock
    private ApiRequestRepository repository;

    @Mock
    private ApiRequestMapper mapper;

    @Mock
    private HealthMetricsService healthMetricsService;

    @InjectMocks
    private ApiRequestServiceImpl service;

    private CreateApiRequestDto createDto;
    private ApiRequest entity;
    private ApiRequestResponseDto responseDto;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();

        createDto = CreateApiRequestDto.builder()
                .projectId("test-project")
                .method("GET")
                .path("/api/users")
                .responseStatus(200)
                .responseTime(100L)
                .createdAt(LocalDateTime.now())
                .build();

        entity = ApiRequest.builder()
                .id(testId)
                .projectId("test-project")
                .method("GET")
                .path("/api/users")
                .responseStatus(200)
                .responseTime(100L)
                .createdAt(LocalDateTime.now())
                .build();

        responseDto = ApiRequestResponseDto.builder()
                .id(testId)
                .projectId("test-project")
                .method("GET")
                .path("/api/users")
                .responseStatus(200)
                .responseTime(100L)
                .build();
    }

    @Test
    void createRequest_shouldSaveAndReturnDto() {
        when(mapper.toEntity(createDto)).thenReturn(entity);
        when(repository.save(entity)).thenReturn(entity);
        when(mapper.toResponseDto(entity)).thenReturn(responseDto);

        ApiRequestResponseDto result = service.createRequest(createDto);

        assertThat(result).isEqualTo(responseDto);
        verify(repository).save(entity);
        verify(healthMetricsService).updateMetricsForEndpoint("test-project", "/api/users");
    }

    @Test
    void getRequestById_whenExists_shouldReturnDto() {
        when(repository.findById(testId)).thenReturn(Optional.of(entity));
        when(mapper.toResponseDto(entity)).thenReturn(responseDto);

        ApiRequestResponseDto result = service.getRequestById(testId);

        assertThat(result).isEqualTo(responseDto);
    }

    @Test
    void getRequestById_whenNotExists_shouldThrowException() {
        when(repository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getRequestById(testId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("API Request not found");
    }

    @Test
    void getListView_shouldReturnLatestPerPath() {
        List<ApiRequest> requests = Arrays.asList(entity);
        List<ApiRequestListItemDto> dtos = Arrays.asList(
                ApiRequestListItemDto.builder()
                        .id(testId)
                        .method("GET")
                        .path("/api/users")
                        .responseStatus(200)
                        .responseTime(100L)
                        .build()
        );

        when(repository.findLatestRequestPerPath("test-project")).thenReturn(requests);
        when(mapper.toListItemDtoList(requests)).thenReturn(dtos);

        List<ApiRequestListItemDto> result = service.getListView("test-project");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPath()).isEqualTo("/api/users");
    }

    @Test
    void getTableView_shouldReturnPagedResults() {
        ApiRequestFilterDto filter = ApiRequestFilterDto.builder()
                .projectId("test-project")
                .page(0)
                .size(10)
                .sortBy("createdAt")
                .sortDirection("DESC")
                .build();

        Page<ApiRequest> page = new PageImpl<>(Arrays.asList(entity));
        when(repository.findByProjectId(eq("test-project"), any(Pageable.class))).thenReturn(page);
        when(mapper.toResponseDtoList(any())).thenReturn(Arrays.asList(responseDto));

        PagedResponseDto<ApiRequestResponseDto> result = service.getTableView(filter);


        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getPage()).isEqualTo(0);
    }

    @Test
    void searchRequests_shouldReturnFilteredResults() {
        Page<ApiRequest> page = new PageImpl<>(Arrays.asList(entity));
        when(repository.searchByPath(eq("test-project"), eq("users"), any(Pageable.class)))
                .thenReturn(page);
        when(mapper.toResponseDtoList(any())).thenReturn(Arrays.asList(responseDto));

        PagedResponseDto<ApiRequestResponseDto> result = service.searchRequests("test-project", "users", 0, 10);

        assertThat(result.getContent()).hasSize(1);
        verify(repository).searchByPath(eq("test-project"), eq("users"), any(Pageable.class));
    }

    @Test
    void getTotalCount_shouldReturnCount() {
        when(repository.countByProjectId("test-project")).thenReturn(42L);

        long result = service.getTotalCount("test-project");

        assertThat(result).isEqualTo(42L);
    }
}
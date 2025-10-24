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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ApiRequestServiceImpl implements ApiRequestService {

    private final ApiRequestRepository repository;
    private final ApiRequestMapper mapper;
    private final HealthMetricsService healthMetricsService;

    @Override
    @Transactional
    public ApiRequestResponseDto createRequest(CreateApiRequestDto dto) {
        log.debug("Creating API request for project: {}, path: {}", dto.getProjectId(), dto.getPath());

        ApiRequest entity = mapper.toEntity(dto);
        ApiRequest saved = repository.save(entity);

        healthMetricsService.updateMetricsForEndpoint(dto.getProjectId(), dto.getPath());

        return mapper.toResponseDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiRequestResponseDto getRequestById(UUID id) {
        ApiRequest request = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("API Request not found with id: " + id));
        return mapper.toResponseDto(request);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ApiRequestListItemDto> getListView(String projectId) {
        log.debug("Getting list view for project: {}", projectId);
        List<ApiRequest> requests = repository.findLatestRequestPerPath(projectId);
        return mapper.toListItemDtoList(requests);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<ApiRequestResponseDto> getTableView(ApiRequestFilterDto filter) {
        log.debug("Getting table view with filter: {}", filter);

        Pageable pageable = createPageable(filter.getPage(), filter.getSize(),
                filter.getSortBy(), filter.getSortDirection());

        Page<ApiRequest> page = applyFilters(filter, pageable);

        return buildPagedResponse(page, mapper.toResponseDtoList(page.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponseDto<ApiRequestResponseDto> searchRequests(String projectId, String search,
                                                                  int page, int size) {
        log.debug("Searching requests for project: {}, search: {}", projectId, search);

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<ApiRequest> resultPage = repository.searchByPath(projectId, search, pageable);

        return buildPagedResponse(resultPage, mapper.toResponseDtoList(resultPage.getContent()));
    }

    @Override
    @Transactional(readOnly = true)
    public long getTotalCount(String projectId) {
        return repository.countByProjectId(projectId);
    }

    private Pageable createPageable(int page, int size, String sortBy, String sortDirection) {
        Sort.Direction direction = "ASC".equalsIgnoreCase(sortDirection)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(page, size, Sort.by(direction, sortBy));
    }

    private Page<ApiRequest> applyFilters(ApiRequestFilterDto filter, Pageable pageable) {
        String projectId = filter.getProjectId();

        if (filter.getMethod() != null && filter.getStartDate() != null && filter.getEndDate() != null) {
            return repository.findByProjectIdAndMethodAndCreatedAtBetween(
                    projectId, filter.getMethod(), filter.getStartDate(), filter.getEndDate(), pageable);
        }

        if (filter.getMethod() != null) {
            return repository.findByProjectIdAndMethod(projectId, filter.getMethod(), pageable);
        }

        if (filter.getResponseStatus() != null) {
            return repository.findByProjectIdAndResponseStatus(projectId, filter.getResponseStatus(), pageable);
        }

        if (filter.getStartDate() != null && filter.getEndDate() != null) {
            return repository.findByProjectIdAndCreatedAtBetween(
                    projectId, filter.getStartDate(), filter.getEndDate(), pageable);
        }

        if (filter.getSearch() != null && !filter.getSearch().isEmpty()) {
            return repository.searchByPath(projectId, filter.getSearch(), pageable);
        }

        return repository.findByProjectId(projectId, pageable);
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
package com.hackathon.backend.service;

import com.hackathon.backend.dto.request.ApiRequestFilterDto;
import com.hackathon.backend.dto.request.CreateApiRequestDto;
import com.hackathon.backend.dto.response.ApiRequestListItemDto;
import com.hackathon.backend.dto.response.ApiRequestResponseDto;
import com.hackathon.backend.dto.response.PagedResponseDto;

import java.util.List;
import java.util.UUID;

public interface ApiRequestService {

    /**
     * Create a new API request record
     */
    ApiRequestResponseDto createRequest(CreateApiRequestDto dto);

    /**
     * Get API request by ID
     */
    ApiRequestResponseDto getRequestById(UUID id);

    /**
     * Get latest request per unique path (List View)
     */
    List<ApiRequestListItemDto> getListView(String projectId);

    /**
     * Get all requests with pagination and filters (Table View)
     */
    PagedResponseDto<ApiRequestResponseDto> getTableView(ApiRequestFilterDto filter);

    /**
     * Search requests by path
     */
    PagedResponseDto<ApiRequestResponseDto> searchRequests(String projectId, String search, int page, int size);

    /**
     * Get total count for project
     */
    long getTotalCount(String projectId);
}
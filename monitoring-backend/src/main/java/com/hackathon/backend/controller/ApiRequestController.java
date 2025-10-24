package com.hackathon.backend.controller;

import com.hackathon.backend.dto.request.ApiRequestFilterDto;
import com.hackathon.backend.dto.request.CreateApiRequestDto;
import com.hackathon.backend.dto.response.ApiRequestListItemDto;
import com.hackathon.backend.dto.response.ApiRequestResponseDto;
import com.hackathon.backend.dto.response.PagedResponseDto;
import com.hackathon.backend.service.ApiRequestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Tag(name = "API Requests", description = "Endpoints for managing API request data")
public class ApiRequestController {

    private final ApiRequestService service;

    @PostMapping
    @Operation(summary = "Create API request", description = "Capture a new API request from SDK")
    public ResponseEntity<ApiRequestResponseDto> createRequest(
            @Valid @RequestBody CreateApiRequestDto dto) {
        log.info("Received request: {} {} from project: {}", dto.getMethod(), dto.getPath(), dto.getProjectId());
        ApiRequestResponseDto response = service.createRequest(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get request by ID", description = "Retrieve a specific API request by its ID")
    public ResponseEntity<ApiRequestResponseDto> getRequestById(@PathVariable UUID id) {
        ApiRequestResponseDto response = service.getRequestById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/list")
    @Operation(summary = "Get list view", description = "Get latest request per unique path")
    public ResponseEntity<List<ApiRequestListItemDto>> getListView(
            @RequestParam String projectId) {
        List<ApiRequestListItemDto> response = service.getListView(projectId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/table")
    @Operation(summary = "Get table view", description = "Get all requests with pagination and filters")
    public ResponseEntity<PagedResponseDto<ApiRequestResponseDto>> getTableView(
            @RequestParam String projectId,
            @RequestParam(required = false) String method,
            @RequestParam(required = false) Integer responseStatus,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDirection,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        ApiRequestFilterDto filter = ApiRequestFilterDto.builder()
                .projectId(projectId)
                .method(method)
                .responseStatus(responseStatus)
                .startDate(startDate)
                .endDate(endDate)
                .search(search)
                .sortBy(sortBy)
                .sortDirection(sortDirection)
                .page(page)
                .size(size)
                .build();

        PagedResponseDto<ApiRequestResponseDto> response = service.getTableView(filter);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "Search requests", description = "Search requests by path")
    public ResponseEntity<PagedResponseDto<ApiRequestResponseDto>> searchRequests(
            @RequestParam String projectId,
            @RequestParam String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        PagedResponseDto<ApiRequestResponseDto> response = service.searchRequests(projectId, search, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/count")
    @Operation(summary = "Get total count", description = "Get total request count for project")
    public ResponseEntity<Long> getTotalCount(@RequestParam String projectId) {
        long count = service.getTotalCount(projectId);
        return ResponseEntity.ok(count);
    }
}
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
public class ApiRequestFilterDto {

    private String projectId;
    private String method;
    private Integer responseStatus;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String search;
    private String sortBy = "createdAt"; // createdAt or responseTime
    private String sortDirection = "DESC"; // ASC or DESC
    private Integer page = 0;
    private Integer size = 20;
}
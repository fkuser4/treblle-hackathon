package com.hackathon.backend.mapper;

import com.hackathon.backend.dto.response.HealthMetricsListItemDto;
import com.hackathon.backend.dto.response.HealthMetricsResponseDto;
import com.hackathon.backend.entity.EndpointHealthMetrics;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface HealthMetricsMapper {

    HealthMetricsResponseDto toResponseDto(EndpointHealthMetrics entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "endpoint", source = "endpoint")
    @Mapping(target = "avgResponseTime", source = "avgResponseTime")
    @Mapping(target = "totalRequests", source = "totalRequests")
    @Mapping(target = "successRate", source = "successRate")
    @Mapping(target = "healthScore", source = "healthScore")
    @Mapping(target = "lastUpdated", source = "lastUpdated")
    HealthMetricsListItemDto toListItemDto(EndpointHealthMetrics entity);

    List<HealthMetricsResponseDto> toResponseDtoList(List<EndpointHealthMetrics> entities);

    List<HealthMetricsListItemDto> toListItemDtoList(List<EndpointHealthMetrics> entities);
}
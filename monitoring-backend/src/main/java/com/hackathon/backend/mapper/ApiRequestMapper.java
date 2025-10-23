package com.hackathon.backend.mapper;

import com.hackathon.backend.dto.request.CreateApiRequestDto;
import com.hackathon.backend.dto.response.ApiRequestListItemDto;
import com.hackathon.backend.dto.response.ApiRequestResponseDto;
import com.hackathon.backend.entity.ApiRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ApiRequestMapper {

    ApiRequest toEntity(CreateApiRequestDto dto);

    ApiRequestResponseDto toResponseDto(ApiRequest entity);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "method", source = "method")
    @Mapping(target = "path", source = "path")
    @Mapping(target = "responseStatus", source = "responseStatus")
    @Mapping(target = "responseTime", source = "responseTime")
    @Mapping(target = "createdAt", source = "createdAt")
    ApiRequestListItemDto toListItemDto(ApiRequest entity);

    List<ApiRequestResponseDto> toResponseDtoList(List<ApiRequest> entities);

    List<ApiRequestListItemDto> toListItemDtoList(List<ApiRequest> entities);
}
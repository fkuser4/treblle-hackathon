package com.hackathon.backend.repository;

import com.hackathon.backend.entity.ApiRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface ApiRequestRepository extends JpaRepository<ApiRequest, UUID> {

    Page<ApiRequest> findByProjectId(String projectId, Pageable pageable);

    Page<ApiRequest> findByProjectIdAndMethod(String projectId, String method, Pageable pageable);

    Page<ApiRequest> findByProjectIdAndResponseStatus(String projectId, Integer responseStatus, Pageable pageable);

    Page<ApiRequest> findByProjectIdAndCreatedAtBetween(
            String projectId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    Page<ApiRequest> findByProjectIdAndMethodAndCreatedAtBetween(
            String projectId,
            String method,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT a FROM ApiRequest a WHERE a.projectId = :projectId AND a.path LIKE %:search%")
    Page<ApiRequest> searchByPath(
            @Param("projectId") String projectId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query(value = """
        SELECT DISTINCT ON (path) *
        FROM api_requests
        WHERE project_id = :projectId
        ORDER BY path, created_at DESC
        """, nativeQuery = true)
    List<ApiRequest> findLatestRequestPerPath(@Param("projectId") String projectId);

    long countByProjectId(String projectId);

    List<ApiRequest> findByProjectIdAndPath(String projectId, String path);
}
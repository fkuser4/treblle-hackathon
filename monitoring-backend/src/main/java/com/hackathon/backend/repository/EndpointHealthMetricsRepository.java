package com.hackathon.backend.repository;

import com.hackathon.backend.entity.EndpointHealthMetrics;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface EndpointHealthMetricsRepository extends JpaRepository<EndpointHealthMetrics, UUID> {

    Optional<EndpointHealthMetrics> findByProjectIdAndEndpoint(String projectId, String endpoint);

    Page<EndpointHealthMetrics> findByProjectId(String projectId, Pageable pageable);

    Page<EndpointHealthMetrics> findByProjectIdAndHealthScoreLessThan(
            String projectId,
            Integer threshold,
            Pageable pageable
    );

    Page<EndpointHealthMetrics> findByProjectIdAndSuccessRateLessThan(
            String projectId,
            Double threshold,
            Pageable pageable
    );

    Page<EndpointHealthMetrics> findByProjectIdAndLastUpdatedBetween(
            String projectId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            Pageable pageable
    );

    @Query("SELECT e FROM EndpointHealthMetrics e WHERE e.projectId = :projectId AND e.endpoint LIKE %:search%")
    Page<EndpointHealthMetrics> searchByEndpoint(
            @Param("projectId") String projectId,
            @Param("search") String search,
            Pageable pageable
    );

    @Query("""
        SELECT e FROM EndpointHealthMetrics e 
        WHERE e.projectId = :projectId 
        AND e.lastUpdated = (
            SELECT MAX(e2.lastUpdated) 
            FROM EndpointHealthMetrics e2 
            WHERE e2.projectId = e.projectId 
            AND e2.endpoint = e.endpoint
        )
        """)
    List<EndpointHealthMetrics> findLatestMetricsPerEndpoint(@Param("projectId") String projectId);

    long countByProjectId(String projectId);

    List<EndpointHealthMetrics> findByProjectId(String projectId);
}
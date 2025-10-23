package com.hackathon.backend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "endpoint_health_metrics",
        uniqueConstraints = @UniqueConstraint(columnNames = {"project_id", "endpoint"}),
        indexes = {
                @Index(name = "idx_health_project_id", columnList = "project_id"),
                @Index(name = "idx_health_score", columnList = "health_score"),
                @Index(name = "idx_last_updated", columnList = "last_updated")
        }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EndpointHealthMetrics {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false, length = 100)
    private String projectId;

    @Column(name = "endpoint", nullable = false, length = 500)
    private String endpoint;

    @Column(name = "avg_response_time")
    private Double avgResponseTime;

    @Column(name = "min_response_time")
    private Long minResponseTime;

    @Column(name = "max_response_time")
    private Long maxResponseTime;

    @Column(name = "total_requests")
    private Long totalRequests;

    @Column(name = "success_count")
    private Long successCount;

    @Column(name = "error_count")
    private Long errorCount;

    @Column(name = "success_rate")
    private Double successRate;

    @Column(name = "health_score")
    private Integer healthScore;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    @Column(name = "first_seen")
    private LocalDateTime firstSeen;

    @PrePersist
    protected void onCreate() {
        if (lastUpdated == null) {
            lastUpdated = LocalDateTime.now();
        }
        if (firstSeen == null) {
            firstSeen = LocalDateTime.now();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
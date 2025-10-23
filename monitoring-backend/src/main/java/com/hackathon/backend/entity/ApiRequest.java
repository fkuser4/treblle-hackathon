package com.hackathon.backend.entity;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "api_requests", indexes = {
        @Index(name = "idx_project_id", columnList = "project_id"),
        @Index(name = "idx_path", columnList = "path"),
        @Index(name = "idx_method", columnList = "method"),
        @Index(name = "idx_created_at", columnList = "created_at"),
        @Index(name = "idx_response_time", columnList = "response_time")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "project_id", nullable = false, length = 100)
    private String projectId;

    @Column(name = "method", nullable = false, length = 10)
    private String method;

    @Column(name = "path", nullable = false, length = 500)
    private String path;

    @Column(name = "query_string", length = 1000)
    private String queryString;

    @Column(name = "response_status")
    private Integer responseStatus;

    @Column(name = "response_body", columnDefinition = "TEXT")
    private String responseBody;

    @Column(name = "response_time", nullable = false)
    private Long responseTime;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Type(JsonBinaryType.class)
    @Column(name = "request_headers", columnDefinition = "jsonb")
    private Map<String, String> requestHeaders;

    @Column(name = "request_body", columnDefinition = "TEXT")
    private String requestBody;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
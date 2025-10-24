package com.hackathon.backend.repository;

import com.hackathon.backend.entity.ApiRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ApiRequestRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("test")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private ApiRequestRepository repository;

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void findByProjectId_shouldReturnAllRequestsForProject() {
        ApiRequest request1 = createRequest("project-1", "/api/users", "GET");
        ApiRequest request2 = createRequest("project-1", "/api/products", "GET");
        ApiRequest request3 = createRequest("project-2", "/api/users", "POST");

        repository.saveAll(List.of(request1, request2, request3));

        Page<ApiRequest> result = repository.findByProjectId("project-1", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("projectId").containsOnly("project-1");
    }

    @Test
    void findByProjectIdAndMethod_shouldFilterByMethod() {
        ApiRequest request1 = createRequest("project-1", "/api/users", "GET");
        ApiRequest request2 = createRequest("project-1", "/api/users", "POST");
        ApiRequest request3 = createRequest("project-1", "/api/products", "GET");

        repository.saveAll(List.of(request1, request2, request3));

        Page<ApiRequest> result = repository.findByProjectIdAndMethod(
                "project-1", "GET", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting("method").containsOnly("GET");
    }

    @Test
    void searchByPath_shouldFindMatchingPaths() {
        ApiRequest request1 = createRequest("project-1", "/api/users", "GET");
        ApiRequest request2 = createRequest("project-1", "/api/users/123", "GET");
        ApiRequest request3 = createRequest("project-1", "/api/products", "GET");

        repository.saveAll(List.of(request1, request2, request3));

        Page<ApiRequest> result = repository.searchByPath(
                "project-1", "users", PageRequest.of(0, 10));

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).allMatch(r -> r.getPath().contains("users"));
    }

    @Test
    void findLatestRequestPerPath_shouldReturnOnlyLatestPerPath() {
        ApiRequest old = createRequest("project-1", "/api/users", "GET");
        old.setCreatedAt(LocalDateTime.now().minusHours(1));

        ApiRequest newest = createRequest("project-1", "/api/users", "GET");
        newest.setCreatedAt(LocalDateTime.now());

        repository.saveAll(List.of(old, newest));

        List<ApiRequest> result = repository.findLatestRequestPerPath("project-1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(newest.getId());
    }

    @Test
    void countByProjectId_shouldReturnCorrectCount() {
        repository.saveAll(List.of(
                createRequest("project-1", "/api/users", "GET"),
                createRequest("project-1", "/api/products", "GET"),
                createRequest("project-2", "/api/users", "GET")
        ));

        long count = repository.countByProjectId("project-1");

        assertThat(count).isEqualTo(2);
    }

    private ApiRequest createRequest(String projectId, String path, String method) {
        return ApiRequest.builder()
                .projectId(projectId)
                .path(path)
                .method(method)
                .responseStatus(200)
                .responseTime(100L)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
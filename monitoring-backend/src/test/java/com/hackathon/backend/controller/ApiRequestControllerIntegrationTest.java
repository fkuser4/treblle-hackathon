package com.hackathon.backend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hackathon.backend.dto.request.CreateApiRequestDto;
import com.hackathon.backend.repository.ApiRequestRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ApiRequestControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ApiRequestRepository repository;

    private static final String API_KEY = "hackathon-2025-super-secret-key";

    @BeforeEach
    void setUp() {
        repository.deleteAll();
    }

    @Test
    void createRequest_withValidData_shouldReturn201() throws Exception {
        CreateApiRequestDto dto = CreateApiRequestDto.builder()
                .projectId("test-project")
                .method("GET")
                .path("/api/test")
                .responseStatus(200)
                .responseTime(100L)
                .createdAt(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/requests")
                        .header("X-API-Key", API_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.projectId").value("test-project"))
                .andExpect(jsonPath("$.method").value("GET"))
                .andExpect(jsonPath("$.path").value("/api/test"));
    }

    @Test
    void createRequest_withoutApiKey_shouldReturn401() throws Exception {
        CreateApiRequestDto dto = CreateApiRequestDto.builder()
                .projectId("test-project")
                .method("GET")
                .path("/api/test")
                .responseStatus(200)
                .responseTime(100L)
                .createdAt(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/requests")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createRequest_withInvalidApiKey_shouldReturn403() throws Exception {
        CreateApiRequestDto dto = CreateApiRequestDto.builder()
                .projectId("test-project")
                .method("GET")
                .path("/api/test")
                .responseStatus(200)
                .responseTime(100L)
                .createdAt(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/requests")
                        .header("X-API-Key", "invalid-key")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getListView_shouldReturnLatestRequests() throws Exception {
        CreateApiRequestDto dto = CreateApiRequestDto.builder()
                .projectId("test-list")
                .method("GET")
                .path("/api/list-test")
                .responseStatus(200)
                .responseTime(100L)
                .createdAt(LocalDateTime.now())
                .build();

        mockMvc.perform(post("/api/requests")
                .header("X-API-Key", API_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)));

        Thread.sleep(1000);

        mockMvc.perform(get("/api/requests/list")
                        .header("X-API-Key", API_KEY)
                        .param("projectId", "test-list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void getTableView_shouldReturnPagedResults() throws Exception {
        mockMvc.perform(get("/api/requests/table")
                        .header("X-API-Key", API_KEY)
                        .param("projectId", "test-project")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }
}
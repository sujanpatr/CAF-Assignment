package com.assignment.config.controller;

import com.assignment.config.dto.ConfigResponse;
import com.assignment.config.service.ConfigService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for ConfigController using @WebMvcTest.
 * 
 * Following TDD: These tests are written BEFORE the implementation.
 * @WebMvcTest loads only the web layer (controller), not the full application context.
 */
@WebMvcTest(ConfigController.class)
@DisplayName("ConfigController Tests")
class ConfigControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ConfigService configService;

    @Test
    @DisplayName("Should return config section successfully")
    void shouldReturnConfigSectionSuccessfully() throws Exception {
        // Given: Service returns a valid ConfigResponse
        ConfigResponse mockResponse = new ConfigResponse();
        mockResponse.addProperty("broker", "https://orbroker.in");
        List<String> topics = new ArrayList<>();
        topics.add("test_os_topic_1");
        topics.add("test_os_topic_2");
        mockResponse.addProperty("topic", topics);

        when(configService.getSection("Order Service")).thenReturn(mockResponse);

        // When/Then: GET request should return 200 with correct JSON
        mockMvc.perform(get("/config")
                .param("section", "Order Service")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.broker").value("https://orbroker.in"))
                .andExpect(jsonPath("$.topic[0]").value("test_os_topic_1"))
                .andExpect(jsonPath("$.topic[1]").value("test_os_topic_2"));

        verify(configService, times(1)).getSection("Order Service");
    }

    @Test
    @DisplayName("Should return 404 for non-existent section")
    void shouldReturn404ForNonExistentSection() throws Exception {
        // Given: Service returns null for non-existent section
        when(configService.getSection("NonExistentSection")).thenReturn(null);

        // When/Then: GET request should return 404
        mockMvc.perform(get("/config")
                .param("section", "NonExistentSection")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());

        verify(configService, times(1)).getSection("NonExistentSection");
    }

    @Test
    @DisplayName("Should return 400 when section parameter is missing")
    void shouldReturn400WhenSectionParameterIsMissing() throws Exception {
        // When/Then: GET request without section parameter should return 400
        mockMvc.perform(get("/config")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(configService, never()).getSection(anyString());
    }

    @Test
    @DisplayName("Should return single value as string, not array")
    void shouldReturnSingleValueAsStringNotArray() throws Exception {
        // Given: Service returns response with single-value property
        ConfigResponse mockResponse = new ConfigResponse();
        mockResponse.addProperty("broker", "https://orbroker.in");

        when(configService.getSection("Gateway")).thenReturn(mockResponse);

        // When/Then: Single value should be string in JSON
        mockMvc.perform(get("/config")
                .param("section", "Gateway")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.broker").value("https://orbroker.in"))
                .andExpect(jsonPath("$.broker").isString());

        verify(configService, times(1)).getSection("Gateway");
    }

    @Test
    @DisplayName("Should handle section name with spaces")
    void shouldHandleSectionNameWithSpaces() throws Exception {
        // Given: Service returns response for section with spaces
        ConfigResponse mockResponse = new ConfigResponse();
        mockResponse.addProperty("endpoint", "https://xyz.in");

        when(configService.getSection("Order Service")).thenReturn(mockResponse);

        // When/Then: Should handle section name with spaces correctly
        mockMvc.perform(get("/config")
                .param("section", "Order Service")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endpoint").value("https://xyz.in"));

        verify(configService, times(1)).getSection("Order Service");
    }

    @Test
    @DisplayName("Should return empty response for section with no properties")
    void shouldReturnEmptyResponseForSectionWithNoProperties() throws Exception {
        // Given: Service returns empty ConfigResponse
        ConfigResponse mockResponse = new ConfigResponse();
        when(configService.getSection("EmptySection")).thenReturn(mockResponse);

        // When/Then: Should return 200 with empty JSON object
        mockMvc.perform(get("/config")
                .param("section", "EmptySection")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("{}"));

        verify(configService, times(1)).getSection("EmptySection");
    }
}


package com.assignment.price.controller;

import com.assignment.price.dto.PriceResponse;
import com.assignment.price.service.PriceService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for PriceController using @WebMvcTest.
 * 
 * Following TDD: These tests are written BEFORE the implementation.
 */
@WebMvcTest(PriceController.class)
@DisplayName("PriceController Tests")
class PriceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PriceService priceService;

    @Test
    @DisplayName("Should upload TSV file successfully")
    void shouldUploadTsvFileSuccessfully() throws Exception {
        // Given: A TSV file
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "prices.tsv",
                "text/tab-separated-values",
                "SkuID | StartTime | EndTime | Price\nu00006541|10:00|10:15|101".getBytes()
        );

        doNothing().when(priceService).uploadPrices(any(org.springframework.web.multipart.MultipartFile.class));

        // When/Then: POST request should return 200
        mockMvc.perform(multipart("/price/upload")
                .file(file))
                .andExpect(status().isOk());

        verify(priceService, times(1)).uploadPrices(any(org.springframework.web.multipart.MultipartFile.class));
    }

    @Test
    @DisplayName("Should return price for valid SKU and time")
    void shouldReturnPriceForValidSkuAndTime() throws Exception {
        // Given: Service returns a valid price
        PriceResponse mockResponse = new PriceResponse(101.0);
        when(priceService.parseTime("10:03")).thenReturn(LocalTime.of(10, 3));
        when(priceService.getPrice("u00006541", LocalTime.of(10, 3))).thenReturn(mockResponse);

        // When/Then: GET request should return 200 with price
        mockMvc.perform(get("/price")
                .param("skuid", "u00006541")
                .param("time", "10:03")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value(101.0));

        verify(priceService, times(1)).parseTime("10:03");
        verify(priceService, times(1)).getPrice("u00006541", LocalTime.of(10, 3));
    }

    @Test
    @DisplayName("Should return NOT SET when price not found")
    void shouldReturnNotSetWhenPriceNotFound() throws Exception {
        // Given: Service returns NOT SET
        PriceResponse mockResponse = new PriceResponse();
        when(priceService.parseTime("11:00")).thenReturn(LocalTime.of(11, 0));
        when(priceService.getPrice("u00006541", LocalTime.of(11, 0))).thenReturn(mockResponse);

        // When/Then: GET request should return 200 with NOT SET
        mockMvc.perform(get("/price")
                .param("skuid", "u00006541")
                .param("time", "11:00")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value("NOT SET"));

        verify(priceService, times(1)).parseTime("11:00");
        verify(priceService, times(1)).getPrice("u00006541", LocalTime.of(11, 0));
    }

    @Test
    @DisplayName("Should return 400 when SKU parameter is missing")
    void shouldReturn400WhenSkuParameterIsMissing() throws Exception {
        // When/Then: GET request without skuid should return 400
        mockMvc.perform(get("/price")
                .param("time", "10:03")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(priceService, never()).getPrice(anyString(), any());
    }

    @Test
    @DisplayName("Should handle query without time parameter")
    void shouldHandleQueryWithoutTimeParameter() throws Exception {
        // Given: Service returns NOT SET when time is null
        PriceResponse mockResponse = new PriceResponse();
        when(priceService.getPrice("u00006541", null)).thenReturn(mockResponse);

        // When/Then: GET request without time should return NOT SET
        mockMvc.perform(get("/price")
                .param("skuid", "u00006541")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.price").value("NOT SET"));

        verify(priceService, times(1)).getPrice("u00006541", null);
    }

    @Test
    @DisplayName("Should return 400 for invalid time format")
    void shouldReturn400ForInvalidTimeFormat() throws Exception {
        // Given: parseTime returns null for invalid format
        when(priceService.parseTime("25:00")).thenReturn(null);
        
        // When/Then: GET request with invalid time format should return 400
        mockMvc.perform(get("/price")
                .param("skuid", "u00006541")
                .param("time", "25:00")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

        verify(priceService, times(1)).parseTime("25:00");
        verify(priceService, never()).getPrice(anyString(), any());
    }

    @Test
    @DisplayName("Should return 400 when file is missing in upload")
    void shouldReturn400WhenFileIsMissingInUpload() throws Exception {
        // When/Then: POST request without file should return 400
        mockMvc.perform(multipart("/price/upload"))
                .andExpect(status().isBadRequest());

        verify(priceService, never()).uploadPrices(any(org.springframework.web.multipart.MultipartFile.class));
    }
}


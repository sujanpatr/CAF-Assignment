package com.assignment.price.service;

import com.assignment.price.dto.PriceResponse;
import com.assignment.price.model.PriceOffer;
import com.assignment.price.parser.TsvPriceParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PriceService.
 * 
 * Following TDD: These tests are written BEFORE the implementation.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("PriceService Tests")
class PriceServiceTest {

    @Mock
    private TsvPriceParser tsvPriceParser;

    @InjectMocks
    private PriceService priceService;

    private Path tempTsvFile;

    @BeforeEach
    void setUp() throws IOException {
        tempTsvFile = Files.createTempFile("test-prices", ".tsv");
    }

    @Test
    @DisplayName("Should upload TSV file successfully")
    void shouldUploadTsvFileSuccessfully() throws IOException {
        // Given: A valid TSV file
        String tsvContent = """
                SkuID | StartTime | EndTime | Price
                u00006541|10:00|10:15|101
                """;
        Files.writeString(tempTsvFile, tsvContent);

        List<PriceOffer> mockOffers = new ArrayList<>();
        mockOffers.add(new PriceOffer("u00006541", LocalTime.of(10, 0), LocalTime.of(10, 15), 101.0));
        
        when(tsvPriceParser.parse(anyString())).thenReturn(mockOffers);

        // When: Uploading the file
        priceService.uploadPrices(tempTsvFile.toString());

        // Then: Should call parser
        verify(tsvPriceParser, times(1)).parse(tempTsvFile.toString());
    }

    @Test
    @DisplayName("Should find price for valid SKU and time")
    void shouldFindPriceForValidSkuAndTime() throws IOException {
        // Given: Prices are loaded
        List<PriceOffer> offers = new ArrayList<>();
        offers.add(new PriceOffer("u00006541", LocalTime.of(10, 0), LocalTime.of(10, 15), 101.0));
        
        when(tsvPriceParser.parse(anyString())).thenReturn(offers);
        priceService.uploadPrices(tempTsvFile.toString());

        // When: Querying price
        PriceResponse response = priceService.getPrice("u00006541", LocalTime.of(10, 3));

        // Then: Should return correct price
        assertNotNull(response);
        assertTrue(response.isPriceSet());
        assertEquals(101.0, response.getPrice());
    }

    @Test
    @DisplayName("Should return NOT SET when no offer found for time")
    void shouldReturnNotSetWhenNoOfferFoundForTime() throws IOException {
        // Given: Prices are loaded but query time doesn't match
        List<PriceOffer> offers = new ArrayList<>();
        offers.add(new PriceOffer("u00006541", LocalTime.of(10, 0), LocalTime.of(10, 15), 101.0));
        
        when(tsvPriceParser.parse(anyString())).thenReturn(offers);
        priceService.uploadPrices(tempTsvFile.toString());

        // When: Querying price outside offer time
        PriceResponse response = priceService.getPrice("u00006541", LocalTime.of(11, 0));

        // Then: Should return NOT SET
        assertNotNull(response);
        assertFalse(response.isPriceSet());
        assertEquals("NOT SET", response.getPrice());
    }

    @Test
    @DisplayName("Should return NOT SET for non-existent SKU")
    void shouldReturnNotSetForNonExistentSku() throws IOException {
        // Given: Prices are loaded for different SKU
        List<PriceOffer> offers = new ArrayList<>();
        offers.add(new PriceOffer("u00006541", LocalTime.of(10, 0), LocalTime.of(10, 15), 101.0));
        
        when(tsvPriceParser.parse(anyString())).thenReturn(offers);
        priceService.uploadPrices(tempTsvFile.toString());

        // When: Querying non-existent SKU
        PriceResponse response = priceService.getPrice("NONEXISTENT", LocalTime.of(10, 3));

        // Then: Should return NOT SET
        assertNotNull(response);
        assertFalse(response.isPriceSet());
        assertEquals("NOT SET", response.getPrice());
    }

    @Test
    @DisplayName("Should handle overlapping offers - return first match")
    void shouldHandleOverlappingOffers() throws IOException {
        // Given: Multiple overlapping offers for same SKU
        List<PriceOffer> offers = new ArrayList<>();
        offers.add(new PriceOffer("u00006541", LocalTime.of(10, 0), LocalTime.of(10, 15), 101.0));
        offers.add(new PriceOffer("u00006541", LocalTime.of(10, 5), LocalTime.of(10, 10), 99.0));
        
        when(tsvPriceParser.parse(anyString())).thenReturn(offers);
        priceService.uploadPrices(tempTsvFile.toString());

        // When: Querying price at overlapping time
        PriceResponse response = priceService.getPrice("u00006541", LocalTime.of(10, 7));

        // Then: Should return a price (implementation decides which one)
        assertNotNull(response);
        // Note: Implementation should handle this - could return first match or most specific
    }

    @Test
    @DisplayName("Should handle time at exact start boundary")
    void shouldHandleTimeAtExactStartBoundary() throws IOException {
        // Given: Offer with specific time range
        List<PriceOffer> offers = new ArrayList<>();
        offers.add(new PriceOffer("u00006541", LocalTime.of(10, 0), LocalTime.of(10, 15), 101.0));
        
        when(tsvPriceParser.parse(anyString())).thenReturn(offers);
        priceService.uploadPrices(tempTsvFile.toString());

        // When: Querying at exact start time
        PriceResponse response = priceService.getPrice("u00006541", LocalTime.of(10, 0));

        // Then: Should return price (boundary inclusive)
        assertNotNull(response);
        assertTrue(response.isPriceSet());
    }

    @Test
    @DisplayName("Should handle time at exact end boundary")
    void shouldHandleTimeAtExactEndBoundary() throws IOException {
        // Given: Offer with specific time range
        List<PriceOffer> offers = new ArrayList<>();
        offers.add(new PriceOffer("u00006541", LocalTime.of(10, 0), LocalTime.of(10, 15), 101.0));
        
        when(tsvPriceParser.parse(anyString())).thenReturn(offers);
        priceService.uploadPrices(tempTsvFile.toString());

        // When: Querying at exact end time
        PriceResponse response = priceService.getPrice("u00006541", LocalTime.of(10, 15));

        // Then: Should return price (boundary inclusive)
        assertNotNull(response);
        assertTrue(response.isPriceSet());
    }

    @Test
    @DisplayName("Should handle query without time parameter")
    void shouldHandleQueryWithoutTimeParameter() throws IOException {
        // Given: Prices are loaded
        List<PriceOffer> offers = new ArrayList<>();
        offers.add(new PriceOffer("u00006541", LocalTime.of(10, 0), LocalTime.of(10, 15), 101.0));
        
        when(tsvPriceParser.parse(anyString())).thenReturn(offers);
        priceService.uploadPrices(tempTsvFile.toString());

        // When: Querying without time (should use current time or return NOT SET)
        // Implementation decision: without time, return NOT SET
        PriceResponse response = priceService.getPrice("u00006541", null);

        // Then: Should return NOT SET (time is required)
        assertNotNull(response);
        assertFalse(response.isPriceSet());
        assertEquals("NOT SET", response.getPrice());
    }
}


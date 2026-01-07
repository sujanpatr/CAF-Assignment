package com.assignment.price.parser;

import com.assignment.price.model.PriceOffer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TsvPriceParser.
 * 
 * Following TDD: These tests are written BEFORE the implementation.
 */
@DisplayName("TsvPriceParser Tests")
class TsvPriceParserTest {

    private TsvPriceParser parser;
    private Path tempTsvFile;

    @BeforeEach
    void setUp() throws IOException {
        parser = new TsvPriceParser();
        tempTsvFile = Files.createTempFile("test-prices", ".tsv");
    }

    @Test
    @DisplayName("Should parse TSV file with header correctly")
    void shouldParseTsvFileWithHeader() throws IOException {
        // Given: TSV file with header and data
        String tsvContent = """
                SkuID | StartTime | EndTime | Price
                u00006541|10:00|10:15|101
                """;
        Files.writeString(tempTsvFile, tsvContent);

        // When: Parsing the file
        List<PriceOffer> offers = parser.parse(tempTsvFile.toString());

        // Then: Should parse correctly
        assertNotNull(offers);
        assertEquals(1, offers.size());
        
        PriceOffer offer = offers.get(0);
        assertEquals("u00006541", offer.getSkuId());
        assertEquals(LocalTime.of(10, 0), offer.getStartTime());
        assertEquals(LocalTime.of(10, 15), offer.getEndTime());
        assertEquals(101.0, offer.getPrice());
    }

    @Test
    @DisplayName("Should parse multiple offers")
    void shouldParseMultipleOffers() throws IOException {
        // Given: TSV file with multiple offers
        String tsvContent = """
                SkuID | StartTime | EndTime | Price
                u00006541|10:00|10:15|101
                i00006111|10:02|10:05|100
                """;
        Files.writeString(tempTsvFile, tsvContent);

        // When: Parsing the file
        List<PriceOffer> offers = parser.parse(tempTsvFile.toString());

        // Then: Should parse all offers
        assertEquals(2, offers.size());
        assertEquals("u00006541", offers.get(0).getSkuId());
        assertEquals("i00006111", offers.get(1).getSkuId());
    }

    @Test
    @DisplayName("Should parse complete assignment example")
    void shouldParseCompleteAssignmentExample() throws IOException {
        // Given: Complete TSV file from assignment
        String tsvContent = """
                SkuID | StartTime | EndTime | Price
                u00006541|10:00|10:15|101
                i00006111|10:02|10:05|100
                u09099000|10:00|10:08|5000
                t12182868|10:00|20:00|87
                b98989000|00:30|07:00|9128
                u00006541|10:05|10:10|99
                t12182868|14:00|15:00|92
                """;
        Files.writeString(tempTsvFile, tsvContent);

        // When: Parsing the file
        List<PriceOffer> offers = parser.parse(tempTsvFile.toString());

        // Then: Should parse all 7 offers
        assertEquals(7, offers.size());
    }

    @Test
    @DisplayName("Should handle time format HH:mm correctly")
    void shouldHandleTimeFormatCorrectly() throws IOException {
        // Given: TSV with various time formats
        String tsvContent = """
                SkuID | StartTime | EndTime | Price
                u00006541|00:00|23:59|101
                i00006111|09:30|17:45|100
                """;
        Files.writeString(tempTsvFile, tsvContent);

        // When: Parsing the file
        List<PriceOffer> offers = parser.parse(tempTsvFile.toString());

        // Then: Should parse times correctly
        assertEquals(LocalTime.of(0, 0), offers.get(0).getStartTime());
        assertEquals(LocalTime.of(23, 59), offers.get(0).getEndTime());
        assertEquals(LocalTime.of(9, 30), offers.get(1).getStartTime());
        assertEquals(LocalTime.of(17, 45), offers.get(1).getEndTime());
    }

    @Test
    @DisplayName("Should throw exception for invalid time format")
    void shouldThrowExceptionForInvalidTimeFormat() {
        // Given: TSV with invalid time format
        String tsvContent = """
                SkuID | StartTime | EndTime | Price
                u00006541|25:00|10:15|101
                """;
        try {
            Files.writeString(tempTsvFile, tsvContent);
        } catch (IOException e) {
            fail("Failed to create test file");
        }

        // When/Then: Should throw exception
        assertThrows(Exception.class, () -> {
            parser.parse(tempTsvFile.toString());
        });
    }

    @Test
    @DisplayName("Should throw exception for non-existent file")
    void shouldThrowExceptionForNonExistentFile() {
        // Given: Non-existent file path
        String invalidPath = "/non/existent/path/prices.tsv";

        // When/Then: Should throw IOException
        assertThrows(IOException.class, () -> {
            parser.parse(invalidPath);
        });
    }
}


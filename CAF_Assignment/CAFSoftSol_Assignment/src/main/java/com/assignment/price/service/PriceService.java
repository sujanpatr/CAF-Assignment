package com.assignment.price.service;

import com.assignment.price.dto.PriceResponse;
import com.assignment.price.model.PriceOffer;
import com.assignment.price.parser.TsvPriceParser;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

/**
 * Service layer for price management.
 * 
 * Responsibilities:
 * - Upload and parse TSV price files
 * - Store prices in memory optimized for fast time-based lookup
 * - Query prices by SKU and time
 */
@Service
public class PriceService {

    private final TsvPriceParser tsvPriceParser;
    
    // In-memory store: Map<skuId, List<PriceOffer>>
    // Offers are stored per SKU for fast lookup
    private Map<String, List<PriceOffer>> priceStore;
    
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Constructor - uses dependency injection for TsvPriceParser.
     * 
     * @param tsvPriceParser The TSV price parser (injected by Spring)
     */
    public PriceService(TsvPriceParser tsvPriceParser) {
        this.tsvPriceParser = tsvPriceParser;
        this.priceStore = new HashMap<>();
    }

    /**
     * Uploads a TSV file and stores prices in memory.
     * 
     * @param filePath Path to the TSV file
     * @throws IOException if file cannot be read or parsed
     */
    public void uploadPrices(String filePath) throws IOException {
        List<PriceOffer> offers = tsvPriceParser.parse(filePath);
        
        // Group offers by SKU ID for fast lookup
        Map<String, List<PriceOffer>> newStore = new HashMap<>();
        
        for (PriceOffer offer : offers) {
            String skuId = offer.getSkuId();
            newStore.computeIfAbsent(skuId, k -> new ArrayList<>()).add(offer);
        }
        
        // Sort offers by start time for each SKU (for efficient lookup)
        for (List<PriceOffer> skuOffers : newStore.values()) {
            skuOffers.sort(Comparator.comparing(PriceOffer::getStartTime));
        }
        
        // Replace entire store (or merge - depends on requirement)
        this.priceStore = newStore;
    }

    /**
     * Uploads a TSV file from MultipartFile and stores prices in memory.
     * 
     * @param file The uploaded TSV file
     * @throws IOException if file cannot be read or parsed
     */
    public void uploadPrices(MultipartFile file) throws IOException {
        // Save uploaded file temporarily
        Path tempFile = Files.createTempFile("uploaded-prices", ".tsv");
        try {
            file.transferTo(tempFile.toFile());
            uploadPrices(tempFile.toString());
        } finally {
            // Clean up temp file
            Files.deleteIfExists(tempFile);
        }
    }

    /**
     * Gets the price for a SKU at a specific time.
     * 
     * @param skuId The SKU identifier (required)
     * @param time The time to query (optional, can be null)
     * @return PriceResponse with price or "NOT SET"
     */
    public PriceResponse getPrice(String skuId, LocalTime time) {
        // Validate SKU ID
        if (skuId == null || skuId.trim().isEmpty()) {
            return new PriceResponse(); // NOT SET
        }
        
        // If time is not provided, return NOT SET
        if (time == null) {
            return new PriceResponse(); // NOT SET
        }
        
        // Get offers for this SKU
        List<PriceOffer> offers = priceStore.get(skuId);
        if (offers == null || offers.isEmpty()) {
            return new PriceResponse(); // NOT SET
        }
        
        // Find active offer at the given time
        // Since offers are sorted by start time, we can optimize lookup
        for (PriceOffer offer : offers) {
            if (offer.isActiveAt(time)) {
                return new PriceResponse(offer.getPrice());
            }
        }
        
        // No active offer found
        return new PriceResponse(); // NOT SET
    }

    /**
     * Parses a time string in HH:mm format.
     * 
     * @param timeStr Time string in HH:mm format
     * @return LocalTime object, or null if invalid
     */
    public LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        
        try {
            return LocalTime.parse(timeStr.trim(), TIME_FORMATTER);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * Checks if a SKU exists in the store.
     * 
     * @param skuId The SKU identifier
     * @return true if SKU exists, false otherwise
     */
    public boolean hasSku(String skuId) {
        return priceStore.containsKey(skuId);
    }
}


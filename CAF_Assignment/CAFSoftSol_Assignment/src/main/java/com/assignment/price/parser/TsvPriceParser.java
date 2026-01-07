package com.assignment.price.parser;

import com.assignment.price.model.PriceOffer;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parser for TSV (Tab-Separated Values) price files.
 * 
 * Expected format:
 * SkuID | StartTime | EndTime | Price
 * u00006541|10:00|10:15|101
 * 
 * Columns are pipe-separated (|).
 * Time format: HH:mm (24-hour format)
 */
@Component
public class TsvPriceParser {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * Parses a TSV file and returns a list of PriceOffer objects.
     * 
     * @param filePath The path to the TSV file
     * @return List of PriceOffer objects
     * @throws IOException if file cannot be read
     * @throws IllegalArgumentException if file format is invalid
     */
    public List<PriceOffer> parse(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        List<PriceOffer> offers = new ArrayList<>();
        
        // Skip header line (first line)
        boolean isFirstLine = true;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Skip empty lines
            if (trimmedLine.isEmpty()) {
                continue;
            }
            
            // Skip header line
            if (isFirstLine) {
                isFirstLine = false;
                continue;
            }
            
            // Parse data line
            String[] parts = trimmedLine.split("\\|");
            if (parts.length != 4) {
                throw new IllegalArgumentException(
                    "Invalid TSV format. Expected 4 columns separated by |, got: " + parts.length
                );
            }
            
            String skuId = parts[0].trim();
            String startTimeStr = parts[1].trim();
            String endTimeStr = parts[2].trim();
            String priceStr = parts[3].trim();
            
            // Parse time
            LocalTime startTime;
            LocalTime endTime;
            try {
                startTime = LocalTime.parse(startTimeStr, TIME_FORMATTER);
                endTime = LocalTime.parse(endTimeStr, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                throw new IllegalArgumentException(
                    "Invalid time format. Expected HH:mm, got: " + startTimeStr + " or " + endTimeStr, e
                );
            }
            
            // Parse price
            double price;
            try {
                price = Double.parseDouble(priceStr);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(
                    "Invalid price format. Expected number, got: " + priceStr, e
                );
            }
            
            // Create and add offer
            PriceOffer offer = new PriceOffer(skuId, startTime, endTime, price);
            offers.add(offer);
        }
        
        return offers;
    }
}


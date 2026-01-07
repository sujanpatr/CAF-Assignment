package com.assignment.price.controller;

import com.assignment.price.dto.PriceResponse;
import com.assignment.price.service.PriceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalTime;

/**
 * REST Controller for Price API.
 * 
 * Provides endpoints to:
 * - Upload TSV price files
 * - Query prices by SKU and time
 */
@RestController
@RequestMapping("/price")
public class PriceController {

    private final PriceService priceService;

    /**
     * Constructor - injects PriceService.
     * 
     * @param priceService The price service
     */
    public PriceController(PriceService priceService) {
        this.priceService = priceService;
    }

    /**
     * Uploads a TSV file containing price offers.
     * 
     * @param file The TSV file to upload (multipart/form-data)
     * @return ResponseEntity with success status
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadPrices(
            @RequestParam("file") MultipartFile file) {
        
        // Validate file
        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is required");
        }
        
        // Validate file type (optional - can check content type or extension)
        String filename = file.getOriginalFilename();
        if (filename != null && !filename.endsWith(".tsv") && !filename.endsWith(".txt")) {
            return ResponseEntity.badRequest().body("Invalid file type. Expected .tsv file");
        }
        
        try {
            priceService.uploadPrices(file);
            return ResponseEntity.ok("File uploaded successfully");
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error uploading file: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("Invalid file format: " + e.getMessage());
        }
    }

    /**
     * Gets the price for a SKU at a specific time.
     * 
     * @param skuid The SKU identifier (required)
     * @param time The time in HH:mm format (optional)
     * @return ResponseEntity with PriceResponse
     */
    @GetMapping
    public ResponseEntity<PriceResponse> getPrice(
            @RequestParam(value = "skuid", required = true) String skuid,
            @RequestParam(value = "time", required = false) String time) {
        
        // Validate SKU ID
        if (skuid == null || skuid.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Parse time if provided
        LocalTime localTime = null;
        if (time != null && !time.trim().isEmpty()) {
            localTime = priceService.parseTime(time);
            if (localTime == null) {
                // Invalid time format
                return ResponseEntity.badRequest().build();
            }
        }
        
        // Get price from service
        PriceResponse response = priceService.getPrice(skuid, localTime);
        
        return ResponseEntity.ok(response);
    }
}


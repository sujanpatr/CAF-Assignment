package com.assignment.config.controller;

import com.assignment.config.dto.ConfigResponse;
import com.assignment.config.service.ConfigService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for Configuration API.
 * 
 * Provides endpoints to retrieve configuration sections.
 * 
 * Endpoint: GET /config?section={sectionName}
 */
@RestController
@RequestMapping("/config")
public class ConfigController {

    private final ConfigService configService;

    /**
     * Constructor - injects ConfigService.
     * 
     * @param configService The configuration service
     */
    public ConfigController(ConfigService configService) {
        this.configService = configService;
    }

    /**
     * Loads a configuration file.
     * 
     * @param filePath The path to the config file
     * @return ResponseEntity with success status
     */
    @PostMapping("/load")
    public ResponseEntity<String> loadConfig(
            @RequestParam(value = "filePath", required = true) String filePath) {
        
        // Validate input
        if (filePath == null || filePath.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("File path is required");
        }
        
        try {
            configService.loadConfig(filePath);
            return ResponseEntity.ok("Config file loaded successfully");
        } catch (java.io.IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error loading config file: " + e.getMessage());
        }
    }

    /**
     * Retrieves a configuration section by name.
     * 
     * @param section The section name (required query parameter)
     * @return ResponseEntity with ConfigResponse or 404 if not found
     */
    @GetMapping
    public ResponseEntity<ConfigResponse> getConfig(
            @RequestParam(value = "section", required = true) String section) {
        
        // Validate input
        if (section == null || section.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        // Retrieve section from service
        ConfigResponse response = configService.getSection(section);
        
        if (response == null) {
            // Section not found
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        // Return successful response
        return ResponseEntity.ok(response);
    }
}


package com.assignment.config.service;

import com.assignment.config.dto.ConfigResponse;
import com.assignment.config.model.ConfigSection;
import com.assignment.config.parser.ConfigParser;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * Service layer for configuration management.
 * 
 * Responsibilities:
 * - Load configuration files
 * - Store configurations in memory for fast retrieval
 * - Convert internal models to DTOs for API responses
 */
@Service
public class ConfigService {

    private final ConfigParser configParser;
    
    // In-memory store: Map<sectionName, ConfigSection>
    private Map<String, ConfigSection> configStore;

    /**
     * Constructor - uses dependency injection for ConfigParser.
     * 
     * @param configParser The config parser (injected by Spring)
     */
    public ConfigService(ConfigParser configParser) {
        this.configParser = configParser;
        this.configStore = new java.util.HashMap<>();
    }

    /**
     * Loads a configuration file and stores it in memory.
     * 
     * @param filePath Path to the configuration file
     * @throws IOException if file cannot be read
     */
    public void loadConfig(String filePath) throws IOException {
        Map<String, ConfigSection> parsedSections = configParser.parse(filePath);
        // Merge with existing store (or replace - depends on requirement)
        // For this assignment, we'll replace the entire store
        this.configStore = parsedSections;
    }

    /**
     * Retrieves a configuration section by name.
     * 
     * @param sectionName The name of the section to retrieve
     * @return ConfigResponse DTO, or null if section doesn't exist
     */
    public ConfigResponse getSection(String sectionName) {
        if (sectionName == null || sectionName.trim().isEmpty()) {
            return null;
        }
        
        ConfigSection section = configStore.get(sectionName);
        if (section == null) {
            return null;
        }
        
        // Convert ConfigSection to ConfigResponse DTO
        ConfigResponse response = new ConfigResponse();
        Map<String, List<String>> properties = section.getProperties();
        
        for (Map.Entry<String, List<String>> entry : properties.entrySet()) {
            String key = entry.getKey();
            List<String> values = entry.getValue();
            
            if (values == null || values.isEmpty()) {
                // Empty value - add as empty string
                response.addProperty(key, "");
            } else if (values.size() == 1) {
                // Single value - add as string
                response.addProperty(key, values.get(0));
            } else {
                // Multiple values - add as array
                response.addProperty(key, values);
            }
        }
        
        return response;
    }

    /**
     * Checks if a section exists.
     * 
     * @param sectionName The name of the section
     * @return true if section exists, false otherwise
     */
    public boolean hasSection(String sectionName) {
        return configStore.containsKey(sectionName);
    }
}


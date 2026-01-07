package com.assignment.config.parser;

import com.assignment.config.model.ConfigSection;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

/**
 * Parser for configuration files.
 * 
 * Parses config files with the following format:
 * - Section names are on their own line
 * - Properties are key-value pairs: "key = value"
 * - Values can be single strings or comma-separated lists
 * - Empty lines separate sections
 * 
 * Example:
 * Gateway
 * endpoint = https://xyz.in
 * topic = test_topic_1, test_topic_2
 */
@Component
public class ConfigParser {

    /**
     * Parses a configuration file and returns a map of sections.
     * 
     * @param filePath The path to the config file
     * @return Map where key is section name and value is ConfigSection
     * @throws IOException if file cannot be read
     */
    public Map<String, ConfigSection> parse(String filePath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        Map<String, ConfigSection> sections = new HashMap<>();
        
        ConfigSection currentSection = null;
        
        for (String line : lines) {
            String trimmedLine = line.trim();
            
            // Skip empty lines
            if (trimmedLine.isEmpty()) {
                continue;
            }
            
            // Check if this is a section name (no '=' sign)
            if (!trimmedLine.contains("=")) {
                // This is a section name
                String sectionName = trimmedLine;
                currentSection = new ConfigSection(sectionName);
                sections.put(sectionName, currentSection);
            } else {
                // This is a property line
                if (currentSection == null) {
                    // Skip properties without a section
                    continue;
                }
                
                // Parse key-value pair
                String[] parts = trimmedLine.split("=", 2);
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();
                    
                    // Check if value contains comma (multiple values)
                    if (value.contains(",")) {
                        // Split by comma and trim each value
                        List<String> values = new ArrayList<>();
                        String[] valueArray = value.split(",");
                        for (String v : valueArray) {
                            String trimmedValue = v.trim();
                            if (!trimmedValue.isEmpty()) {
                                values.add(trimmedValue);
                            }
                        }
                        currentSection.addProperty(key, values);
                    } else {
                        // Single value
                        currentSection.addProperty(key, value);
                    }
                }
            }
        }
        
        return sections;
    }
}


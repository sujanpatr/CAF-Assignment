package com.assignment.config.dto;

import com.fasterxml.jackson.annotation.JsonAnyGetter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DTO (Data Transfer Object) for Config API response.
 * 
 * This class represents the JSON response structure:
 * {
 *   "broker": "https://orbroker.in",
 *   "topic": ["test_os_topic_1", "test_os_topic_2"]
 * }
 * 
 * Single values are returned as strings.
 * Multiple values (comma-separated) are returned as arrays.
 */
public class ConfigResponse {
    
    private final Map<String, Object> properties;
    
    /**
     * Constructor - creates an empty ConfigResponse.
     */
    public ConfigResponse() {
        this.properties = new HashMap<>();
    }
    
    /**
     * Adds a property with a single value (string).
     * 
     * @param key The property key
     * @param value The property value
     */
    public void addProperty(String key, String value) {
        if (key != null && value != null) {
            properties.put(key, value.trim());
        }
    }
    
    /**
     * Adds a property with multiple values (array).
     * 
     * @param key The property key
     * @param values List of values
     */
    public void addProperty(String key, List<String> values) {
        if (key != null && values != null && !values.isEmpty()) {
            // If single value, store as string; otherwise as array
            if (values.size() == 1) {
                properties.put(key, values.get(0).trim());
            } else {
                List<String> trimmedValues = new ArrayList<>();
                for (String value : values) {
                    trimmedValues.add(value.trim());
                }
                properties.put(key, trimmedValues);
            }
        }
    }
    
    /**
     * Gets all properties as a Map.
     * This is used by Jackson to serialize to JSON.
     * @JsonAnyGetter ensures the map contents are serialized as root-level properties.
     * 
     * @return Map of properties
     */
    @JsonAnyGetter
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    /**
     * Checks if the response is empty.
     * 
     * @return true if no properties exist, false otherwise
     */
    public boolean isEmpty() {
        return properties.isEmpty();
    }
    
    @Override
    public String toString() {
        return "ConfigResponse{" +
                "properties=" + properties +
                '}';
    }
}


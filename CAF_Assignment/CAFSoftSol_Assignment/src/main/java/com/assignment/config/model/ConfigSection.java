package com.assignment.config.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Internal model representing a configuration section.
 * 
 * A section contains multiple properties (key-value pairs).
 * Values can be single strings or comma-separated lists.
 * 
 * Example:
 * Section: "Order Service"
 * Properties:
 *   - broker: "https://orbroker.in"
 *   - topic: ["test_os_topic_1", "test_os_topic_2"]
 */
public class ConfigSection {
    
    private final String sectionName;
    private final Map<String, List<String>> properties;
    
    /**
     * Constructor to create a new ConfigSection.
     * 
     * @param sectionName The name of the section (e.g., "Order Service")
     */
    public ConfigSection(String sectionName) {
        this.sectionName = sectionName;
        this.properties = new HashMap<>();
    }
    
    /**
     * Adds a property to this section.
     * If the property already exists, it will be replaced.
     * 
     * @param key The property key (e.g., "broker")
     * @param values List of values for this property
     */
    public void addProperty(String key, List<String> values) {
        if (key != null && values != null) {
            properties.put(key.trim(), new ArrayList<>(values));
        }
    }
    
    /**
     * Adds a single-value property to this section.
     * 
     * @param key The property key
     * @param value The property value
     */
    public void addProperty(String key, String value) {
        if (key != null && value != null) {
            List<String> values = new ArrayList<>();
            values.add(value.trim());
            properties.put(key.trim(), values);
        }
    }
    
    /**
     * Gets all properties for this section.
     * 
     * @return Map of property keys to their values (as lists)
     */
    public Map<String, List<String>> getProperties() {
        return new HashMap<>(properties);
    }
    
    /**
     * Gets the section name.
     * 
     * @return The section name
     */
    public String getSectionName() {
        return sectionName;
    }
    
    /**
     * Gets a specific property value.
     * Returns null if the property doesn't exist.
     * 
     * @param key The property key
     * @return List of values for the property, or null if not found
     */
    public List<String> getProperty(String key) {
        return properties.get(key);
    }
    
    /**
     * Checks if a property exists in this section.
     * 
     * @param key The property key
     * @return true if the property exists, false otherwise
     */
    public boolean hasProperty(String key) {
        return properties.containsKey(key);
    }
    
    @Override
    public String toString() {
        return "ConfigSection{" +
                "sectionName='" + sectionName + '\'' +
                ", properties=" + properties +
                '}';
    }
}


package com.assignment.config.parser;

import com.assignment.config.model.ConfigSection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ConfigParser.
 * 
 * Following TDD: These tests are written BEFORE the implementation.
 * They will fail initially, then we'll implement ConfigParser to make them pass.
 */
@DisplayName("ConfigParser Tests")
class ConfigParserTest {

    private ConfigParser configParser;
    private Path tempConfigFile;

    @BeforeEach
    void setUp() throws IOException {
        configParser = new ConfigParser();
        
        // Create a temporary config file for testing
        tempConfigFile = Files.createTempFile("test-config", ".txt");
    }

    @Test
    @DisplayName("Should parse config file with single section and single values")
    void shouldParseSingleSectionWithSingleValues() throws IOException {
        // Given: A config file with one section and single-value properties
        String configContent = """
                Gateway
                endpoint = https://xyz.in
                certurl = https://cloud.internalportal.com
                """;
        Files.writeString(tempConfigFile, configContent);

        // When: Parsing the file
        Map<String, ConfigSection> result = configParser.parse(tempConfigFile.toString());

        // Then: Should parse correctly
        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.containsKey("Gateway"));
        
        ConfigSection gateway = result.get("Gateway");
        assertNotNull(gateway);
        assertEquals("Gateway", gateway.getSectionName());
        assertEquals("https://xyz.in", gateway.getProperty("endpoint").get(0));
        assertEquals("https://cloud.internalportal.com", gateway.getProperty("certurl").get(0));
    }

    @Test
    @DisplayName("Should parse config file with multiple sections")
    void shouldParseMultipleSections() throws IOException {
        // Given: A config file with multiple sections
        String configContent = """
                Gateway
                endpoint = https://xyz.in
                
                CXO
                endpont = http://internal.cxo.com
                """;
        Files.writeString(tempConfigFile, configContent);

        // When: Parsing the file
        Map<String, ConfigSection> result = configParser.parse(tempConfigFile.toString());

        // Then: Should parse both sections
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.containsKey("Gateway"));
        assertTrue(result.containsKey("CXO"));
    }

    @Test
    @DisplayName("Should parse comma-separated list values")
    void shouldParseCommaSeparatedValues() throws IOException {
        // Given: A config file with comma-separated values
        String configContent = """
                Order Service
                broker = https://orbroker.in
                topic = test_os_topic_1, test_os_topic_2
                """;
        Files.writeString(tempConfigFile, configContent);

        // When: Parsing the file
        Map<String, ConfigSection> result = configParser.parse(tempConfigFile.toString());

        // Then: Should parse list correctly
        ConfigSection orderService = result.get("Order Service");
        assertNotNull(orderService);
        
        List<String> topics = orderService.getProperty("topic");
        assertNotNull(topics);
        assertEquals(2, topics.size());
        assertEquals("test_os_topic_1", topics.get(0));
        assertEquals("test_os_topic_2", topics.get(1));
    }

    @Test
    @DisplayName("Should handle empty property values")
    void shouldHandleEmptyPropertyValues() throws IOException {
        // Given: A config file with empty property value
        String configContent = """
                CXO
                redirect url =
                broker = http://cxobroker.in
                """;
        Files.writeString(tempConfigFile, configContent);

        // When: Parsing the file
        Map<String, ConfigSection> result = configParser.parse(tempConfigFile.toString());

        // Then: Should handle empty value
        ConfigSection cxo = result.get("CXO");
        assertNotNull(cxo);
        assertTrue(cxo.hasProperty("redirect url"));
        List<String> redirectUrl = cxo.getProperty("redirect url");
        assertNotNull(redirectUrl);
        assertTrue(redirectUrl.isEmpty() || redirectUrl.get(0).isEmpty());
    }

    @Test
    @DisplayName("Should handle properties with spaces in keys")
    void shouldHandlePropertiesWithSpacesInKeys() throws IOException {
        // Given: A config file with spaces in property keys
        String configContent = """
                Gateway
                download loc = /home/user/temp
                """;
        Files.writeString(tempConfigFile, configContent);

        // When: Parsing the file
        Map<String, ConfigSection> result = configParser.parse(tempConfigFile.toString());

        // Then: Should preserve spaces in keys
        ConfigSection gateway = result.get("Gateway");
        assertNotNull(gateway);
        assertTrue(gateway.hasProperty("download loc"));
        assertEquals("/home/user/temp", gateway.getProperty("download loc").get(0));
    }

    @Test
    @DisplayName("Should throw exception for non-existent file")
    void shouldThrowExceptionForNonExistentFile() {
        // Given: A non-existent file path
        String invalidPath = "/non/existent/path/config.txt";

        // When/Then: Should throw IOException
        assertThrows(IOException.class, () -> {
            configParser.parse(invalidPath);
        });
    }

    @Test
    @DisplayName("Should handle empty file")
    void shouldHandleEmptyFile() throws IOException {
        // Given: An empty config file
        Files.writeString(tempConfigFile, "");

        // When: Parsing the file
        Map<String, ConfigSection> result = configParser.parse(tempConfigFile.toString());

        // Then: Should return empty map
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Should parse complete example from assignment")
    void shouldParseCompleteExample() throws IOException {
        // Given: Complete config file from assignment example
        String configContent = """
                Gateway
                endpoint = https://xyz.in
                certurl = https://cloud.internalportal.com
                download loc = /home/user/temp

                CXO
                endpont = http://internal.cxo.com
                redirect url =
                broker = http://cxobroker.in
                topic = test_cxo_topic, test_cxo_topic_1

                Order Service
                broker = https://orbroker.in
                topic = test_os_topic_1, test_os_topic_2
                """;
        Files.writeString(tempConfigFile, configContent);

        // When: Parsing the file
        Map<String, ConfigSection> result = configParser.parse(tempConfigFile.toString());

        // Then: Should parse all sections correctly
        assertEquals(3, result.size());
        
        // Verify Gateway section
        ConfigSection gateway = result.get("Gateway");
        assertEquals("https://xyz.in", gateway.getProperty("endpoint").get(0));
        assertEquals("/home/user/temp", gateway.getProperty("download loc").get(0));
        
        // Verify CXO section
        ConfigSection cxo = result.get("CXO");
        List<String> cxoTopics = cxo.getProperty("topic");
        assertEquals(2, cxoTopics.size());
        assertEquals("test_cxo_topic", cxoTopics.get(0));
        
        // Verify Order Service section
        ConfigSection orderService = result.get("Order Service");
        assertEquals("https://orbroker.in", orderService.getProperty("broker").get(0));
        List<String> osTopics = orderService.getProperty("topic");
        assertEquals(2, osTopics.size());
        assertEquals("test_os_topic_1", osTopics.get(0));
        assertEquals("test_os_topic_2", osTopics.get(1));
    }
}


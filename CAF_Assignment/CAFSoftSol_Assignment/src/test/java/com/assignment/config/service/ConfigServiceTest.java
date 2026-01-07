package com.assignment.config.service;

import com.assignment.config.dto.ConfigResponse;
import com.assignment.config.parser.ConfigParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ConfigService.
 * 
 * Following TDD: These tests are written BEFORE the implementation.
 * We use Mockito to mock the ConfigParser dependency.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ConfigService Tests")
class ConfigServiceTest {

    @Mock
    private ConfigParser configParser;

    @InjectMocks
    private ConfigService configService;

    private Path tempConfigFile;

    @BeforeEach
    void setUp() throws IOException {
        tempConfigFile = Files.createTempFile("test-config", ".txt");
    }

    @Test
    @DisplayName("Should load config file successfully")
    void shouldLoadConfigFileSuccessfully() throws IOException {
        // Given: A valid config file
        String configContent = """
                Order Service
                broker = https://orbroker.in
                topic = test_os_topic_1, test_os_topic_2
                """;
        Files.writeString(tempConfigFile, configContent);

        // When: Loading the config
        configService.loadConfig(tempConfigFile.toString());

        // Then: Should call parser
        verify(configParser, times(1)).parse(tempConfigFile.toString());
    }

    @Test
    @DisplayName("Should throw exception when file does not exist")
    void shouldThrowExceptionWhenFileDoesNotExist() {
        // Given: A non-existent file path
        String invalidPath = "/non/existent/path/config.txt";
        
        // Mock parser to throw IOException
        try {
            doThrow(new IOException("File not found")).when(configParser).parse(invalidPath);
        } catch (IOException e) {
            // This won't happen in mock
        }

        // When/Then: Should propagate the exception
        assertThrows(IOException.class, () -> {
            configService.loadConfig(invalidPath);
        });
    }

    @Test
    @DisplayName("Should retrieve section by name successfully")
    void shouldRetrieveSectionByNameSuccessfully() throws IOException {
        // Given: Config is loaded with Order Service section
        String configContent = """
                Order Service
                broker = https://orbroker.in
                topic = test_os_topic_1, test_os_topic_2
                """;
        Files.writeString(tempConfigFile, configContent);
        
        // Mock parser to return a map with Order Service
        com.assignment.config.model.ConfigSection orderService = 
            new com.assignment.config.model.ConfigSection("Order Service");
        orderService.addProperty("broker", "https://orbroker.in");
        orderService.addProperty("topic", java.util.Arrays.asList("test_os_topic_1", "test_os_topic_2"));
        
        when(configParser.parse(anyString())).thenReturn(
            Map.of("Order Service", orderService)
        );
        
        configService.loadConfig(tempConfigFile.toString());

        // When: Retrieving the section
        ConfigResponse response = configService.getSection("Order Service");

        // Then: Should return correct response
        assertNotNull(response);
        assertFalse(response.isEmpty());
        Map<String, Object> properties = response.getProperties();
        assertEquals("https://orbroker.in", properties.get("broker"));
        assertTrue(properties.get("topic") instanceof java.util.List);
    }

    @Test
    @DisplayName("Should return null for non-existent section")
    void shouldReturnNullForNonExistentSection() throws IOException {
        // Given: Config is loaded but section doesn't exist
        String configContent = """
                Gateway
                endpoint = https://xyz.in
                """;
        Files.writeString(tempConfigFile, configContent);
        
        com.assignment.config.model.ConfigSection gateway = 
            new com.assignment.config.model.ConfigSection("Gateway");
        gateway.addProperty("endpoint", "https://xyz.in");
        
        when(configParser.parse(anyString())).thenReturn(
            Map.of("Gateway", gateway)
        );
        
        configService.loadConfig(tempConfigFile.toString());

        // When: Retrieving non-existent section
        ConfigResponse response = configService.getSection("NonExistentSection");

        // Then: Should return null
        assertNull(response);
    }

    @Test
    @DisplayName("Should convert single value to string in response")
    void shouldConvertSingleValueToStringInResponse() throws IOException {
        // Given: Config with single-value property
        String configContent = """
                Gateway
                endpoint = https://xyz.in
                """;
        Files.writeString(tempConfigFile, configContent);
        
        com.assignment.config.model.ConfigSection gateway = 
            new com.assignment.config.model.ConfigSection("Gateway");
        gateway.addProperty("endpoint", "https://xyz.in");
        
        when(configParser.parse(anyString())).thenReturn(
            Map.of("Gateway", gateway)
        );
        
        configService.loadConfig(tempConfigFile.toString());

        // When: Retrieving the section
        ConfigResponse response = configService.getSection("Gateway");

        // Then: Single value should be string, not array
        assertNotNull(response);
        Map<String, Object> properties = response.getProperties();
        assertEquals("https://xyz.in", properties.get("endpoint"));
        assertFalse(properties.get("endpoint") instanceof java.util.List);
    }

    @Test
    @DisplayName("Should convert multiple values to array in response")
    void shouldConvertMultipleValuesToArrayInResponse() throws IOException {
        // Given: Config with multi-value property
        String configContent = """
                Order Service
                topic = test_os_topic_1, test_os_topic_2
                """;
        Files.writeString(tempConfigFile, configContent);
        
        com.assignment.config.model.ConfigSection orderService = 
            new com.assignment.config.model.ConfigSection("Order Service");
        orderService.addProperty("topic", java.util.Arrays.asList("test_os_topic_1", "test_os_topic_2"));
        
        when(configParser.parse(anyString())).thenReturn(
            Map.of("Order Service", orderService)
        );
        
        configService.loadConfig(tempConfigFile.toString());

        // When: Retrieving the section
        ConfigResponse response = configService.getSection("Order Service");

        // Then: Multiple values should be array
        assertNotNull(response);
        Map<String, Object> properties = response.getProperties();
        assertTrue(properties.get("topic") instanceof java.util.List);
        @SuppressWarnings("unchecked")
        java.util.List<String> topics = (java.util.List<String>) properties.get("topic");
        assertEquals(2, topics.size());
        assertEquals("test_os_topic_1", topics.get(0));
        assertEquals("test_os_topic_2", topics.get(1));
    }

    @Test
    @DisplayName("Should handle empty property values")
    void shouldHandleEmptyPropertyValues() throws IOException {
        // Given: Config with empty property value
        String configContent = """
                CXO
                redirect url =
                """;
        Files.writeString(tempConfigFile, configContent);
        
        com.assignment.config.model.ConfigSection cxo = 
            new com.assignment.config.model.ConfigSection("CXO");
        cxo.addProperty("redirect url", "");
        
        when(configParser.parse(anyString())).thenReturn(
            Map.of("CXO", cxo)
        );
        
        configService.loadConfig(tempConfigFile.toString());

        // When: Retrieving the section
        ConfigResponse response = configService.getSection("CXO");

        // Then: Should handle empty value
        assertNotNull(response);
        Map<String, Object> properties = response.getProperties();
        assertTrue(properties.containsKey("redirect url"));
    }
}


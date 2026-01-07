package com.assignment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main Spring Boot Application
 * 
 * This application provides two main features:
 * 1. Config File Parser - Parse and retrieve configuration by section
 * 2. TSV Price Engine - Upload TSV files and query prices by SKU and time
 */
@SpringBootApplication
public class ConfigPriceEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigPriceEngineApplication.class, args);
    }
}


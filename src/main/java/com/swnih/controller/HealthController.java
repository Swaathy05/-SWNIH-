package com.swnih.controller;

import com.swnih.config.DatabaseConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller for monitoring system status.
 * Provides endpoints to check application and database health.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private DatabaseConfig databaseConfig;

    /**
     * Basic health check endpoint.
     * 
     * @return health status response
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> health = new HashMap<>();
        health.put("status", "UP");
        health.put("timestamp", LocalDateTime.now());
        health.put("application", "Smart Web Notification Intelligence Hub");
        health.put("version", "1.0.0");
        
        return ResponseEntity.ok(health);
    }

    /**
     * Database health check endpoint.
     * Tests database connectivity and connection pool status.
     * 
     * @return database health status
     */
    @GetMapping("/database")
    public ResponseEntity<Map<String, Object>> databaseHealth() {
        Map<String, Object> dbHealth = new HashMap<>();
        
        try {
            // Test database connection
            try (Connection connection = dataSource.getConnection()) {
                boolean isValid = connection.isValid(5); // 5 second timeout
                
                dbHealth.put("status", isValid ? "UP" : "DOWN");
                dbHealth.put("database", "MySQL");
                dbHealth.put("connectionValid", isValid);
                dbHealth.put("connectionPoolInfo", databaseConfig.getConnectionPoolInfo());
                dbHealth.put("timestamp", LocalDateTime.now());
                
                if (isValid) {
                    return ResponseEntity.ok(dbHealth);
                } else {
                    return ResponseEntity.status(503).body(dbHealth);
                }
            }
        } catch (SQLException e) {
            dbHealth.put("status", "DOWN");
            dbHealth.put("error", e.getMessage());
            dbHealth.put("timestamp", LocalDateTime.now());
            
            return ResponseEntity.status(503).body(dbHealth);
        }
    }

    /**
     * Detailed system information endpoint.
     * 
     * @return detailed system status
     */
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> systemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // Application info
        info.put("application", "Smart Web Notification Intelligence Hub");
        info.put("version", "1.0.0");
        info.put("description", "Unified priority-based notification management system");
        
        // System info
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("javaVersion", System.getProperty("java.version"));
        systemInfo.put("javaVendor", System.getProperty("java.vendor"));
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));
        systemInfo.put("availableProcessors", runtime.availableProcessors());
        systemInfo.put("maxMemory", runtime.maxMemory());
        systemInfo.put("totalMemory", runtime.totalMemory());
        systemInfo.put("freeMemory", runtime.freeMemory());
        
        info.put("system", systemInfo);
        info.put("timestamp", LocalDateTime.now());
        
        return ResponseEntity.ok(info);
    }
}
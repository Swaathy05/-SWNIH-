package com.swnih.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;

/**
 * Database configuration class for setting up connection pooling.
 * Configures HikariCP with connection pool settings as per requirements (5-20 connections).
 */
@Configuration
public class DatabaseConfig {

    @Value("${spring.datasource.url}")
    private String jdbcUrl;

    @Value("${spring.datasource.username}")
    private String username;

    @Value("${spring.datasource.password}")
    private String password;

    @Value("${spring.datasource.driver-class-name}")
    private String driverClassName;

    @Value("${spring.datasource.hikari.minimum-idle:5}")
    private int minimumIdle;

    @Value("${spring.datasource.hikari.maximum-pool-size:20}")
    private int maximumPoolSize;

    @Value("${spring.datasource.hikari.idle-timeout:300000}")
    private long idleTimeout;

    @Value("${spring.datasource.hikari.connection-timeout:20000}")
    private long connectionTimeout;

    @Value("${spring.datasource.hikari.max-lifetime:1200000}")
    private long maxLifetime;

    @Value("${spring.datasource.hikari.pool-name:SwnihHikariPool}")
    private String poolName;

    /**
     * Configure HikariCP DataSource with connection pooling settings.
     * Pool size is configured between 5-20 connections as per requirements.
     * 
     * @return configured HikariDataSource
     */
    @Bean
    @Primary
    public DataSource dataSource() {
        HikariConfig config = new HikariConfig();
        
        // Use H2 in-memory database for easy setup
        config.setJdbcUrl("jdbc:h2:mem:swnih_db;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("password");
        
        // Connection pool settings (Requirements 6.5)
        config.setMinimumIdle(minimumIdle);           // Minimum 5 connections
        config.setMaximumPoolSize(maximumPoolSize);   // Maximum 20 connections
        config.setIdleTimeout(idleTimeout);           // 5 minutes idle timeout
        config.setConnectionTimeout(connectionTimeout); // 20 seconds connection timeout
        config.setMaxLifetime(maxLifetime);           // 20 minutes max lifetime
        config.setPoolName(poolName);
        
        // Performance and reliability settings
        config.setLeakDetectionThreshold(60000);      // 1 minute leak detection
        config.setValidationTimeout(5000);            // 5 seconds validation timeout
        config.setConnectionTestQuery("SELECT 1");    // Simple validation query
        
        // Additional HikariCP optimizations
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("useLocalSessionState", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        config.addDataSourceProperty("cacheResultSetMetadata", "true");
        config.addDataSourceProperty("cacheServerConfiguration", "true");
        config.addDataSourceProperty("elideSetAutoCommits", "true");
        config.addDataSourceProperty("maintainTimeStats", "false");
        
        return new HikariDataSource(config);
    }

    /**
     * Get current connection pool statistics for monitoring.
     * @return connection pool information
     */
    public String getConnectionPoolInfo() {
        return String.format(
            "HikariCP Pool Configuration: minimumIdle=%d, maximumPoolSize=%d, " +
            "idleTimeout=%dms, connectionTimeout=%dms, maxLifetime=%dms",
            minimumIdle, maximumPoolSize, idleTimeout, connectionTimeout, maxLifetime
        );
    }
}
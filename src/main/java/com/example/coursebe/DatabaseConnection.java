package com.example.coursebe;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;

public class DatabaseConnection {
    private static final HikariDataSource dataSource;

    static {
        String dbUrl = System.getenv("DATABASE_URL");
        String dbUsername = System.getenv("DATABASE_USERNAME");
        String dbPassword = System.getenv("DATABASE_PASSWORD");

        if (dbUrl == null || dbUsername == null || dbPassword == null) {
            throw new IllegalArgumentException("Database connection details are not set in environment variables.");
        }
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(dbUrl);
        config.setUsername(dbUsername);
        config.setPassword(dbPassword);
        config.setDriverClassName("org.postgresql.Driver");
        config.setMaximumPoolSize(10);

        dataSource = new HikariDataSource(config);
    }

    public static DataSource getDataSource() {
        return dataSource;
    }
}
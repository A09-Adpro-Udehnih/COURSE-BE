package com.example.coursebe;

import org.flywaydb.core.Flyway;

public class MigrationManager {
    public static void migrate() {
        Flyway flyway = Flyway.configure()
                .dataSource(DatabaseConnection.getDataSource())
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
    }

    public static void main(String[] args) {
        System.out.println("☕ Running database migrations...");
        migrate();
        System.out.println("✅ Migrations completed successfully!");
    }
}

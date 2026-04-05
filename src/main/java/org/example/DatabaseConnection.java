package org.example;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DatabaseConnection {

    private static final String DEFAULT_URL =
            "jdbc:mysql://localhost:3306/shopping_cart_localization"
                    + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
                    + "&characterEncoding=UTF-8";

    private DatabaseConnection() {
    }

    public static String getJdbcUrl() {
        return envOrDefault("SHOPPING_CART_DB_URL", DEFAULT_URL);
    }

    public static String getUsername() {
        return envOrDefault("SHOPPING_CART_DB_USER", "root");
    }

    public static String getPassword() {
        return envOrDefault("SHOPPING_CART_DB_PASSWORD", "toor");
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(getJdbcUrl(), getUsername(), getPassword());
    }

    private static String envOrDefault(String name, String defaultValue) {
        String v = System.getenv(name);
        return (v == null || v.isBlank()) ? defaultValue : v;
    }
}

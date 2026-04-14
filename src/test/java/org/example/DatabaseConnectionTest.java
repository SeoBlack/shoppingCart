package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DatabaseConnectionTest {

    @BeforeAll
    static void registerDriver() throws SQLException {
        MockJdbcSupport.registerDriver();
    }

    @Test
    void gettersReturnConfiguredDefaults() {
        assertEquals(MockJdbcSupport.DEFAULT_DB_URL, DatabaseConnection.getJdbcUrl());
        assertEquals("root", DatabaseConnection.getUsername());
        assertEquals("toor", DatabaseConnection.getPassword());
    }

    @Test
    void getConnectionUsesDriverManagerWithExpectedCredentials() throws SQLException {
        MockJdbcSupport.Scenario scenario = new MockJdbcSupport.Scenario();
        MockJdbcSupport.setScenario(scenario);

        Connection connection = DatabaseConnection.getConnection();
        assertNotNull(connection);
        connection.close();

        assertEquals(MockJdbcSupport.DEFAULT_DB_URL, scenario.capturedUrl);
        assertEquals("root", scenario.capturedUser);
        assertEquals("toor", scenario.capturedPassword);
    }
}

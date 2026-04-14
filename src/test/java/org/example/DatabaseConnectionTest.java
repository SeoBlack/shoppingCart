package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

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

    @Test
    void envOrDefaultCoversBothBranches() throws Exception {
        Method m = DatabaseConnection.class.getDeclaredMethod("envOrDefault", String.class, String.class);
        m.setAccessible(true);

        String fromPath = (String) m.invoke(null, "PATH", "fallback");
        String fromMissing = (String) m.invoke(null, "THIS_VAR_SHOULD_NOT_EXIST_4242", "fallback");

        assertNotEquals("fallback", fromPath);
        assertEquals("fallback", fromMissing);
    }

    @Test
    void privateConstructorCanBeInvokedByReflection() throws Exception {
        Constructor<DatabaseConnection> ctor = DatabaseConnection.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        assertNotNull(ctor.newInstance());
    }
}

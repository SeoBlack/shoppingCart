package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LocalizationServiceTest {

    private static final String SELECT_BY_LANG =
            "SELECT `key`, value FROM localization_strings WHERE language = ?";

    @BeforeAll
    static void registerDriver() throws SQLException {
        MockJdbcSupport.registerDriver();
    }

    @Test
    void loadStringsReturnsRowsFromResultSet() throws SQLException {
        MockJdbcSupport.Scenario scenario = new MockJdbcSupport.Scenario();
        MockJdbcSupport.StatementBehavior behavior = MockJdbcSupport.StatementBehavior.forUpdateResult(1);

        Map<Integer, Object> row1 = new HashMap<>();
        row1.put(1, "title");
        row1.put(2, "Hello");
        Map<Integer, Object> row2 = new HashMap<>();
        row2.put(1, "button.add");
        row2.put(2, "Add");
        behavior.queryRows.add(row1);
        behavior.queryRows.add(row2);
        scenario.whenSql(SELECT_BY_LANG, behavior);
        MockJdbcSupport.setScenario(scenario);

        Map<String, String> strings = new LocalizationService().loadStrings("en");

        assertEquals(2, strings.size());
        assertEquals("Hello", strings.get("title"));
        assertEquals("Add", strings.get("button.add"));
    }

    @Test
    void loadStringsReturnsEmptyMapWhenNoRows() throws SQLException {
        MockJdbcSupport.Scenario scenario = new MockJdbcSupport.Scenario();
        scenario.whenSql(SELECT_BY_LANG, MockJdbcSupport.StatementBehavior.forUpdateResult(1));
        MockJdbcSupport.setScenario(scenario);

        Map<String, String> strings = new LocalizationService().loadStrings("sv");

        assertTrue(strings.isEmpty());
    }
}

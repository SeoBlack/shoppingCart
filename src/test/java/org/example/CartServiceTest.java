package org.example;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CartServiceTest {

    private static final String INSERT_RECORD =
            "INSERT INTO cart_records (total_items, total_cost, language) VALUES (?,?,?)";
    private static final String UPDATE_RECORD_TOTALS =
            "UPDATE cart_records SET total_items = ?, total_cost = ? WHERE id = ?";
    private static final String INSERT_ITEM =
            "INSERT INTO cart_items (cart_record_id, item_number, price, quantity, subtotal) VALUES (?,?,?,?,?)";

    @BeforeAll
    static void registerDriver() throws SQLException {
        MockJdbcSupport.registerDriver();
    }

    @Test
    void persistAfterAddThrowsForEmptyCart() {
        CartService service = new CartService();
        ShoppingCart cart = new ShoppingCart();

        assertThrows(IllegalArgumentException.class, () -> service.persistAfterAdd(cart, "en", null));
    }

    @Test
    void persistAfterAddInsertsRecordAndItemForNewCart() throws SQLException {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(12.5, 2);

        MockJdbcSupport.Scenario scenario = new MockJdbcSupport.Scenario();
        MockJdbcSupport.StatementBehavior insertRecord = MockJdbcSupport.StatementBehavior.forUpdateResult(1);
        Map<Integer, Object> generatedRow = new HashMap<>();
        generatedRow.put(1, 101);
        insertRecord.generatedRows.add(generatedRow);
        scenario.whenSql(INSERT_RECORD, insertRecord);
        scenario.whenSql(INSERT_ITEM, MockJdbcSupport.StatementBehavior.forUpdateResult(1));
        MockJdbcSupport.setScenario(scenario);

        int id = new CartService().persistAfterAdd(cart, "en", null);

        assertEquals(101, id);
        assertTrue(scenario.executedSql.contains(INSERT_RECORD));
        assertTrue(scenario.executedSql.contains(INSERT_ITEM));
        assertTrue(scenario.events.contains("commit"));
    }

    @Test
    void persistAfterAddUpdatesExistingRecordAndInsertsItem() throws SQLException {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(9.0, 3);

        MockJdbcSupport.Scenario scenario = new MockJdbcSupport.Scenario();
        scenario.whenSql(UPDATE_RECORD_TOTALS, MockJdbcSupport.StatementBehavior.forUpdateResult(1));
        scenario.whenSql(INSERT_ITEM, MockJdbcSupport.StatementBehavior.forUpdateResult(1));
        MockJdbcSupport.setScenario(scenario);

        int id = new CartService().persistAfterAdd(cart, "sv", 44);

        assertEquals(44, id);
        assertTrue(scenario.executedSql.contains(UPDATE_RECORD_TOTALS));
        assertTrue(scenario.executedSql.contains(INSERT_ITEM));
        assertTrue(scenario.events.contains("commit"));
    }

    @Test
    void persistAfterAddRollsBackWhenUpdateAffectsNoRows() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(2.0, 1);

        MockJdbcSupport.Scenario scenario = new MockJdbcSupport.Scenario();
        scenario.whenSql(UPDATE_RECORD_TOTALS, MockJdbcSupport.StatementBehavior.forUpdateResult(0));
        scenario.whenSql(INSERT_ITEM, MockJdbcSupport.StatementBehavior.forUpdateResult(1));
        MockJdbcSupport.setScenario(scenario);

        SQLException ex = assertThrows(SQLException.class,
                () -> new CartService().persistAfterAdd(cart, "en", 55));
        assertTrue(ex.getMessage().contains("cart_record not found"));
        assertTrue(scenario.events.contains("rollback"));
    }

    @Test
    void persistAfterAddRollsBackWhenInsertRecordHasNoGeneratedKey() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(5.0, 5);

        MockJdbcSupport.Scenario scenario = new MockJdbcSupport.Scenario();
        scenario.whenSql(INSERT_RECORD, MockJdbcSupport.StatementBehavior.forUpdateResult(1));
        scenario.whenSql(INSERT_ITEM, MockJdbcSupport.StatementBehavior.forUpdateResult(1));
        MockJdbcSupport.setScenario(scenario);

        SQLException ex = assertThrows(SQLException.class,
                () -> new CartService().persistAfterAdd(cart, "fi", null));
        assertTrue(ex.getMessage().contains("No generated key"));
        assertTrue(scenario.events.contains("rollback"));
    }
}

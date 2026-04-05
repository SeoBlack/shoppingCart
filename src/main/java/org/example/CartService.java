package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

public final class CartService {

    private static final String INSERT_RECORD =
            "INSERT INTO cart_records (total_items, total_cost, language) VALUES (?,?,?)";

    private static final String UPDATE_RECORD_TOTALS =
            "UPDATE cart_records SET total_items = ?, total_cost = ? WHERE id = ?";

    private static final String INSERT_ITEM =
            "INSERT INTO cart_items (cart_record_id, item_number, price, quantity, subtotal) VALUES (?,?,?,?,?)";
    public int persistAfterAdd(ShoppingCart cart, String language, Integer existingCartRecordId) throws SQLException {
        int n = cart.getItems().size();
        if (n < 1) {
            throw new IllegalArgumentException("cart must contain at least one item");
        }

        Map<Double, Integer> row = cart.getItems().get(n - 1);
        Map.Entry<Double, Integer> entry = row.entrySet().iterator().next();
        double price = entry.getKey();
        int quantity = entry.getValue();
        double subtotal = price * quantity;
        double totalCost = cart.getTotalPrice();

        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                int cartRecordId;
                if (existingCartRecordId == null) {
                    try (PreparedStatement ps = conn.prepareStatement(INSERT_RECORD, Statement.RETURN_GENERATED_KEYS)) {
                        ps.setInt(1, n);
                        ps.setDouble(2, totalCost);
                        ps.setString(3, language);
                        ps.executeUpdate();
                        try (ResultSet keys = ps.getGeneratedKeys()) {
                            if (!keys.next()) {
                                throw new SQLException("No generated key for cart_records insert");
                            }
                            cartRecordId = keys.getInt(1);
                        }
                    }
                } else {
                    cartRecordId = existingCartRecordId;
                    try (PreparedStatement ps = conn.prepareStatement(UPDATE_RECORD_TOTALS)) {
                        ps.setInt(1, n);
                        ps.setDouble(2, totalCost);
                        ps.setInt(3, cartRecordId);
                        if (ps.executeUpdate() != 1) {
                            throw new SQLException("cart_record not found: " + cartRecordId);
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(INSERT_ITEM)) {
                    ps.setInt(1, cartRecordId);
                    ps.setInt(2, n);
                    ps.setDouble(3, price);
                    ps.setInt(4, quantity);
                    ps.setDouble(5, subtotal);
                    ps.executeUpdate();
                }

                conn.commit();
                return cartRecordId;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        }
    }
}

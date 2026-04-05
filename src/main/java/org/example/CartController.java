package org.example;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.sql.SQLException;
import java.util.ResourceBundle;

public class CartController implements Initializable {

    @FXML
    private TextField priceField;
    @FXML
    private TextField qtyField;
    @FXML
    private Label errorLabel;
    @FXML
    private Label totalValue;
    @FXML
    private TableView<CartItem> cartTable;
    @FXML
    private TableColumn<CartItem, Double> priceCol;
    @FXML
    private TableColumn<CartItem, Integer> qtyCol;
    @FXML
    private TableColumn<CartItem, Double> lineTotalCol;
    @FXML
    private Button exitBtn;

    private ShoppingCart shoppingCart;
    private String languageCode;
    private CartService cartService;
    private Integer dbCartRecordId;
    private ResourceBundle rb;
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        this.rb = rb;

        priceCol.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getPrice()).asObject());
        priceCol.setCellFactory(c -> formatDoubleCell());

        qtyCol.setCellValueFactory(d ->
                new SimpleIntegerProperty(d.getValue().getQuantity()).asObject());

        lineTotalCol.setCellValueFactory(d ->
                new SimpleDoubleProperty(d.getValue().getLineTotal()).asObject());
        lineTotalCol.setCellFactory(c -> formatDoubleCell());

        cartTable.setItems(cartItems);
        cartTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    public void initCart(ShoppingCart cart, String languageCode, CartService cartService) {
        this.shoppingCart = cart;
        this.languageCode = languageCode;
        this.cartService = cartService;
        this.dbCartRecordId = null;
    }

    @FXML
    private void onAddItem() {
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            int qty = Integer.parseInt(qtyField.getText().trim());
            if (price >= 0 && qty > 0) {
                shoppingCart.addItem(price, qty);
                cartItems.add(new CartItem(price, qty));
                priceField.clear();
                qtyField.clear();
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
                totalValue.setText(String.format("%.2f", shoppingCart.getTotalPrice()));
                try {
                    dbCartRecordId = cartService.persistAfterAdd(shoppingCart, languageCode, dbCartRecordId);
                } catch (SQLException e) {
                    undoLastAdd(price, qty);
                    new Alert(Alert.AlertType.ERROR,
                            "Could not save the cart to the database.\n" + e.getMessage()).showAndWait();
                }
            } else {
                showError(rb.getString("invalidInputError"));
            }
        } catch (NumberFormatException ex) {
            showError(rb.getString("invalidInputError"));
        }
    }

    @FXML
    private void onExit() {
        ((Stage) exitBtn.getScene().getWindow()).close();
    }

    private void undoLastAdd(double price, int qty) {
        if (!shoppingCart.getItems().isEmpty()) {
            shoppingCart.getItems().remove(shoppingCart.getItems().size() - 1);
        }
        if (!cartItems.isEmpty()) {
            CartItem last = cartItems.get(cartItems.size() - 1);
            if (last.getPrice() == price && last.getQuantity() == qty) {
                cartItems.remove(cartItems.size() - 1);
            }
        }
        totalValue.setText(String.format("%.2f", shoppingCart.getTotalPrice()));
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private TableCell<CartItem, Double> formatDoubleCell() {
        return new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                setText(empty || val == null ? null : String.format("%.2f", val));
            }
        };
    }

    public static class CartItem {
        private final double price;
        private final int quantity;

        CartItem(double price, int quantity) {
            this.price = price;
            this.quantity = quantity;
        }

        public double getPrice() {
            return price;
        }

        public int getQuantity() {
            return quantity;
        }

        public double getLineTotal() {
            return price * quantity;
        }
    }
}

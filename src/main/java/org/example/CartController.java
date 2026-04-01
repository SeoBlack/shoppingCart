package org.example;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class CartController implements Initializable {

    @FXML private TextField                        priceField;
    @FXML private TextField                        qtyField;
    @FXML private Label                            errorLabel;
    @FXML private Label                            totalValue;
    @FXML private TableView<CartItem>              cartTable;
    @FXML private TableColumn<CartItem, Double>    priceCol;
    @FXML private TableColumn<CartItem, Integer>   qtyCol;
    @FXML private TableColumn<CartItem, Double>    lineTotalCol;
    @FXML private Button                           exitBtn;

    private ShoppingCart shoppingCart;
    private ResourceBundle rb;
    private final ObservableList<CartItem> cartItems = FXCollections.observableArrayList();

    //  Lifecycle 
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
    public void initCart(ShoppingCart cart) {
        this.shoppingCart = cart;
    }

    //  Event handlers 

    @FXML
    private void onAddItem() {
        try {
            double price = Double.parseDouble(priceField.getText().trim());
            int    qty   = Integer.parseInt(qtyField.getText().trim());
            if (price >= 0 && qty > 0) {
                shoppingCart.addItem(price, qty);
                cartItems.add(new CartItem(price, qty));
                priceField.clear();
                qtyField.clear();
                errorLabel.setVisible(false);
                errorLabel.setManaged(false);
                totalValue.setText(String.format("%.2f", shoppingCart.getTotalPrice()));
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

    //  Helpers 

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

    //  Table model 

    public static class CartItem {
        private final double price;
        private final int    quantity;

        CartItem(double price, int quantity) {
            this.price    = price;
            this.quantity = quantity;
        }

        public double getPrice()     { return price; }
        public int    getQuantity()  { return quantity; }
        public double getLineTotal() { return price * quantity; }
    }
}

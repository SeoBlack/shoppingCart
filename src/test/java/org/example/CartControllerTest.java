package org.example;

import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CartControllerTest {

    @BeforeAll
    static void initFx() throws InterruptedException {
        FxTestUtils.initToolkit();
    }

    @Test
    void onAddItemShowsValidationErrorForBadNumbers() throws Exception {
        CartController controller = new CartController();
        TextField price = new TextField();
        TextField qty = new TextField();
        Label error = new Label();
        Label total = new Label();
        CartService service = Mockito.mock(CartService.class);

        price.setText("abc");
        qty.setText("1");

        configureController(controller, price, qty, error, total, service);
        FxTestUtils.runOnFxThreadAndWait(() -> invokePrivate(controller, "onAddItem"));

        assertEquals("Invalid input", error.getText());
        assertEquals(true, error.isVisible());
        assertEquals(true, error.isManaged());
    }

    @Test
    void onAddItemAddsRowAndUpdatesTotalWhenPersistSucceeds() throws Exception {
        CartController controller = new CartController();
        TextField price = new TextField();
        TextField qty = new TextField();
        Label error = new Label();
        Label total = new Label();
        CartService service = Mockito.mock(CartService.class);

        price.setText("2.50");
        qty.setText("2");
        Mockito.when(service.persistAfterAdd(Mockito.any(), Mockito.anyString(), Mockito.isNull())).thenReturn(42);

        configureController(controller, price, qty, error, total, service);
        FxTestUtils.runOnFxThreadAndWait(() -> invokePrivate(controller, "onAddItem"));

        ShoppingCart cart = (ShoppingCart) getField(controller, "shoppingCart");
        assertEquals(1, cart.getItems().size());
        assertEquals(5.0, cart.getTotalPrice());
        String normalizedTotal = total.getText().replace(',', '.');
        assertEquals("5.00", normalizedTotal);
    }

    @Test
    void onAddItemShowsValidationErrorForNegativeValues() throws Exception {
        CartController controller = new CartController();
        TextField price = new TextField();
        TextField qty = new TextField();
        Label error = new Label();
        Label total = new Label();
        CartService service = Mockito.mock(CartService.class);

        price.setText("-1.00");
        qty.setText("2");

        configureController(controller, price, qty, error, total, service);
        FxTestUtils.runOnFxThreadAndWait(() -> invokePrivate(controller, "onAddItem"));

        assertEquals("Invalid input", error.getText());
        assertEquals(true, error.isVisible());
    }

    @Test
    void initializeConfiguresTableAndColumns() throws Exception {
        CartController controller = new CartController();
        setField(controller, "priceCol", new TableColumn<CartController.CartItem, Double>());
        setField(controller, "qtyCol", new TableColumn<CartController.CartItem, Integer>());
        setField(controller, "lineTotalCol", new TableColumn<CartController.CartItem, Double>());
        setField(controller, "cartTable", new TableView<CartController.CartItem>());

        FxTestUtils.runOnFxThreadAndWait(() ->
                controller.initialize((URL) null, bundleWithInvalidInput())
        );

        Object priceFactory = ((TableColumn<?, ?>) getField(controller, "priceCol")).getCellValueFactory();
        Object lineTotalFactory = ((TableColumn<?, ?>) getField(controller, "lineTotalCol")).getCellValueFactory();
        assertNotNull(priceFactory);
        assertNotNull(lineTotalFactory);
    }

    @Test
    void undoLastAddRemovesMatchingLastLineAndResetsTotal() throws Exception {
        CartController controller = new CartController();
        TextField price = new TextField();
        TextField qty = new TextField();
        Label error = new Label();
        Label total = new Label();
        CartService service = Mockito.mock(CartService.class);
        Mockito.when(service.persistAfterAdd(Mockito.any(), Mockito.anyString(), Mockito.isNull())).thenReturn(1);

        price.setText("7.00");
        qty.setText("2");
        configureController(controller, price, qty, error, total, service);
        FxTestUtils.runOnFxThreadAndWait(() -> invokePrivate(controller, "onAddItem"));
        FxTestUtils.runOnFxThreadAndWait(() -> invokePrivate(controller, "undoLastAdd", 7.0, 2));

        ShoppingCart cart = (ShoppingCart) getField(controller, "shoppingCart");
        assertEquals(0, cart.getItems().size());
        String normalizedTotal = total.getText().replace(',', '.');
        assertEquals("0.00", normalizedTotal);
    }

    @Test
    void onExitClosesWindow() throws Exception {
        CartController controller = new CartController();
        AtomicReference<Button> exitButtonRef = new AtomicReference<>();
        AtomicReference<Stage> stageRef = new AtomicReference<>();

        FxTestUtils.runOnFxThreadAndWait(() -> {
            Button exitButton = new Button("Exit");
            Stage stage = new Stage();
            stage.setScene(new Scene(new StackPane(exitButton), 200, 120));
            stage.show();
            exitButtonRef.set(exitButton);
            stageRef.set(stage);
        });

        setField(controller, "exitBtn", exitButtonRef.get());
        FxTestUtils.runOnFxThreadAndWait(() -> invokePrivate(controller, "onExit"));

        assertEquals(false, stageRef.get().isShowing());
    }

    @Test
    void formatDoubleCellFormatsToTwoDecimals() throws Exception {
        CartController controller = new CartController();
        @SuppressWarnings("unchecked")
        TableCell<CartController.CartItem, Double> cell =
                (TableCell<CartController.CartItem, Double>) invokePrivateReturn(controller, "formatDoubleCell");

        FxTestUtils.runOnFxThreadAndWait(() -> {
            Method updateItem = cell.getClass().getDeclaredMethod("updateItem", Object.class, boolean.class);
            updateItem.setAccessible(true);
            updateItem.invoke(cell, 12.5, false);
        });

        String normalized = cell.getText().replace(',', '.');
        assertEquals("12.50", normalized);
    }

    private static void configureController(
            CartController controller,
            TextField price,
            TextField qty,
            Label error,
            Label total,
            CartService service
    ) throws Exception {
        setField(controller, "priceField", price);
        setField(controller, "qtyField", qty);
        setField(controller, "errorLabel", error);
        setField(controller, "totalValue", total);
        setField(controller, "rb", bundleWithInvalidInput());
        controller.initCart(new ShoppingCart(), "en", service);
    }

    private static ResourceBundle bundleWithInvalidInput() {
        return new MapResourceBundle(Map.of("invalidInputError", "Invalid input"));
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    private static Object getField(Object target, String fieldName) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        return f.get(target);
    }

    private static void invokePrivate(Object target, String methodName) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        m.invoke(target);
    }

    private static void invokePrivate(Object target, String methodName, Class<?> arg1, Class<?> arg2, Object value1, Object value2) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName, arg1, arg2);
        m.setAccessible(true);
        m.invoke(target, value1, value2);
    }

    private static void invokePrivate(Object target, String methodName, double value1, int value2) throws Exception {
        invokePrivate(target, methodName, double.class, int.class, value1, value2);
    }

    private static Object invokePrivateReturn(Object target, String methodName) throws Exception {
        Method m = target.getClass().getDeclaredMethod(methodName);
        m.setAccessible(true);
        return m.invoke(target);
    }
}

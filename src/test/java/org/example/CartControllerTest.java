package org.example;

import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
}

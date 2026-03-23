import org.example.ShoppingCart;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class shoppingcartTests {

    @Test
    void startsWithEmptyCart() {
        ShoppingCart cart = new ShoppingCart();

        assertTrue(cart.getItems().isEmpty());
        assertEquals(0.0, cart.getTotalPrice(), 0.000001);
    }

    @Test
    void addItemStoresPriceAndQuantity() {
        ShoppingCart cart = new ShoppingCart();

        cart.addItem(19.99, 3);

        assertEquals(1, cart.getItems().size());
        Map<Double, Integer> firstItem = cart.getItems().get(0);
        assertEquals(3, firstItem.get(19.99));
    }

    @Test
    void totalPriceSumsAllItems() {
        ShoppingCart cart = new ShoppingCart();
        cart.addItem(10.0, 2);
        cart.addItem(2.5, 4);
        cart.addItem(1.99, 1);

        double total = cart.getTotalPrice();

        assertEquals(31.99, total, 0.000001);
    }
}

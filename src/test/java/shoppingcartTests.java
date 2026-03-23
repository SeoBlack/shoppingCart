import org.example.ShoppingCart;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("ShoppingCart calculations")
public class shoppingcartTests {

    private static final double EPS = 1e-6;

    @Nested
    @DisplayName("Individual item (line) cost")
    class IndividualLineCost {

        @Test
        @DisplayName("single line: price × quantity")
        void oneItemLineTotalMatchesPriceTimesQuantity() {
            ShoppingCart cart = new ShoppingCart();
            cart.addItem(12.50, 4);

            assertEquals(50.0, cart.getLineTotal(0), EPS);
        }

        @Test
        @DisplayName("each line computed independently")
        void multipleLinesEachMatchPriceTimesQuantity() {
            ShoppingCart cart = new ShoppingCart();
            cart.addItem(10.0, 2);   // 20.0
            cart.addItem(3.33, 3);   // 9.99
            cart.addItem(0.99, 10);  // 9.90

            assertEquals(20.0, cart.getLineTotal(0), EPS);
            assertEquals(9.99, cart.getLineTotal(1), EPS);
            assertEquals(9.90, cart.getLineTotal(2), EPS);
        }

        @Test
        @DisplayName("quantity 1 uses full unit price")
        void quantityOneIsUnitPrice() {
            ShoppingCart cart = new ShoppingCart();
            cart.addItem(7.77, 1);

            assertEquals(7.77, cart.getLineTotal(0), EPS);
        }

        @Test
        @DisplayName("invalid index throws")
        void invalidIndexThrows() {
            ShoppingCart cart = new ShoppingCart();
            cart.addItem(1.0, 1);

            assertThrows(IndexOutOfBoundsException.class, () -> cart.getLineTotal(-1));
            assertThrows(IndexOutOfBoundsException.class, () -> cart.getLineTotal(1));
        }
    }

    @Nested
    @DisplayName("Total cart cost")
    class TotalCartCost {

        @Test
        @DisplayName("empty cart total is zero")
        void emptyCartTotalIsZero() {
            ShoppingCart cart = new ShoppingCart();

            assertEquals(0.0, cart.getTotalPrice(), EPS);
        }

        @Test
        @DisplayName("total equals sum of all line totals")
        void totalEqualsSumOfLineTotals() {
            ShoppingCart cart = new ShoppingCart();
            cart.addItem(10.0, 2);
            cart.addItem(2.5, 4);
            cart.addItem(1.99, 1);

            double expected =
                    cart.getLineTotal(0) + cart.getLineTotal(1) + cart.getLineTotal(2);

            assertEquals(expected, cart.getTotalPrice(), EPS);
            assertEquals(31.99, cart.getTotalPrice(), EPS);
        }

        @Test
        @DisplayName("single item total equals that line only")
        void singleItemTotalMatchesLineTotal() {
            ShoppingCart cart = new ShoppingCart();
            cart.addItem(19.99, 3);

            assertEquals(cart.getLineTotal(0), cart.getTotalPrice(), EPS);
            assertEquals(59.97, cart.getTotalPrice(), EPS);
        }

        @Test
        @DisplayName("many lines accumulate correctly")
        void manyItemsSumCorrectly() {
            ShoppingCart cart = new ShoppingCart();
            for (int i = 1; i <= 5; i++) {
                cart.addItem((double) i, i);
            }
            // 1*1 + 2*2 + 3*3 + 4*4 + 5*5 = 1+4+9+16+25 = 55
            assertEquals(55.0, cart.getTotalPrice(), EPS);
        }
    }

    @Nested
    @DisplayName("Storage and getters")
    class Storage {

        @Test
        void startsWithEmptyCart() {
            ShoppingCart cart = new ShoppingCart();

            assertTrue(cart.getItems().isEmpty());
            assertEquals(0.0, cart.getTotalPrice(), EPS);
        }

        @Test
        void addItemStoresPriceAndQuantity() {
            ShoppingCart cart = new ShoppingCart();

            cart.addItem(19.99, 3);

            assertEquals(1, cart.getItems().size());
            Map<Double, Integer> firstItem = cart.getItems().get(0);
            assertEquals(3, firstItem.get(19.99));
        }
    }
}

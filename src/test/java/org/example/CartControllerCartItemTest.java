package org.example;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CartControllerCartItemTest {

    @Test
    void cartItemGettersAndLineTotalWork() {
        CartController.CartItem item = new CartController.CartItem(4.25, 4);

        assertEquals(4.25, item.getPrice());
        assertEquals(4, item.getQuantity());
        assertEquals(17.0, item.getLineTotal());
    }
}

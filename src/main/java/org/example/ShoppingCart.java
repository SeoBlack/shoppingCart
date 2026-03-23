package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


//{itemPrice, itemQuantity}
public class ShoppingCart {
    ArrayList<Map<Double, Integer>> items;
    public ShoppingCart() {
        this.items = new ArrayList<Map<Double, Integer>>();
    }
    public void addItem(Double price, int quantity) {
        Map<Double, Integer> item = new HashMap<Double, Integer>();
        item.put(price, quantity);
        this.items.add(item);
    }
    public ArrayList<Map<Double, Integer>> getItems() {
        return items;
    }
    public Double getTotalPrice() {
        Double totalPrice = 0.0;
        System.out.println(items);
        for (Map<Double, Integer> item : items) {
            for (Map.Entry<Double, Integer> entry : item.entrySet()) {
                totalPrice += entry.getKey() * entry.getValue();
            }
        }
        return totalPrice;
    }


}

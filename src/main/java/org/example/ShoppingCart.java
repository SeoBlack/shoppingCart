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

    //get one item total price
    public double getLineTotal(int index) {
        Map<Double, Integer> item = items.get(index);
        double lineTotal = 0.0;
        for (Map.Entry<Double, Integer> entry : item.entrySet()) {
            lineTotal += entry.getKey() * entry.getValue();
        }
        return lineTotal;
    }

 
    public Double getTotalPrice() {
        double totalPrice = 0.0;
        for (int i = 0; i < items.size(); i++) {
            totalPrice += getLineTotal(i);
        }
        return totalPrice;
    }


}

package org.example;

import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        ShoppingCart shoppingCart = new ShoppingCart();


        Scanner sc = new Scanner(System.in);
        String language;
        String country;

        System.out.println("pick a language to start:");
        System.out.println("1. English");
        System.out.println("2. Swedish");
        System.out.println("3. Finnish");
        System.out.println("4. Japanese");

        int localeChoise = sc.nextInt();
        switch (localeChoise) {
            case 1:
                language = "en";
                country = "UK";
                break;
            case 2:
                language = "sv";
                country = "SW";
                break;
            case 3:
                language = "fi";
                country = "FI";
                break;
            case 4:
                language = "ja";
                country = "JA";
                break;
                default:
                    language = "en";
                    country = "UK";

        }
        Locale local = new Locale(language, country);
        ResourceBundle rb =  ResourceBundle.getBundle("languages", local);
        while(true){
            System.out.println("1. " + rb.getString("menuAddItem"));
            System.out.println("2. " + rb.getString("menuDisplayTotalPrice"));
            System.out.println("3. " + rb.getString("menuExit"));

            int choice = sc.nextInt();
            switch (choice){
                case 1:
                    System.out.println(rb.getString("enterPrice"));
                    double price = sc.nextDouble();
                    System.out.println(rb.getString("enterQuantity"));
                    int quantity = sc.nextInt();
                    if(price>=0 && quantity>0){
                        shoppingCart.addItem(price,quantity);
                    }else {
                        System.out.println(rb.getString("invalidInputError"));
                    }
                    break;
                case 2:
                    System.out.println(rb.getString("totalPrice")+": " +  shoppingCart.getTotalPrice());
                    break;
                case 3:
                    System.exit(0);

            }
        }
    }
}
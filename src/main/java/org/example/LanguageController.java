package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.ResourceBundle;

public class LanguageController {

    @FXML
    private ComboBox<String> langCombo;

    private final LocalizationService localizationService = new LocalizationService();

    @FXML
    public void initialize() {
        langCombo.getItems().addAll(
                "English",
                "Svenska",
                "Suomi",
                "\u65E5\u672C\u8A9E",
                "\u0627\u0644\u0639\u0631\u0628\u064A\u0629"
        );
        langCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void onConfirm() {
        String lang;
        boolean rtl = false;

        switch (langCombo.getSelectionModel().getSelectedIndex()) {
            case 1 -> lang = "sv";
            case 2 -> lang = "fi";
            case 3 -> lang = "ja";
            case 4 -> {
                lang = "ar";
                rtl = true;
            }
            default -> lang = "en";
        }

        final String languageCode = lang;

        Map<String, String> strings;
        try {
            strings = localizationService.loadStrings(languageCode);
        } catch (SQLException e) {
            new Alert(Alert.AlertType.ERROR,
                    "Could not load UI strings from the database.\n" + e.getMessage()).showAndWait();
            return;
        }

        if (strings.isEmpty()) {
            new Alert(Alert.AlertType.WARNING,
                    "No localization rows found for language \"" + languageCode + "\".").showAndWait();
            return;
        }

        ResourceBundle bundle = new MapResourceBundle(strings);
        CartService cartService = new CartService();

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cart_view.fxml"));
            loader.setResources(bundle);
            javafx.scene.Parent root = loader.load();

            CartController cartController = loader.getController();
            cartController.initCart(new ShoppingCart(), languageCode, cartService);

            Scene scene = new Scene(root, 800, 580);
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            if (rtl) {
                scene.getRoot().setNodeOrientation(NodeOrientation.RIGHT_TO_LEFT);
            }

            Stage stage = (Stage) langCombo.getScene().getWindow();
            stage.setResizable(true);
            stage.setScene(scene);
            stage.setWidth(800);
            stage.setHeight(620);
            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

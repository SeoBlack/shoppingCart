package org.example;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.NodeOrientation;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class LanguageController {

    @FXML private ComboBox<String> langCombo;

    private static final ResourceBundle.Control UTF8_CONTROL = new ResourceBundle.Control() {
        @Override
        public ResourceBundle newBundle(String baseName, Locale locale, String format,
                                        ClassLoader loader, boolean reload) throws IOException {
            String bundleName   = toBundleName(baseName, locale);
            String resourceName = toResourceName(bundleName, "properties");
            try (InputStream is = loader.getResourceAsStream(resourceName)) {
                if (is == null) return null;
                return new PropertyResourceBundle(new InputStreamReader(is, StandardCharsets.UTF_8));
            }
        }
    };

    @FXML
    public void initialize() {
        langCombo.getItems().addAll(
                "English",
                "Svenska",
                "Suomi",
                "\u65E5\u672C\u8A9E",               // 日本語
                "\u0627\u0644\u0639\u0631\u0628\u064A\u0629"  // العربية
        );
        langCombo.getSelectionModel().selectFirst();
    }

    @FXML
    private void onConfirm() {
        String lang, country;
        boolean rtl = false;

        switch (langCombo.getSelectionModel().getSelectedIndex()) {
            case 1 -> { lang = "sv"; country = "SW"; }
            case 2 -> { lang = "fi"; country = "FI"; }
            case 3 -> { lang = "ja"; country = "JA"; }
            case 4 -> { lang = "ar"; country = "SA"; rtl = true; }
            default -> { lang = "en"; country = "UK"; }
        }

        Locale locale = new Locale(lang, country);
        ResourceBundle rb = ResourceBundle.getBundle("languages", locale, UTF8_CONTROL);

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/cart_view.fxml"));
            loader.setResources(rb);
            javafx.scene.Parent root = loader.load();

            CartController cartController = loader.getController();
            cartController.initCart(new ShoppingCart());

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

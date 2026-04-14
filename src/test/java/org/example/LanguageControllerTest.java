package org.example;

import javafx.scene.control.ComboBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LanguageControllerTest {

    @BeforeAll
    static void initFx() throws InterruptedException {
        FxTestUtils.initToolkit();
    }

    @Test
    void initializePopulatesLanguagesAndSelectsFirst() throws Exception {
        LanguageController controller = new LanguageController();
        ComboBox<String> comboBox = new ComboBox<>();
        setField(controller, "langCombo", comboBox);

        FxTestUtils.runOnFxThreadAndWait(controller::initialize);

        assertEquals(5, comboBox.getItems().size());
        assertEquals(List.of("English", "Svenska", "Suomi", "日本語", "العربية"), comboBox.getItems());
        assertEquals("English", comboBox.getSelectionModel().getSelectedItem());
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

}

package org.example;

import javafx.application.Application;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ShoppingCartAppMainTest {

    @BeforeAll
    static void initFx() throws InterruptedException {
        FxTestUtils.initToolkit();
    }

    @Test
    void mainCallsApplicationLaunchWithArgs() {
        String[] args = {"--smoke"};
        try (MockedStatic<Application> mocked = Mockito.mockStatic(Application.class)) {
            ShoppingCartApp.main(args);
            mocked.verify(() -> Application.launch(args));
        }
    }

    @Test
    void startConfiguresPrimaryStage() throws Exception {
        ShoppingCartApp app = new ShoppingCartApp();
        FxTestUtils.runOnFxThreadAndWait(() -> {
            Stage stage = new Stage();
            app.start(stage);

            assertEquals("Soreen Oraibi", stage.getTitle());
            assertFalse(stage.isResizable());
            assertNotNull(stage.getScene());
            assertFalse(stage.getScene().getStylesheets().isEmpty());

            stage.close();
        });
    }
}

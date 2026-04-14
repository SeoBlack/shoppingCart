package org.example;

import javafx.application.Application;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class ShoppingCartAppMainTest {

    @Test
    void mainCallsApplicationLaunchWithArgs() {
        String[] args = {"--smoke"};
        try (MockedStatic<Application> mocked = Mockito.mockStatic(Application.class)) {
            ShoppingCartApp.main(args);
            mocked.verify(() -> Application.launch(args));
        }
    }
}

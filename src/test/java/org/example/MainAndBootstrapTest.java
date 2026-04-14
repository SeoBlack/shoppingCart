package org.example;

import javafx.application.Application;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import java.lang.reflect.Constructor;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class MainAndBootstrapTest {

    @Test
    void mainDelegatesToMainApp() {
        String[] args = {"a", "b"};
        try (MockedStatic<MainApp> mocked = Mockito.mockStatic(MainApp.class)) {
            Main.main(args);
            mocked.verify(() -> MainApp.main(args));
        }
    }

    @Test
    void mainAppMainLaunchesShoppingCartApp() {
        String[] args = {"x"};
        try (MockedStatic<Application> mocked = Mockito.mockStatic(Application.class)) {
            MainApp.main(args);
            mocked.verify(() -> Application.launch(ShoppingCartApp.class, args));
        }
    }

    @Test
    void privateConstructorCanBeReflected() throws Exception {
        Constructor<MainApp> ctor = MainApp.class.getDeclaredConstructor();
        ctor.setAccessible(true);
        MainApp instance = ctor.newInstance();
        assertNotNull(instance);
    }

    @Test
    void mainDefaultConstructorIsCovered() {
        Main instance = new Main();
        assertNotNull(instance);
    }
}

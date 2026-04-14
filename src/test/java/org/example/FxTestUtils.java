package org.example;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

final class FxTestUtils {

    private static final AtomicBoolean STARTED = new AtomicBoolean(false);

    private FxTestUtils() {
    }

    static void initToolkit() throws InterruptedException {
        if (STARTED.compareAndSet(false, true)) {
            CountDownLatch latch = new CountDownLatch(1);
            try {
                Platform.startup(latch::countDown);
            } catch (IllegalStateException alreadyStarted) {
                latch.countDown();
            }
            latch.await(5, TimeUnit.SECONDS);
        }
    }

    static void runOnFxThreadAndWait(ThrowingRunnable action) throws Exception {
        if (Platform.isFxApplicationThread()) {
            action.run();
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        AtomicBoolean failed = new AtomicBoolean(false);
        final Exception[] error = new Exception[1];
        Platform.runLater(() -> {
            try {
                action.run();
            } catch (Exception e) {
                failed.set(true);
                error[0] = e;
            } finally {
                latch.countDown();
            }
        });
        latch.await(10, TimeUnit.SECONDS);
        if (failed.get()) {
            throw error[0];
        }
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}

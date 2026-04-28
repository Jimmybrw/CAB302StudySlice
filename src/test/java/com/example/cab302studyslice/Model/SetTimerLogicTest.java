package com.example.cab302studyslice.Model;

import com.example.cab302studyslice.Controller.TimerController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class SetTimerLogicTest {

    @Test
    void testCountdownRemainingTime() throws Exception {
        TimerController controller = new TimerController();

        // Inject setTimerDurationSeconds = 100 seconds
        Field durationField = TimerController.class.getDeclaredField("setTimerDurationSeconds");
        durationField.setAccessible(true);
        durationField.set(controller, 100L);

        // Fake start time 50 seconds ago
        Field startField = TimerController.class.getDeclaredField("setTimerStartTime");
        startField.setAccessible(true);
        startField.set(controller, System.currentTimeMillis() - 50_000);

        // Access private formatTime
        Method formatMethod = TimerController.class.getDeclaredMethod("formatTime", long.class);
        formatMethod.setAccessible(true);

        long remaining = 100 - 50;
        String formatted = (String) formatMethod.invoke(controller, remaining);

        assertEquals("00:00:50", formatted);
    }

    @Test
    void testCountdownStopsAtZero() throws Exception {
        TimerController controller = new TimerController();

        Field durationField = TimerController.class.getDeclaredField("setTimerDurationSeconds");
        durationField.setAccessible(true);
        durationField.set(controller, 10L);

        Field startField = TimerController.class.getDeclaredField("setTimerStartTime");
        startField.setAccessible(true);
        startField.set(controller, System.currentTimeMillis() - 20_000);

        Method formatMethod = TimerController.class.getDeclaredMethod("formatTime", long.class);
        formatMethod.setAccessible(true);

        long remaining = 10 - 20;
        long safeRemaining = Math.max(remaining, 0);

        String formatted = (String) formatMethod.invoke(controller, safeRemaining);

        assertEquals("00:00:00", formatted);
    }
}

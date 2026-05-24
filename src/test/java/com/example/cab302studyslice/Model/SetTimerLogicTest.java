package com.example.cab302studyslice.Model;

import com.example.cab302studyslice.Controller.TimerController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SetTimerLogicTest {

    @Test
    void testCountdownRemainingTime() throws Exception {
        TimerController controller = new TimerController();

        Method formatMethod = TimerController.class.getDeclaredMethod("formatTime", int.class);
        formatMethod.setAccessible(true);

        String formatted = (String) formatMethod.invoke(controller, 50);

        assertEquals("00:00:50", formatted);
    }

    @Test
    void formatsZeroWhenCountdownWouldGoNegative() throws Exception {
        TimerController controller = new TimerController();

        Method formatMethod = TimerController.class.getDeclaredMethod("formatTime", int.class);
        formatMethod.setAccessible(true);

        int safeRemaining = Math.max(10 - 20, 0);
        String formatted = (String) formatMethod.invoke(controller, safeRemaining);

        assertEquals("00:00:00", formatted);
    }
}

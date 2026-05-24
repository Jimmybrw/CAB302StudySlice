package com.example.cab302studyslice.Model;

import com.example.cab302studyslice.Controller.TimerController;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TimerResetTest {

    @Test
    void timerFormatsZeroCorrectly() throws Exception {

        TimerController controller = new TimerController();

        Method formatMethod =
                TimerController.class.getDeclaredMethod("formatTime", int.class);

        formatMethod.setAccessible(true);

        String formatted =
                (String) formatMethod.invoke(controller, 0);

        assertEquals("00:00:00", formatted);
    }
}
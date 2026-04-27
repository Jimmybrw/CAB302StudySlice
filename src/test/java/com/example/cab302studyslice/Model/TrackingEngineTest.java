package com.example.cab302studyslice.Model;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrackingEngineTest {

    @Test
    void recordsSimplifiedAppTitlesAndUpdatesTotalTime() {
        TrackingEngine engine = new TrackingEngine();
        List<String> uiUpdates = new ArrayList<>();
        engine.setUiUpdater(uiUpdates::add);

        engine.recordTitle("Research notes - Google Chrome");
        engine.recordTitle("Main.java - Code");
        engine.recordTitle("Research notes - Google Chrome");

        Map<String, Integer> timeSpent = engine.getTimeSpent();
        assertEquals(3, engine.getTotalSeconds());
        assertEquals(2, timeSpent.get("WEB: Research notes"));
        assertEquals(1, timeSpent.get("VS CODE"));
        assertEquals(3, uiUpdates.size());
        assertTrue(uiUpdates.get(2).contains("Total Study Time: 00:00:03"));
        assertTrue(uiUpdates.get(2).contains("WEB: Research notes : 00:00:02"));
    }

    @Test
    void resetClearsSessionStateReadyForANewSession() {
        TrackingEngine engine = new TrackingEngine();
        engine.recordTitle("Slides - PowerPnt");
        engine.recordTitle("Desktop");

        engine.reset();

        assertEquals(0, engine.getTotalSeconds());
        assertTrue(engine.getTimeSpent().isEmpty());
    }

    @Test
    void returnedTimeSpentMapCannotMutateEngineState() {
        TrackingEngine engine = new TrackingEngine();
        engine.recordTitle("Inbox - Outlook");

        Map<String, Integer> returnedMap = engine.getTimeSpent();
        returnedMap.put("FAKE APP", 99);

        assertFalse(engine.getTimeSpent().containsKey("FAKE APP"));
        assertEquals(1, engine.getTimeSpent().get("OUTLOOK"));
    }

    @Test
    void simplifiesBrowserTabsDesktopAndKnownApplicationTitles() {
        assertEquals("WEB: CAB302 assignment", TrackingEngine.simplifyTitle("CAB302 assignment - Google Chrome"));
        assertEquals("WEB: Lecture notes", TrackingEngine.simplifyTitle("Lecture notes and 3 more pages - Microsoft Edge"));
        assertEquals("INTELLIJ IDEA", TrackingEngine.simplifyTitle("StudySlice - IntelliJ IDEA"));
        assertEquals("DESKTOP", TrackingEngine.simplifyTitle(""));
    }
}

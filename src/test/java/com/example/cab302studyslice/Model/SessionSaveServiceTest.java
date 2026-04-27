package com.example.cab302studyslice.Model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionSaveServiceTest {
    private final SessionSaveService service = new SessionSaveService();

    @Test
    void preparesTrackedSessionForSavingWithTrimmedTitleTimesAndActivities() {
        Map<String, Integer> timeSpent = new LinkedHashMap<>();
        timeSpent.put("WORD", 600);
        timeSpent.put("WEB: Research", 1200);
        LocalDateTime endTime = LocalDateTime.of(2026, 4, 27, 12, 30);

        SessionSaveService.PrepareResult result = service.prepareSaveRequest(
                "  Assignment block  ",
                7,
                1800,
                timeSpent,
                endTime
        );

        assertTrue(result.isReady());
        SessionSaveService.SaveRequest request = result.request();
        assertEquals("Assignment block", request.title());
        assertEquals(LocalDateTime.of(2026, 4, 27, 12, 0), request.startTime());
        assertEquals(endTime, request.endTime());
        assertEquals(1800, request.totalSeconds());
        assertEquals(2, request.activities().size());
        assertEquals("WORD", request.activities().getFirst().getAppName());
        assertEquals(600, request.activities().getFirst().getDuration());
    }
}

package com.example.cab302studyslice.Model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HistoryFormatterTest {
    private final HistoryFormatter formatter = new HistoryFormatter();

    @Test
    void activityPreviewSortsByDurationAndLimitsToTopFour() {
        List<Activity> activities = List.of(
                new Activity("WORD", 20),
                new Activity("WEB: Research", 90),
                new Activity("INTELLIJ IDEA", 45),
                new Activity("OUTLOOK", 10),
                new Activity("POWERPOINT", 30)
        );

        String preview = formatter.buildActivityPreview(activities);

        assertEquals("""
                Top activities:
                - WEB: Research - 00:01:30
                - INTELLIJ IDEA - 00:00:45
                - POWERPOINT - 00:00:30
                - WORD - 00:00:20
                +1 more""", preview);
    }

    @Test
    void emptyActivityPreviewExplainsThatNothingWasRecorded() {
        assertEquals("No activities recorded.", formatter.buildActivityPreview(List.of()));
        assertEquals("No activities recorded.", formatter.buildActivityPreview(null));
    }

    @Test
    void formatSessionRangeHandlesCompleteAndPartialDates() {
        LocalDateTime start = LocalDateTime.of(2026, 4, 27, 9, 30);
        LocalDateTime end = LocalDateTime.of(2026, 4, 27, 10, 45);

        assertEquals("From 27/04/2026 09:30 to 27/04/2026 10:45", formatter.formatSessionRange(start, end));
        assertEquals("Started 27/04/2026 09:30", formatter.formatSessionRange(start, null));
        assertEquals("Ended 27/04/2026 10:45", formatter.formatSessionRange(null, end));
        assertEquals("Date not available", formatter.formatSessionRange(null, null));
    }

    @Test
    void formatSecondsClampsNegativeDurationsToZero() {
        assertEquals("00:00:00", formatter.formatSeconds(-5));
        assertEquals("01:01:01", formatter.formatSeconds(3661));
    }
}

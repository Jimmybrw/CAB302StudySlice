package com.example.cab302studyslice.Model;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SessionHistoryEntryTest {

    @Test
    void constructorNormalisesMissingTitleAndNegativeDuration() {
        SessionHistoryEntry entry = new SessionHistoryEntry(
                7,
                null,
                LocalDateTime.of(2026, 4, 27, 8, 0),
                LocalDateTime.of(2026, 4, 27, 9, 0),
                -15
        );

        assertEquals("", entry.getTitle());
        assertEquals(0, entry.getTotalSeconds());
        assertEquals("00:00:00", entry.getFormattedTotalTime());
    }

    @Test
    void activitiesIgnoreNullEntriesAndCannotBeMutatedFromOutside() {
        SessionHistoryEntry entry = new SessionHistoryEntry(1, "Morning study", null, null, 3661);

        entry.addActivity(new Activity("WORD", 120));
        entry.addActivity(null);

        assertEquals("01:01:01", entry.getFormattedTotalTime());
        assertEquals(1, entry.getActivities().size());
        assertEquals("WORD", entry.getActivities().getFirst().getAppName());
        assertThrows(UnsupportedOperationException.class, () -> entry.getActivities().add(new Activity("FAKE", 1)));
        assertTrue(entry.getActivities().stream().noneMatch(activity -> activity == null));
    }
}

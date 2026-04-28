package com.example.cab302studyslice.Model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

class HistoryStoreTest {

    @BeforeEach
    void clearHistoryStore() throws Exception {
        Field field = HistoryStore.class.getDeclaredField("historyData");
        field.setAccessible(true);
        ((StringBuilder) field.get(null)).setLength(0);
    }

    @Test
    void emptyStoreReturnsPlaceholderMessage() {
        assertEquals("No study history available yet.", HistoryStore.getHistoryText());
    }

    @Test
    void singleSessionIsReturnedAsIs() {
        HistoryStore.addSession("Session 1");
        assertEquals("Session 1", HistoryStore.getHistoryText());
    }

    @Test
    void multipleSessionsAreSeparatedByDivider() {
        HistoryStore.addSession("Session 1");
        HistoryStore.addSession("Session 2");

        String result = HistoryStore.getHistoryText();
        assertTrue(result.contains("Session 1"));
        assertTrue(result.contains("Session 2"));
        assertTrue(result.contains("------------------------------"));
    }

    @Test
    void firstSessionHasNoDividerPrefix() {
        HistoryStore.addSession("Only session");

        assertFalse(HistoryStore.getHistoryText().contains("---"));
    }

    @Test
    void addingThreeSessionsProducesTwoDividers() {
        HistoryStore.addSession("A");
        HistoryStore.addSession("B");
        HistoryStore.addSession("C");

        String result = HistoryStore.getHistoryText();
        int count = 0;
        int index = 0;
        while ((index = result.indexOf("------------------------------", index)) != -1) {
            count++;
            index += 1;
        }
        assertEquals(2, count);
    }
}
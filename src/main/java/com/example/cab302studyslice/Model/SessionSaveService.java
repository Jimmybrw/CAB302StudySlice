package com.example.cab302studyslice.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Service for preparing session data before database persistence.
 * Validates session information and converts tracked app times into Activity objects.
 */
public class SessionSaveService {
    /**
     * Status codes for session preparation operations.
     */
    public enum Status {
        READY,
        MISSING_SESSION_DATA,
        MISSING_TITLE,
        NOT_LOGGED_IN
    }

    /**
     * Request containing validated session data ready for persistence.
     */
    public record SaveRequest(String title,
                              LocalDateTime startTime,
                              LocalDateTime endTime,
                              int totalSeconds,
                              List<Activity> activities) {
    }

    /**
     * Result of a session preparation operation.
     * Contains validation status, message, and the prepared request if successful.
     */
    public record PrepareResult(Status status, String message, SaveRequest request) {
        /**
         * Checks if the preparation was successful.
         *
         * @return true if status is READY, false otherwise
         */
        public boolean isReady() {
            return status == Status.READY;
        }
    }

    /**
     * Prepares and validates a session save request.
     * Converts tracked time data into Activity objects and generates start time.
     *
     * @param sessionName the name of the session (must not be empty)
     * @param currentUserId the ID of the user saving the session
     * @param totalSeconds the total tracked time in seconds
     * @param timeSpent map of app names to duration in seconds
     * @param endTime the session end time
     * @return a PrepareResult with validation status and prepared request if successful
     */
    public PrepareResult prepareSaveRequest(String sessionName,
                                            int currentUserId,
                                            int totalSeconds,
                                            Map<String, Integer> timeSpent,
                                            LocalDateTime endTime) {
        if (totalSeconds <= 0 || timeSpent == null || timeSpent.isEmpty()) {
            return new PrepareResult(Status.MISSING_SESSION_DATA, "Error: No Session Data", null);
        }

        String cleanSessionName = sessionName == null ? "" : sessionName.trim();
        if (cleanSessionName.isEmpty()) {
            return new PrepareResult(Status.MISSING_TITLE, "Please enter a session name.", null);
        }

        if (currentUserId <= 0) {
            return new PrepareResult(Status.NOT_LOGGED_IN, "Please log in again before saving.", null);
        }

        LocalDateTime safeEndTime = endTime == null ? LocalDateTime.now() : endTime;
        LocalDateTime startTime = safeEndTime.minusSeconds(totalSeconds);
        List<Activity> activities = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : timeSpent.entrySet()) {
            activities.add(new Activity(entry.getKey(), entry.getValue()));
        }

        SaveRequest request = new SaveRequest(cleanSessionName, startTime, safeEndTime, totalSeconds, activities);
        return new PrepareResult(Status.READY, "", request);
    }
}

package com.example.cab302studyslice.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//Prepares study session data before it is saved to the database
//This class validates the session and converts tracked app time into Activity objects
public class SessionSaveService {
    public enum Status {
        READY,
        MISSING_SESSION_DATA,
        MISSING_TITLE,
        NOT_LOGGED_IN
    }

    public record SaveRequest(String title,
                              LocalDateTime startTime,
                              LocalDateTime endTime,
                              int totalSeconds,
                              List<Activity> activities) {
    }

    public record PrepareResult(Status status, String message, SaveRequest request) {
        public boolean isReady() {
            return status == Status.READY;
        }
    }

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

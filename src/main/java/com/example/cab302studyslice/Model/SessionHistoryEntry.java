package com.example.cab302studyslice.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SessionHistoryEntry {
    private final int sessionId;
    private final String title;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final int totalSeconds;
    private final List<Activity> activities;

    public SessionHistoryEntry(int sessionId,
                               String title,
                               LocalDateTime startTime,
                               LocalDateTime endTime,
                               int totalSeconds) {
        this.sessionId = sessionId;
        this.title = title == null ? "" : title;
        this.startTime = startTime;
        this.endTime = endTime;
        this.totalSeconds = Math.max(0, totalSeconds);
        this.activities = new ArrayList<>();
    }

    public int getSessionId() {
        return sessionId;
    }

    public String getTitle() {
        return title;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    public String getFormattedTotalTime() {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    public List<Activity> getActivities() {
        return Collections.unmodifiableList(activities);
    }

    public void addActivity(Activity activity) {
        if (activity != null) {
            activities.add(activity);
        }
    }
}

package com.example.cab302studyslice.Model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a complete saved study session.
 * Contains session metadata (title, times, duration) and a list of tracked app activities.
 */
public class SessionHistoryEntry {
    private final int sessionId;
    private final String title;
    private final LocalDateTime startTime;
    private final LocalDateTime endTime;
    private final int totalSeconds;
    private final List<Activity> activities;

    /**
     * Creates a new session history entry.
     *
     * @param sessionId the unique identifier for this session
     * @param title the session title (null is converted to empty string)
     * @param startTime the start time of the session
     * @param endTime the end time of the session
     * @param totalSeconds the total duration in seconds
     */
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

    /**
     * Gets the session ID.
     *
     * @return the unique session identifier
     */
    public int getSessionId() {
        return sessionId;
    }

    /**
     * Gets the session title.
     *
     * @return the session title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Gets the session start time.
     *
     * @return the start time, or null if not set
     */
    public LocalDateTime getStartTime() {
        return startTime;
    }

    /**
     * Gets the session end time.
     *
     * @return the end time, or null if not set
     */
    public LocalDateTime getEndTime() {
        return endTime;
    }

    /**
     * Gets the total session duration.
     *
     * @return total time in seconds
     */
    public int getTotalSeconds() {
        return totalSeconds;
    }

    /**
     * Gets the total session duration formatted as HH:MM:SS.
     *
     * @return formatted time string
     */
    public String getFormattedTotalTime() {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    /**
     * Gets an unmodifiable list of activities in this session.
     *
     * @return immutable list of activities
     */
    public List<Activity> getActivities() {
        return Collections.unmodifiableList(activities);
    }

    /**
     * Adds an activity to this session.
     * Null activities are silently ignored.
     *
     * @param activity the activity to add
     */
    public void addActivity(Activity activity) {
        if (activity != null) {
            activities.add(activity);
        }
    }
}

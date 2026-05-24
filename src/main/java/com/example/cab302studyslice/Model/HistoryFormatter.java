package com.example.cab302studyslice.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Utility class for formatting session data for display.
 * Provides methods to format activities, date ranges, and time durations.
 */
public class HistoryFormatter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    /**
     * Builds a formatted summary of top activities in a session.
     * Shows up to 4 activities sorted by duration, with a count of remaining apps.
     *
     * @param activities the list of activities to summarize
     * @return formatted activity preview text
     */
    public String buildActivityPreview(List<Activity> activities) {
        if (activities == null || activities.isEmpty()) {
            return "No activities recorded.";
        }

        List<Activity> sorted = new ArrayList<>(activities);
        sorted.sort(Comparator.comparingInt(Activity::getDuration).reversed());

        int limit = Math.min(4, sorted.size());
        StringBuilder builder = new StringBuilder("Top activities:\n");
        for (int i = 0; i < limit; i++) {
            Activity activity = sorted.get(i);
            builder.append("- ")
                    .append(activity.getAppName())
                    .append(" - ")
                    .append(formatSeconds(activity.getDuration()))
                    .append("\n");
        }

        if (sorted.size() > limit) {
            builder.append("+").append(sorted.size() - limit).append(" more");
        } else if (builder.length() > 0) {
            builder.setLength(builder.length() - 1);
        }
        return builder.toString();
    }

    /**
     * Formats the time range of a session.
     * Handles null values gracefully and provides human-readable output.
     *
     * @param start the session start time
     * @param end the session end time
     * @return formatted time range string
     */
    public String formatSessionRange(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) {
            return "Date not available";
        }
        if (start != null && end != null) {
            return "From " + start.format(DATE_TIME_FORMATTER) + " to " + end.format(DATE_TIME_FORMATTER);
        }
        if (start != null) {
            return "Started " + start.format(DATE_TIME_FORMATTER);
        }
        return "Ended " + end.format(DATE_TIME_FORMATTER);
    }

    /**
     * Formats seconds into HH:MM:SS format.
     *
     * @param totalSeconds the time in seconds
     * @return formatted time string
     */
    public String formatSeconds(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        int hours = safeSeconds / 3600;
        int minutes = (safeSeconds % 3600) / 60;
        int seconds = safeSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

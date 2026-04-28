package com.example.cab302studyslice.Model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

//Formats saved session data for display on the History page
public class HistoryFormatter {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    //Builds a short text summary of the apps used during a session
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

    //Formats the start and end time of a saved study session
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

    //Converts seconds into HH:MM:SS format
    public String formatSeconds(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        int hours = safeSeconds / 3600;
        int minutes = (safeSeconds % 3600) / 60;
        int seconds = safeSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

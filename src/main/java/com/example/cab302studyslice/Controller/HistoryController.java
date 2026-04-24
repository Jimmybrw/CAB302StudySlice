package com.example.cab302studyslice.Controller;
import com.example.cab302studyslice.Model.Activity;
import com.example.cab302studyslice.Model.DatabaseManager;
import com.example.cab302studyslice.Model.SessionHistoryEntry;
import com.example.cab302studyslice.Model.User;
import com.example.cab302studyslice.View.ViewManager;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HistoryController {
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    private void onBackClick() {
        ViewManager.switchScene("dashboard-view.fxml");
    }

    @FXML
    private Label totalHoursLabel;

    @FXML
    private Label totalSessionsLabel;

    @FXML
    private Label stateMessageLabel;

    @FXML
    private TilePane historyContainer;

    @FXML
    public void initialize() {
        loadHistoryFromDatabase();
    }

    private void loadHistoryFromDatabase() {
        historyContainer.getChildren().clear();
        int currentUserId = User.getCurrentUserId();

        if (currentUserId <= 0) {
            totalHoursLabel.setText("00:00:00");
            totalSessionsLabel.setText("0");
            stateMessageLabel.setText("Please log in to load your history.");
            addInfoCard("No user logged in", "Return to login and sign in to view saved sessions.");
            return;
        }

        List<SessionHistoryEntry> sessions = DatabaseManager.getSessionHistoryByUserId(currentUserId);
        int totalSeconds = 0;
        for (SessionHistoryEntry session : sessions) {
            totalSeconds += session.getTotalSeconds();
        }

        totalHoursLabel.setText(formatSeconds(totalSeconds));
        totalSessionsLabel.setText(String.valueOf(sessions.size()));

        if (sessions.isEmpty()) {
            stateMessageLabel.setText("No saved sessions yet.");
            addInfoCard("No sessions yet", "Start tracking on the dashboard and save a session to see it here.");
            return;
        }

        stateMessageLabel.setText("Showing your latest saved sessions from SQL.");
        for (SessionHistoryEntry session : sessions) {
            addSessionCard(session);
        }
    }

    private void addInfoCard(String title, String details) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(360);
        card.setMinHeight(150);
        card.getStyleClass().addAll("dashboard-mini-card", "history-session-card");

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("history-session-title");

        Label detailsLabel = new Label(details);
        detailsLabel.getStyleClass().add("history-session-activities");
        detailsLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, detailsLabel);
        historyContainer.getChildren().add(card);
    }

    private void addSessionCard(SessionHistoryEntry session) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(360);
        card.setMinHeight(200);
        card.getStyleClass().addAll("dashboard-mini-card", "history-session-card");

        Label titleLabel = new Label(session.getTitle().isBlank() ? "Untitled Session" : session.getTitle());
        titleLabel.getStyleClass().add("history-session-title");
        titleLabel.setWrapText(true);

        Label timeLabel = new Label("Total Time: " + session.getFormattedTotalTime());
        timeLabel.getStyleClass().add("history-session-time");

        Label dateLabel = new Label(formatSessionRange(session.getStartTime(), session.getEndTime()));
        dateLabel.getStyleClass().add("history-session-meta");
        dateLabel.setWrapText(true);

        Label activitiesLabel = new Label(buildActivityPreview(session.getActivities()));
        activitiesLabel.getStyleClass().add("history-session-activities");
        activitiesLabel.setWrapText(true);

        card.getChildren().addAll(titleLabel, timeLabel, dateLabel, activitiesLabel);
        historyContainer.getChildren().add(card);
    }

    private String buildActivityPreview(List<Activity> activities) {
        if (activities == null || activities.isEmpty()) {
            return "No activities recorded.";
        }

        List<Activity> sorted = new ArrayList<>(activities);
        sorted.sort(Comparator.comparingInt(Activity::getDuration).reversed());

        int limit = Math.min(4, sorted.size());
        StringBuilder builder = new StringBuilder("Top activities:\n");
        for (int i = 0; i < limit; i++) {
            Activity activity = sorted.get(i);
            builder.append("• ")
                    .append(activity.getAppName())
                    .append(" — ")
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

    private String formatSessionRange(LocalDateTime start, LocalDateTime end) {
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

    private String formatSeconds(int totalSeconds) {
        int safeSeconds = Math.max(0, totalSeconds);
        int hours = safeSeconds / 3600;
        int minutes = (safeSeconds % 3600) / 60;
        int seconds = safeSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.*;
import com.example.cab302studyslice.View.ViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;

import java.util.List;

//Controller for the History page
//loads saved study sessions for the currently logged-in user
//displays them as cards in the UI
public class HistoryController {
    private final HistoryFormatter historyFormatter = new HistoryFormatter();

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

    //Loads the current user's saved sessions from the database
    //Updates the total study time, session count, and history cards.
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

        totalHoursLabel.setText(historyFormatter.formatSeconds(totalSeconds));
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

    //Adds a placeholder/info card when there is no history to display
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

    //Creates aa visual card for one save study session
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

        Label dateLabel = new Label(historyFormatter.formatSessionRange(session.getStartTime(), session.getEndTime()));
        dateLabel.getStyleClass().add("history-session-meta");
        dateLabel.setWrapText(true);

        Label activitiesLabel = new Label(historyFormatter.buildActivityPreview(session.getActivities()));
        activitiesLabel.getStyleClass().add("history-session-activities");
        activitiesLabel.setWrapText(true);

        Button aiButton = new Button("Ask AI");
        aiButton.getStyleClass().add("dashboard-secondary-button");
        aiButton.setOnAction(e -> {
            aiButton.setText("Loading...");
            aiButton.setDisable(true);
            List<SessionHistoryEntry> allSessions = DatabaseManager.getSessionHistoryByUserId(User.getCurrentUserId());
            new Thread(() -> {
                AiAPI.WrappedData data = AiAPI.analyzeSessionStructured(session, allSessions);
                Platform.runLater(() -> {
                    aiButton.setText("Ask AI");
                    aiButton.setDisable(false);

                    if (data == null) {
                        Alert err = new Alert(Alert.AlertType.ERROR);
                        err.setTitle("AI Error");
                        err.setHeaderText("Could not get AI analysis. Please try again.");
                        err.showAndWait();
                        return;
                    }

                    String display =
                            "Score: " + data.score + "/100\n" +
                            "Ranking: " + data.ranking + "/" + data.totalSessions + "\n" +
                            "Record Total Time: " + (data.recordTotalTime ? "Yes" : "No") + "\n" +
                            "Most Used App: " + data.mostUsedApp + "\n" +
                            "Bad Habit: " + data.badHabit + "\n" +
                            "Compared to Sessions: " + data.comparedToSessions + "\n" +
                            "Current Streak: " + data.streakCurrent;

                    TextArea textArea = new TextArea(display);
                    textArea.setEditable(false);
                    textArea.setWrapText(true);
                    textArea.setPrefWidth(500);
                    textArea.setPrefHeight(200);

                    Button saveButton = new Button("Save to Database");
                    saveButton.getStyleClass().add("dashboard-primary-button");
                    saveButton.setOnAction(ev -> {
                        saveButton.setText("Saving...");
                        saveButton.setDisable(true);
                        new Thread(() -> {
                            boolean saved = DatabaseManager.insertWrappedData(
                                    session.getSessionId(),
                                    data.recordTotalTime,
                                    data.mostUsedApp,
                                    data.ranking,
                                    data.badHabit,
                                    data.comparedToSessions,
                                    data.streakCurrent,
                                    data.score
                            );
                            Platform.runLater(() -> {
                                saveButton.setText(saved ? "Saved!" : "Failed - try again");
                                saveButton.setDisable(saved);
                            });
                        }).start();
                    });

                    VBox content = new VBox(10, textArea, saveButton);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("AI Analysis - " + session.getTitle());
                    alert.setHeaderText("Session Analysis");
                    alert.getDialogPane().setContent(content);
                    alert.getDialogPane().setPrefWidth(550);
                    alert.showAndWait();
                });
            }).start();
        });

        card.getChildren().addAll(titleLabel, timeLabel, dateLabel, activitiesLabel, aiButton);
        historyContainer.getChildren().add(card);
    }
}

package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.*;
import com.example.cab302studyslice.View.ViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.util.Comparator;
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

    //Creates a visual card for one saved study session
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

        Button unwrapButton = new Button("Unwrap");
        unwrapButton.getStyleClass().add("dashboard-primary-button");
        unwrapButton.setOnAction(e -> launchUnwrap(session, unwrapButton));

        Button deleteButton = new Button("Delete");
        deleteButton.getStyleClass().add("dashboard-secondary-button");
        deleteButton.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;");
        deleteButton.setOnAction(e -> confirmDeleteSession(session));

        HBox actions = new HBox(8, unwrapButton, deleteButton);
        actions.setAlignment(Pos.CENTER_LEFT);

        card.getChildren().addAll(titleLabel, timeLabel, dateLabel, activitiesLabel, actions);
        historyContainer.getChildren().add(card);
    }

    /**
     * Fetches (or generates) wrapped data for the session, stores it in
     * WrappedDataHolder, then launches the animation sequence.
     */
    private void launchUnwrap(SessionHistoryEntry session, Button unwrapButton) {
        unwrapButton.setText("Loading...");
        unwrapButton.setDisable(true);

        int userId = User.getCurrentUserId();

        new Thread(() -> {
            List<SessionHistoryEntry> allSessions =
                    DatabaseManager.getSessionHistoryByUserId(userId);

            // 1. Try the DB first
            AiAPI.WrappedData data =
                    DatabaseManager.getWrappedDataForSession(session.getSessionId());

            // 2. If not cached yet, ask the AI and save it
            if (data == null) {
                data = AiAPI.analyzeSessionStructured(session, allSessions);
                if (data != null) {
                    DatabaseManager.insertWrappedData(
                            session.getSessionId(),
                            data.recordTotalTime,
                            data.mostUsedApp,
                            data.ranking,
                            data.badHabit,
                            data.comparedToSessions,
                            data.streakCurrent,
                            data.score
                    );
                }
            }

            // 3. Always set totalSessions from the live list
            if (data != null) {
                data.totalSessions = allSessions.size();
            }

            String goodHabit = deriveGoodHabit(session);
            final AiAPI.WrappedData finalData = data;

            Platform.runLater(() -> {
                unwrapButton.setText("Unwrap");
                unwrapButton.setDisable(false);

                if (finalData == null) {
                    Alert err = new Alert(Alert.AlertType.ERROR);
                    err.setTitle("Unwrap Error");
                    err.setHeaderText("Could not generate session analysis. Please try again.");
                    err.showAndWait();
                    return;
                }

                WrappedDataHolder.set(finalData, session, goodHabit);
                ViewManager.switchScene("wrapped-intro-view.fxml");
            });
        }).start();
    }

    /**
     * Shows an UNDECORATED confirmation popup before permanently deleting a session.
     */
    private void confirmDeleteSession(SessionHistoryEntry session) {
        Stage owner = (historyContainer != null && historyContainer.getScene() != null)
                ? (Stage) historyContainer.getScene().getWindow() : null;

        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) dialog.initOwner(owner);
        dialog.setResizable(false);

        Label heading = new Label("Delete Session");
        heading.getStyleClass().add("dashboard-card-label");

        String name = session.getTitle().isBlank() ? "Untitled Session" : session.getTitle();
        Label body = new Label("Are you sure you want to delete \"" + name
                + "\"?\nThis will also remove its activity log and any Unwrap data. This cannot be undone.");
        body.setWrapText(true);
        body.getStyleClass().add("dashboard-helper-text");
        body.setMaxWidth(320);

        Button confirmBtn = new Button("Delete");
        confirmBtn.getStyleClass().add("dashboard-primary-button");
        confirmBtn.setStyle("-fx-background-color: #C0392B; -fx-text-fill: white;");
        confirmBtn.setOnAction(e -> {
            dialog.close();
            new Thread(() -> {
                boolean deleted = DatabaseManager.deleteSession(session.getSessionId());
                Platform.runLater(() -> {
                    if (deleted) {
                        loadHistoryFromDatabase();
                    } else {
                        Alert err = new Alert(Alert.AlertType.ERROR);
                        err.setTitle("Error");
                        err.setHeaderText("Could not delete the session. Please try again.");
                        err.showAndWait();
                    }
                });
            }).start();
        });

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("dashboard-secondary-button");
        cancelBtn.setOnAction(e -> dialog.close());

        HBox buttons = new HBox(10, confirmBtn, cancelBtn);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(16, heading, body, buttons);
        root.getStyleClass().add("dashboard-card");
        root.setStyle("-fx-background-radius: 16; -fx-border-radius: 16;");
        root.setPadding(new Insets(24));
        root.setPrefWidth(380);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        scene.setFill(null);
        scene.getStylesheets().add(
                getClass().getResource("/com/example/cab302studyslice/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * Derives a "good habit" sentence from the session's activity breakdown.
     * Used on the Wrapped good-habit slide (not stored in DB).
     */
    private String deriveGoodHabit(SessionHistoryEntry session) {
        if (session.getActivities().isEmpty()) {
            return "Staying committed to your study goals";
        }
        Activity top = session.getActivities().stream()
                .max(Comparator.comparingInt(Activity::getDuration))
                .orElse(null);
        if (top == null) return "Consistent focus throughout the session";

        int totalSecs = session.getTotalSeconds();
        double pct = totalSecs > 0 ? (100.0 * top.getDuration() / totalSecs) : 0;
        if (pct > 50) {
            return "Deep focus on " + top.getAppName();
        }
        return "Balanced workflow across multiple apps";
    }
}

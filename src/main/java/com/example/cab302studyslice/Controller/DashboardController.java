package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.TrackingEngine;
import com.example.cab302studyslice.View.ViewManager;
import com.example.cab302studyslice.Model.AiAPI;
import com.example.cab302studyslice.Model.Activity;
import com.example.cab302studyslice.Model.HistoryStore;
import com.example.cab302studyslice.Model.DatabaseManager;
import com.example.cab302studyslice.Model.SessionHistoryEntry;
import com.example.cab302studyslice.Model.SessionSaveService;
import com.example.cab302studyslice.Model.User;
import com.example.cab302studyslice.Model.WrappedDataHolder;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;


// Controller to connect the dashboard ui with the actual program features and Java file.
//Handles starting/stopping study sessions
public class DashboardController {

    // Tracking UI elements
    @FXML private Label timerLabel;
    @FXML private TextArea statusTextArea;
    @FXML private Button toggleButton;
    @FXML private Button saveButton;
    @FXML private Button deleteButton;

    // Quick stats labels
    @FXML private Label statTodayTime;
    @FXML private Label statSessions;
    @FXML private Label statStreak;
    @FXML private Label statBestSession;

    // Session snapshot labels
    @FXML private Label snapMostUsedApp;
    @FXML private Label snapSecondApp;
    @FXML private Label snapProductivity;

    // Static engine to persist tracking across scene switches
    private static TrackingEngine engine = new TrackingEngine();
    private static boolean isTracking = false;
    private final SessionSaveService sessionSaveService = new SessionSaveService();

    private void loadQuickStats() {
        int userId = User.getCurrentUserId();
        if (userId <= 0) return;

        List<SessionHistoryEntry> sessions = DatabaseManager.getSessionHistoryByUserId(userId);

        LocalDate today = LocalDate.now();
        int todaySeconds = 0;
        int bestSeconds = 0;
        Set<LocalDate> sessionDates = new TreeSet<>();

        for (SessionHistoryEntry s : sessions) {
            int secs = s.getTotalSeconds();
            if (secs > bestSeconds) bestSeconds = secs;

            LocalDateTime anchor = s.getStartTime() != null ? s.getStartTime() : s.getEndTime();
            if (anchor != null) {
                LocalDate date = anchor.toLocalDate();
                sessionDates.add(date);
                if (date.equals(today)) todaySeconds += secs;
            }
        }

        int streak = 0;
        LocalDate check = today;
        while (sessionDates.contains(check)) { streak++; check = check.minusDays(1); }
        if (streak == 0) {
            check = today.minusDays(1);
            while (sessionDates.contains(check)) { streak++; check = check.minusDays(1); }
        }

        statTodayTime.setText(formatShortTime(todaySeconds));
        statSessions.setText(String.valueOf(sessions.size()));
        statStreak.setText(streak + (streak == 1 ? " day" : " days"));
        statBestSession.setText(formatShortTime(bestSeconds));

        // Session snapshot
        List<String> topApps = DatabaseManager.getTopApps(userId, 2);
        snapMostUsedApp.setText(topApps.size() > 0 ? topApps.get(0) : "-");
        snapSecondApp.setText(topApps.size() > 1 ? topApps.get(1) : "-");
        int score = DatabaseManager.getLatestWrappedScore(userId);
        snapProductivity.setText(score >= 0 ? score + "/100" : "-");
    }

    private String formatShortTime(int totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        return hours + "h " + String.format("%02d", minutes) + "m";
    }

    @FXML
    public void initialize() {
        loadQuickStats();

        // Connect the Engine to the UI
        engine.setUiUpdater(text -> {
            Platform.runLater(() -> {
                if (text.contains("\n")) {
                    String[] parts = text.split("\n", 2);
                    if (timerLabel != null) {
                        timerLabel.setText(parts[0]);
                    }
                    if (statusTextArea != null) {
                        statusTextArea.setText(parts[1]);
                    }
                }
                updateSessionButtons();
            });
        });

        // Sync button text if tracking is already running
        if (isTracking) {
            toggleButton.setText("Stop Tracking");
        }
    }
    private void updateSessionButtons() {
        boolean hasData = hasSessionData();
        if (saveButton != null) saveButton.setDisable(!hasData);
        if (deleteButton != null) deleteButton.setDisable(!hasData);
    }

    //Start or stop tracking depending on the current tracking state
    @FXML
    private void handleToggleTracking() {
        if (!isTracking) {
            engine.startTracking();
            toggleButton.setText("Stop Tracking");
            isTracking = true;
        } else {
            engine.stopTracking();
            toggleButton.setText("Start Tracking");
            isTracking = false;
            updateSessionButtons();
        }
    }


    //Will format the completed tracking session into text for the history page
    private String buildSessionText(String sessionName) {
        StringBuilder builder = new StringBuilder();
        builder.append("Session Name: ")
                .append(sessionName)
                .append("\n");

        builder.append("Session Date: ")
                .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
                .append("\n");

        builder.append("Total Study Time: ")
                .append(formatTime(engine.getTotalSeconds()))
                .append("\n\n");

        for (Map.Entry<String, Integer> entry : engine.getTimeSpent().entrySet()) {
            builder.append(entry.getKey())
                    .append(" : ")
                    .append(formatTime(entry.getValue()))
                    .append("\n");
        }

        return builder.toString();
    }

    //Converts seconds into HH:MM:SS format
    private String formatTime(int totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private boolean hasSessionData() {
        return engine.getTotalSeconds() > 0 && !engine.getTimeSpent().isEmpty();
    }

    private void showNoSessionDataPopup() {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (toggleButton != null && toggleButton.getScene() != null) {
            dialog.initOwner(toggleButton.getScene().getWindow());
        }
        dialog.setResizable(false);

        Label errorLabel = new Label("Error: No Session Data");
        errorLabel.getStyleClass().add("dashboard-card-label");

        Button okButton = new Button("OK");
        okButton.getStyleClass().add("dashboard-primary-button");
        okButton.setOnAction(event -> dialog.close());

        VBox root = new VBox(12, errorLabel, okButton);
        root.getStyleClass().add("dashboard-card");
        root.setStyle("-fx-background-radius: 16; -fx-border-radius: 16;");
        root.setPadding(new Insets(20));
        root.setPrefWidth(300);

        Scene scene = new Scene(root);
        scene.setFill(null);
        scene.getStylesheets().add(getClass().getResource("/com/example/cab302studyslice/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    //Save the current session after validating title, user, and tracked data
    @FXML
    private void handleSaveSession() {
        if (!hasSessionData()) return;
        if (isTracking) {
            engine.stopTracking();
            toggleButton.setText("Start Tracking");
            isTracking = false;
        }
        // Capture the main window before opening the modal dialog
        final Stage mainStage = (toggleButton != null && toggleButton.getScene() != null)
                ? (Stage) toggleButton.getScene().getWindow() : null;

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (mainStage != null) dialog.initOwner(mainStage);
        dialog.setTitle("Save Session");
        dialog.setResizable(false);

        Label titleLabel = new Label("Save Session");
        titleLabel.getStyleClass().add("dashboard-card-label");

        Label helperLabel = new Label("Enter a session name to save this study session.");
        helperLabel.setWrapText(true);
        helperLabel.getStyleClass().add("dashboard-helper-text");

        TextField sessionNameField = new TextField();
        sessionNameField.setPromptText("Session name");
        sessionNameField.getStyleClass().add("auth-field");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("auth-message");

        Button confirmButton = new Button("Confirm");
        confirmButton.getStyleClass().add("dashboard-primary-button");
        confirmButton.setOnAction(event -> {
            String sessionName = sessionNameField.getText().trim();
            SessionSaveService.PrepareResult preparedSession = sessionSaveService.prepareSaveRequest(
                    sessionName,
                    User.getCurrentUserId(),
                    engine.getTotalSeconds(),
                    engine.getTimeSpent(),
                    LocalDateTime.now()
            );

            if (!preparedSession.isReady()) {
                messageLabel.setStyle("-fx-text-fill: #7B4141;");
                messageLabel.setText(preparedSession.message());
                return;
            }

            SessionSaveService.SaveRequest saveRequest = preparedSession.request();

            boolean saved = DatabaseManager.saveFullSession(
                    User.getCurrentUserId(),
                    saveRequest.title(),
                    saveRequest.startTime(),
                    saveRequest.endTime(),
                    saveRequest.totalSeconds(),
                    saveRequest.activities()
            );

            if (!saved) {
                messageLabel.setStyle("-fx-text-fill: #7B4141;");
                messageLabel.setText("Failed to save session to database.");
                return;
            }

            HistoryStore.addSession(buildSessionText(sessionName));
            resetCurrentSession();
            dialog.close();
            loadQuickStats();

            final int userId = User.getCurrentUserId();
            new Thread(() -> {
                int latestId = DatabaseManager.getLatestSessionId(userId);
                if (latestId <= 0) return;
                List<SessionHistoryEntry> allSessions = DatabaseManager.getSessionHistoryByUserId(userId);
                SessionHistoryEntry newest = allSessions.stream()
                        .filter(s -> s.getSessionId() == latestId)
                        .findFirst().orElse(null);
                if (newest == null) return;
                AiAPI.WrappedData data = AiAPI.analyzeSessionStructured(newest, allSessions);
                if (data != null) {
                    data.totalSessions = allSessions.size();
                    DatabaseManager.insertWrappedData(
                            newest.getSessionId(),
                            data.recordTotalTime,
                            data.mostUsedApp,
                            data.ranking,
                            data.badHabit,
                            data.comparedToSessions,
                            data.streakCurrent,
                            data.score
                    );
                    final AiAPI.WrappedData finalData = data;
                    final SessionHistoryEntry finalSession = newest;
                    Platform.runLater(() -> showUnwrapPrompt(finalSession, finalData, mainStage));
                }
            }).start();
        });

        VBox root = new VBox(12, titleLabel, helperLabel, sessionNameField, messageLabel, confirmButton);
        root.getStyleClass().add("dashboard-card");
        root.setPadding(new Insets(20));
        root.setPrefWidth(360);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/cab302studyslice/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @FXML
    private void handleDeleteSession() {
        if (!hasSessionData()) return;
        if (isTracking) {
            engine.stopTracking();
            toggleButton.setText("Start Tracking");
            isTracking = false;
        }
        resetCurrentSession();
    }

    //Reset the current unsaved session data from the dashboard
    private void resetCurrentSession() {
        engine.stopTracking();
        engine.reset();

        if (timerLabel != null) timerLabel.setText("Total Study Time: 00:00:00");
        if (statusTextArea != null) statusTextArea.clear();

        toggleButton.setText("Start Tracking");
        isTracking = false;
        updateSessionButtons();
    }

    // -----------------------------
    // NAVIGATION BUTTONS
    // -----------------------------

    //Open the custom timer popup and start countdown tracking
    @FXML
    private void onTimerClicked() {
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (toggleButton != null && toggleButton.getScene() != null) {
            dialog.initOwner(toggleButton.getScene().getWindow());
        }
        dialog.setTitle("Set Timer");
        dialog.setResizable(false);

        Label titleLabel = new Label("Set Timer");
        titleLabel.getStyleClass().add("dashboard-card-label");

        Label helperLabel = new Label("Enter hours, minutes, and seconds to start a countdown.");
        helperLabel.setWrapText(true);
        helperLabel.getStyleClass().add("dashboard-helper-text");

        TextField hoursField = createTimerField("HH");
        TextField minutesField = createTimerField("MM");
        TextField secondsField = createTimerField("SS");

        Label colonOne = new Label(":");
        colonOne.getStyleClass().add("dashboard-card-label");
        Label colonTwo = new Label(":");
        colonTwo.getStyleClass().add("dashboard-card-label");

        HBox inputRow = new HBox(8, hoursField, colonOne, minutesField, colonTwo, secondsField);
        inputRow.setAlignment(Pos.CENTER);

        Label countdownLabel = new Label("00:00:00");
        countdownLabel.getStyleClass().add("dashboard-mini-value");

        Label messageLabel = new Label();
        messageLabel.getStyleClass().add("auth-message");

        Button startButton = new Button("Start");
        startButton.getStyleClass().add("dashboard-primary-button");

        Button stopButton = new Button("Stop");
        stopButton.getStyleClass().add("dashboard-secondary-button");
        stopButton.setDisable(true);

        Button closeButton = new Button("Close");
        closeButton.getStyleClass().add("dashboard-secondary-button");

        final Timeline[] countdownTimeline = new Timeline[1];
        final int[] remainingSeconds = new int[1];

        startButton.setOnAction(event -> {
            Integer hours = parseTimerField(hoursField.getText(), "Hours", 99, messageLabel);
            Integer minutes = parseTimerField(minutesField.getText(), "Minutes", 59, messageLabel);
            Integer seconds = parseTimerField(secondsField.getText(), "Seconds", 59, messageLabel);

            if (hours == null || minutes == null || seconds == null) {
                return;
            }

            int totalSeconds = (hours * 3600) + (minutes * 60) + seconds;
            if (totalSeconds <= 0) {
                messageLabel.setStyle("-fx-text-fill: #7B4141;");
                messageLabel.setText("Please set a timer greater than 0 seconds.");
                return;
            }

            if (countdownTimeline[0] != null) {
                countdownTimeline[0].stop();
            }

            remainingSeconds[0] = totalSeconds;
            countdownLabel.setText(formatTime(remainingSeconds[0]));
            messageLabel.setStyle("-fx-text-fill: #657972;");
            messageLabel.setText("Timer is running...");
            startButton.setDisable(true);
            stopButton.setDisable(false);

            countdownTimeline[0] = new Timeline(new KeyFrame(Duration.seconds(1), tick -> {
                remainingSeconds[0]--;
                countdownLabel.setText(formatTime(Math.max(remainingSeconds[0], 0)));

                if (remainingSeconds[0] <= 0) {
                    countdownTimeline[0].stop();
                    countdownTimeline[0] = null;
                    startButton.setDisable(false);
                    stopButton.setDisable(true);
                    messageLabel.setStyle("-fx-text-fill: #657972;");
                    messageLabel.setText("Timer complete.");
                    showTimerCompletePopup(dialog);
                }
            }));
            countdownTimeline[0].setCycleCount(Timeline.INDEFINITE);
            countdownTimeline[0].play();
        });

        stopButton.setOnAction(event -> {
            if (countdownTimeline[0] != null) {
                countdownTimeline[0].stop();
                countdownTimeline[0] = null;
            }
            startButton.setDisable(false);
            stopButton.setDisable(true);
            messageLabel.setStyle("-fx-text-fill: #657972;");
            messageLabel.setText("Timer stopped.");
        });

        closeButton.setOnAction(event -> dialog.close());
        dialog.setOnHidden(event -> {
            if (countdownTimeline[0] != null) {
                countdownTimeline[0].stop();
            }
        });

        HBox actionRow = new HBox(10, startButton, stopButton, closeButton);
        actionRow.setAlignment(Pos.CENTER);

        VBox root = new VBox(12, titleLabel, helperLabel, inputRow, countdownLabel, messageLabel, actionRow);
        root.getStyleClass().add("dashboard-card");
        root.setPadding(new Insets(20));
        root.setPrefWidth(380);
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/cab302studyslice/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Unwrap prompt — shown after AI analysis completes in the background
    // ──────────────────────────────────────────────────────────────────────────

    private void showUnwrapPrompt(SessionHistoryEntry session, AiAPI.WrappedData data, Stage owner) {
        Stage prompt = new Stage();
        prompt.initStyle(StageStyle.TRANSPARENT);
        if (owner != null) {
            prompt.initModality(Modality.APPLICATION_MODAL);
            prompt.initOwner(owner);
        }
        prompt.setResizable(false);

        Label heading = new Label("Your session has been analysed!");
        heading.getStyleClass().add("dashboard-card-label");

        Label body = new Label("Want to see how \"" + session.getTitle()
                + "\" stacked up? Unwrap your results now.");
        body.setWrapText(true);
        body.getStyleClass().add("dashboard-helper-text");
        body.setMaxWidth(320);

        Button unwrapBtn = new Button("Unwrap");
        unwrapBtn.getStyleClass().add("dashboard-primary-button");
        unwrapBtn.setOnAction(e -> {
            prompt.close();
            WrappedDataHolder.set(data, session, deriveGoodHabit(session));
            ViewManager.switchScene("wrapped-intro-view.fxml");
        });

        Button laterBtn = new Button("Maybe Later");
        laterBtn.getStyleClass().add("dashboard-secondary-button");
        laterBtn.setOnAction(e -> prompt.close());

        HBox buttons = new HBox(10, unwrapBtn, laterBtn);
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
        prompt.setScene(scene);
        prompt.show();
    }

    private String deriveGoodHabit(SessionHistoryEntry session) {
        if (session.getActivities().isEmpty()) return "Staying committed to your study goals";
        Activity top = session.getActivities().stream()
                .max(java.util.Comparator.comparingInt(Activity::getDuration))
                .orElse(null);
        if (top == null) return "Consistent focus throughout the session";
        int totalSecs = session.getTotalSeconds();
        double pct = totalSecs > 0 ? (100.0 * top.getDuration() / totalSecs) : 0;
        return pct > 50 ? "Deep focus on " + top.getAppName() : "Balanced workflow across multiple apps";
    }

    @FXML
    private void onTrackingClicked() {
        ViewManager.switchScene("timer-view.fxml");
    }

    @FXML
    private void onExploreClicked() {
        ViewManager.switchScene("history-view.fxml");
    }

    private TextField createTimerField(String promptText) {
        TextField timerField = new TextField();
        timerField.setPromptText(promptText);
        timerField.getStyleClass().add("auth-field");
        timerField.setPrefWidth(80);
        timerField.setMaxWidth(80);
        return timerField;
    }

    private Integer parseTimerField(String value, String label, int maxValue, Label messageLabel) {
        String normalizedValue = value == null ? "" : value.trim();
        if (normalizedValue.isEmpty()) {
            return 0;
        }

        try {
            int parsedValue = Integer.parseInt(normalizedValue);
            if (parsedValue < 0 || parsedValue > maxValue) {
                messageLabel.setStyle("-fx-text-fill: #7B4141;");
                messageLabel.setText(label + " must be between 0 and " + maxValue + ".");
                return null;
            }
            return parsedValue;
        } catch (NumberFormatException exception) {
            messageLabel.setStyle("-fx-text-fill: #7B4141;");
            messageLabel.setText(label + " must be a valid number.");
            return null;
        }
    }

    private void showTimerCompletePopup(Stage owner) {
        Stage dialog = new Stage();
        dialog.initStyle(StageStyle.TRANSPARENT);
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setResizable(false);

        Label infoLabel = new Label("Time is up!");
        infoLabel.getStyleClass().add("dashboard-card-label");

        Button okButton = new Button("OK");
        okButton.getStyleClass().add("dashboard-primary-button");
        okButton.setOnAction(event -> dialog.close());

        VBox root = new VBox(12, infoLabel, okButton);
        root.getStyleClass().add("dashboard-card");
        root.setStyle("-fx-background-radius: 16; -fx-border-radius: 16;");
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(260);

        Scene scene = new Scene(root);
        scene.setFill(null);
        scene.getStylesheets().add(getClass().getResource("/com/example/cab302studyslice/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    //for timer page - adjusting
    @FXML private Label setTimerDisplay;
    @FXML private Label liveTimerDisplay;

    @FXML
    private void goToDashboard() {
        switchScene("/com/example/cab302studyslice/dashboard-view.fxml");
    }

    @FXML
    private void goToExplore() {
        switchScene("/com/example/cab302studyslice/explore-view.fxml");
    }


    private void switchScene(String fxmlPath) {
        try {
            Stage stage = (Stage) setTimerDisplay.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxmlPath)));
            stage.setScene(scene);
        } catch (IOException e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }

}

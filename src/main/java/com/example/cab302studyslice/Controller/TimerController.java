package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.*;
import com.example.cab302studyslice.View.ViewManager;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TimerController {

    // ===== UI ELEMENTS =====
    @FXML private Label setTimerDisplay;
    @FXML private Label liveTimerDisplay;

    // ===== TIMER STATE =====
    private long liveStartTime;
    private long setTimerDurationSeconds = 9000; // default 2:30:00
    private long setTimerStartTime;

    private AnimationTimer liveTimerLoop;
    private AnimationTimer setTimerLoop;

    // ===== TRACKING ENGINE =====
    private final TrackingEngine trackingEngine = new TrackingEngine();


    // ============================================================
    //                      NAVIGATION
    // ============================================================

    @FXML
    private void goToDashboard() {
        switchScene("/com/example/cab302studyslice/dashboard-view.fxml");
    }

    @FXML
    private void onExploreClicked() {
        switchScene("/com/example/cab302studyslice/history-view.fxml");
    }


    //
    @FXML private Label timerLabel;
    @FXML private TextArea statusTextArea;
    @FXML private Button toggleButton;

    // Static engine to persist tracking across scene switches
    private static TrackingEngine engine = new TrackingEngine();
    private static boolean isTracking = false;

    @FXML
    public void initialize() {
        // Connect the Engine to the UI
        engine.setUiUpdater(text -> {
            Platform.runLater(() -> {
                if (text.contains("\n")) {
                    String[] parts = text.split("\n", 2);
                    if (timerLabel != null) {
                        timerLabel.setText(parts[0]); // First line is the total time
                    }
                    if (statusTextArea != null) {
                        statusTextArea.setText(parts[1]); // Remaining lines are the activity log
                    }
                }
            });
        });

        // Sync button text if tracking is already running
        if (isTracking) {
            toggleButton.setText("Stop Tracking");
        }
    }

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
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (toggleButton != null && toggleButton.getScene() != null) {
            dialog.initOwner(toggleButton.getScene().getWindow());
        }
        dialog.setTitle("Error");
        dialog.setResizable(false);

        Label errorLabel = new Label("Error: No Session Data");
        errorLabel.getStyleClass().add("dashboard-card-label");

        Button okButton = new Button("OK");
        okButton.getStyleClass().add("dashboard-primary-button");
        okButton.setOnAction(event -> dialog.close());

        VBox root = new VBox(12, errorLabel, okButton);
        root.getStyleClass().add("dashboard-card");
        root.setPadding(new Insets(20));
        root.setPrefWidth(300);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/cab302studyslice/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }
    @FXML
    private void handleSaveSession() {
        if (!hasSessionData()) {
            showNoSessionDataPopup();
            return;
        }
        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (toggleButton != null && toggleButton.getScene() != null) {
            dialog.initOwner(toggleButton.getScene().getWindow());
        }
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
            if (sessionName.isEmpty()) {
                messageLabel.setStyle("-fx-text-fill: #7B4141;");
                messageLabel.setText("Please enter a session name.");
                return;
            }
            int currentUserId = User.getCurrentUserId();
            if (currentUserId <= 0) {
                messageLabel.setStyle("-fx-text-fill: #7B4141;");
                messageLabel.setText("Please log in again before saving.");
                return;
            }

            LocalDateTime endTime = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusSeconds(engine.getTotalSeconds());
            int totalTime = engine.getTotalSeconds();

            List<Activity> activities = new ArrayList<>();
            for (Map.Entry<String, Integer> entry : engine.getTimeSpent().entrySet()) {
                activities.add(new Activity(entry.getKey(), entry.getValue()));
            }

            boolean saved = DatabaseManager.saveFullSession(
                    currentUserId,
                    sessionName,
                    startTime,
                    endTime,
                    totalTime,
                    activities
            );

            if (!saved) {
                messageLabel.setStyle("-fx-text-fill: #7B4141;");
                messageLabel.setText("Failed to save session to database.");
                return;
            }

            HistoryStore.addSession(buildSessionText(sessionName));
            resetCurrentSession();
            dialog.close();
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
        if (!hasSessionData()) {
            showNoSessionDataPopup();
            return;
        }
        resetCurrentSession();
    }

    private void resetCurrentSession() {
        engine.stopTracking();
        engine.reset();

        if (timerLabel != null) {
            timerLabel.setText("Total Study Time: 00:00:00");
        }
        if (statusTextArea != null) {
            statusTextArea.clear();
        }

        toggleButton.setText("Start Tracking");
        isTracking = false;
    }
//
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
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (owner != null) {
            dialog.initOwner(owner);
        }
        dialog.setTitle("Timer Complete");
        dialog.setResizable(false);

        Label infoLabel = new Label("Time is up!");
        infoLabel.getStyleClass().add("dashboard-card-label");

        Button okButton = new Button("OK");
        okButton.getStyleClass().add("dashboard-primary-button");
        okButton.setOnAction(event -> dialog.close());

        VBox root = new VBox(12, infoLabel, okButton);
        root.getStyleClass().add("dashboard-card");
        root.setPadding(new Insets(20));
        root.setAlignment(Pos.CENTER);
        root.setPrefWidth(260);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/com/example/cab302studyslice/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    /// ///////////////////////

    private void switchScene(String fxmlPath) {
        try {
            Stage stage = (Stage) setTimerDisplay.getScene().getWindow();
            Scene scene = new Scene(FXMLLoader.load(getClass().getResource(fxmlPath)));
            stage.setScene(scene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    // ============================================================
    //                      SET TIMER MODE
    // ============================================================

    @FXML
    private void openSetTimerDialog() {
        Dialog<Long> dialog = new Dialog<>();
        dialog.setTitle("Set Timer");
        dialog.initModality(Modality.APPLICATION_MODAL);

        // Hour / minute / second selectors
        Spinner<Integer> hours = new Spinner<>(0, 23, 2);
        Spinner<Integer> minutes = new Spinner<>(0, 59, 30);
        Spinner<Integer> seconds = new Spinner<>(0, 59, 0);

        VBox box = new VBox(10,
                new Label("Hours:"), hours,
                new Label("Minutes:"), minutes,
                new Label("Seconds:"), seconds
        );
        box.setStyle("-fx-padding: 20;");

        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(button -> {
            if (button == ButtonType.OK) {
                return (long) hours.getValue() * 3600 +
                        (long) minutes.getValue() * 60 +
                        seconds.getValue();
            }
            return null;
        });

        dialog.showAndWait().ifPresent(resultSeconds -> {
            setTimerDurationSeconds = resultSeconds;
            setTimerDisplay.setText(formatTime(resultSeconds));
        });
    }


    // ============================================================
    //                      LIVE TIMER MODE
    // ============================================================

    //Start live stopwatch mode
    @FXML
    private void startLiveTimer() {
        stopAllTimers();

        liveStartTime = System.currentTimeMillis();

        // Start tracking apps
        trackingEngine.setUiUpdater(text -> {

            System.out.println(text);
        });
        trackingEngine.startTracking();

        liveTimerLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = (System.currentTimeMillis() - liveStartTime) / 1000;
                liveTimerDisplay.setText(formatTime(elapsed));
            }
        };

        liveTimerLoop.start();
    }


    // ============================================================
    //                      SET TIMER COUNTDOWN
    // ============================================================

    //Starts countdown mode using the user's selected time
    @FXML
    private void startSetTimer() {
        stopAllTimers();

        setTimerStartTime = System.currentTimeMillis();



        setTimerLoop = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long elapsed = (System.currentTimeMillis() - setTimerStartTime) / 1000;
                long remaining = setTimerDurationSeconds - elapsed;

                if (remaining <= 0) {
                    setTimerDisplay.setText("00:00:00");
                    stopAllTimers();
                    trackingEngine.stopTracking();
                    showTimerFinishedAlert();
                    return;
                }

                setTimerDisplay.setText(formatTime(remaining));
            }
        };

        setTimerLoop.start();
    }


    // ============================================================
    //                      UTILITIES
    // ============================================================

    //Stop both live and countdown timers safely
    private void stopAllTimers() {
        if (liveTimerLoop != null) liveTimerLoop.stop();
        if (setTimerLoop != null) setTimerLoop.stop();
        trackingEngine.stopTracking();
    }

    //Show an alert when the countdown timer finishes
    private void showTimerFinishedAlert() {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Timer Finished");
        alert.setHeaderText("Your study session has ended");
        alert.setContentText("Great job staying focused.");
        alert.show();
    }

    private String formatTime(long seconds) {
        long h = seconds / 3600;
        long m = (seconds % 3600) / 60;
        long s = seconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }
}
package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.TrackingEngine;
import com.example.cab302studyslice.View.ViewManager;
import com.example.cab302studyslice.Model.HistoryStore;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;



public class DashboardController {

    // Tracking UI elements
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

            //Save the completed session into HistoryStore.
            String sessionText = buildSessionText();
            System.out.println("Saving session to history...");
            System.out.println(sessionText);

            HistoryStore.addSession(sessionText);
            // Reset tracker so the next study session starts fresh
            engine.reset();

            // Clear dashboard display after session ends
            if (timerLabel != null) {
                timerLabel.setText("Total Study Time: 00:00:00");
            }
            if (statusTextArea != null) {
                statusTextArea.clear();
            }

            toggleButton.setText("Start Tracking");
            isTracking = false;

        }
    }


    //Will format the completed tracking session into text for the history page
    private String buildSessionText() {
        StringBuilder builder = new StringBuilder();

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

    // -----------------------------
    // NAVIGATION BUTTONS
    // -----------------------------
    @FXML
    private void onTimerClicked() {
        ViewManager.switchScene("timer-view.fxml");
    }

    @FXML
    private void onExploreClicked() {
        ViewManager.switchScene("history-view.fxml");
    }
}

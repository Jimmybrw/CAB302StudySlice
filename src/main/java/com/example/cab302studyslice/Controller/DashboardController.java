package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.TrackingEngine;
import com.example.cab302studyslice.View.ViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

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
            toggleButton.setText("Start Tracking");
            isTracking = false;
        }
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

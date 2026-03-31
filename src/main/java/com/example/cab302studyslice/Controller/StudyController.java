package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.TrackingEngine;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class StudyController {
    @FXML private TextArea statusTextArea;
    @FXML private Button toggleButton;
    @FXML private Label timerLabel;

    private static TrackingEngine engine = new TrackingEngine();
    private static boolean isTracking = false;

    @FXML
    public void initialize() {
        if (statusTextArea != null && timerLabel != null) {

            engine.setUiUpdater(data -> Platform.runLater(() -> {
                String[] parts = data.split("\n", 2);
                if (parts.length > 0) {
                    timerLabel.setText(parts[0]);
                }
                if (parts.length > 1) {
                    statusTextArea.setText(parts[1]);
                } else {
                    statusTextArea.setText("");
                }
            }));

            if (isTracking) {
                toggleButton.setText("Stop Tracking");
            }
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
}

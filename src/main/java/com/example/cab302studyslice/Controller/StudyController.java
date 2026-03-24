package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.TrackingEngine;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
/**
 * Practise comment to see commit
 * */

public class StudyController {
    @FXML private TextArea statusTextArea;
    @FXML private Button toggleButton;

    private static TrackingEngine engine = new TrackingEngine();
    private static boolean isTracking = false;

    @FXML
    public void initialize() {
        if (statusTextArea != null) {

            engine.setUiUpdater(data -> Platform.runLater(() -> statusTextArea.setText(data)));

            if (isTracking) {
                toggleButton.setText("Stop Tracking");
            }
        }
    }
// testing commit
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
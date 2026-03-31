package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.TrackingEngine;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

public class StudyController {
    @FXML private Button toggleButton;

    private static TrackingEngine engine = new TrackingEngine();
    private static boolean isTracking = false;

    @FXML
    public void initialize() {
        // The dashboard-view.fxml does not contain timerLabel or statusTextArea,
        // so the UI updater for the TrackingEngine is not set here.
        // If these elements are needed, they should be added to the FXML or
        // this controller should be used with a different FXML that contains them.

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
}

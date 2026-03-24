package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.TrackingEngine;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.util.Duration;

public class StudyController {
    @FXML private TextArea statusTextArea;
    @FXML private Button toggleButton;
    @FXML private Label timerLabel;

    private static TrackingEngine engine = new TrackingEngine();
    private static boolean isTracking = false;
    private static Timeline timeline;

    @FXML
    public void initialize() {
        if (statusTextArea != null) {
            engine.setUiUpdater(data -> Platform.runLater(() -> statusTextArea.setText(data)));
            if (isTracking) {
                toggleButton.setText("Stop Tracking");
            }
        }
        updateTimerLabel();
        if (isTracking) startTimerDisplay();
    }

    @FXML
    private void handleToggleTracking() {
        if (!isTracking) {
            engine.startTracking();
            toggleButton.setText("Stop Tracking");
            isTracking = true;
            startTimerDisplay();
        } else {
            engine.stopTracking();
            toggleButton.setText("Start Tracking");
            isTracking = false;
            if (timeline != null) { timeline.stop(); timeline = null; }
        }
    }

    private void startTimerDisplay() {
        if (timeline != null) timeline.stop();
        Label label = timerLabel;
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> updateTimerLabel(label)));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void updateTimerLabel() {
        updateTimerLabel(timerLabel);
    }

    private static void updateTimerLabel(Label label) {
        if (label == null) return;
        int total = engine.getTotalSeconds();
        int h = total / 3600;
        int m = (total % 3600) / 60;
        int s = total % 60;
        label.setText(String.format("Total Study Time: %02d:%02d:%02d", h, m, s));
    }
}

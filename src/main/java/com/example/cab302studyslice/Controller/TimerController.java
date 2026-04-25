package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.TrackingEngine;
import javafx.animation.AnimationTimer;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

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
    private void goToExplore() {
        switchScene("/com/example/cab302studyslice/explore-view.fxml");
    }

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

    private void stopAllTimers() {
        if (liveTimerLoop != null) liveTimerLoop.stop();
        if (setTimerLoop != null) setTimerLoop.stop();
        trackingEngine.stopTracking();
    }

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
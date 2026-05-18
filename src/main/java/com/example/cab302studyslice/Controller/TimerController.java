package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.*;
import com.example.cab302studyslice.View.ViewManager;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.ArcType;
import javafx.scene.shape.StrokeLineCap;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class TimerController {

    // ── FXML fields ──────────────────────────────────────────────
    @FXML private Canvas   timerCanvas;
    @FXML private Label    countdownLabel;
    @FXML private Label    timerStatusLabel;
    @FXML private Button   startTimerButton;
    @FXML private Button   stopTimerButton;

    @FXML private Label    timerLabel;
    @FXML private TextArea statusTextArea;
    @FXML private Button   toggleButton;   // hidden compatibility shim
    @FXML private Button   saveButton;
    @FXML private Button   deleteButton;

    // ── Dial state ────────────────────────────────────────────────
    private int    lapCount       = 0;   // complete hours already set
    private int    partialSeconds = 0;   // seconds within the current hour (0-3599)
    private double prevAngleDeg   = -1;  // -1 = no previous sample (press just happened)

    private int      setSeconds       = 0;
    private int      remainingSeconds = 0;
    private Timeline countdownTimeline;

    /** Maximum hours the user can set. */
    private static final int MAX_LAPS = 8;

    /**
     * Colours per lap, matching the StudySlice logo palette.
     * Index 0 = 1st hour, index 1 = 2nd hour, etc.
     */
    private static final String[] LAP_COLORS = {
        "#5CC8A5",  // teal-green  (logo primary)
        "#F2745C",  // coral-red   (logo secondary)
        "#EE7AB3",  // pink        (logo tertiary)
        "#6BA3E0",  // sky-blue
        "#9B8FD4",  // lavender
        "#E8C84A",  // amber
        "#5CC8A5",  // cycle back
        "#F2745C",
    };

    // ── Tracking engine (static so it survives scene switches) ────
    private static final TrackingEngine engine = new TrackingEngine();
    private static boolean isTracking = false;


    // ============================================================
    //  Lifecycle
    // ============================================================

    @FXML
    public void initialize() {
        if (timerCanvas != null) {
            drawDonut(0);

            // Press: snap ring to clicked angle, preserve current lap count
            timerCanvas.setOnMousePressed(e -> {
                if (countdownTimeline != null) return;
                double angle = toAngleDeg(e.getX(), e.getY());
                prevAngleDeg   = angle;
                partialSeconds = (int) (angle / 360.0 * 3600);
                setSeconds     = lapCount * 3600 + partialSeconds;
                remainingSeconds = setSeconds;
                drawDonut(setSeconds);
                updateCountdownLabel(setSeconds);
                updateStatusLabel();
            });

            // Drag: accumulate laps via wrap detection
            timerCanvas.setOnMouseDragged(e -> handleDialDrag(e.getX(), e.getY()));

            // Scroll: +/- 1 hour per notch (consume so page doesn't also scroll)
            timerCanvas.setOnScroll(e -> {
                e.consume(); // always consume — don't let ScrollPane see it
                if (countdownTimeline != null) return;
                if (e.getDeltaY() > 0) lapCount = Math.min(lapCount + 1, MAX_LAPS - 1);
                else                    lapCount = Math.max(0, lapCount - 1);
                setSeconds = lapCount * 3600 + partialSeconds;
                remainingSeconds = setSeconds;
                drawDonut(setSeconds);
                updateCountdownLabel(setSeconds);
                updateStatusLabel();
            });

            timerCanvas.setCursor(javafx.scene.Cursor.HAND);
        }

        engine.setUiUpdater(text -> Platform.runLater(() -> {
            if (text.contains("\n")) {
                String[] parts = text.split("\n", 2);
                if (timerLabel    != null) timerLabel.setText(parts[0]);
                if (statusTextArea != null) statusTextArea.setText(parts[1]);
            }
            updateSessionButtons();
        }));

        updateSessionButtons();
    }


    // ============================================================
    //  Navigation
    // ============================================================

    @FXML private void goToDashboard() { ViewManager.switchScene("dashboard-view.fxml"); }


    // ============================================================
    //  Dial interaction
    // ============================================================

    /** Converts a canvas mouse position to a 0–360 angle (0 = 12 o'clock, clockwise). */
    private double toAngleDeg(double mouseX, double mouseY) {
        double cx  = timerCanvas.getWidth()  / 2;
        double cy  = timerCanvas.getHeight() / 2;
        double deg = Math.toDegrees(Math.atan2(mouseY - cy, mouseX - cx)) + 90;
        if (deg <   0) deg += 360;
        if (deg >= 360) deg -= 360;
        return deg;
    }

    private void handleDialDrag(double mouseX, double mouseY) {
        if (countdownTimeline != null) return;

        // Basic dead-zone: ignore drags very close to the centre
        double cx   = timerCanvas.getWidth()  / 2;
        double cy   = timerCanvas.getHeight() / 2;
        double dist = Math.hypot(mouseX - cx, mouseY - cy);
        if (dist < 30) return;

        double angleDeg = toAngleDeg(mouseX, mouseY);

        // Wrap-around detection
        if (prevAngleDeg >= 0) {
            if (prevAngleDeg > 250 && angleDeg < 110)
                lapCount = Math.min(lapCount + 1, MAX_LAPS - 1); // crossed 12 o'clock clockwise
            else if (prevAngleDeg < 110 && angleDeg > 250)
                lapCount = Math.max(0, lapCount - 1);             // crossed 12 o'clock counter-cw
        }
        prevAngleDeg   = angleDeg;
        partialSeconds = (int) (angleDeg / 360.0 * 3600);
        setSeconds     = lapCount * 3600 + partialSeconds;
        remainingSeconds = setSeconds;

        drawDonut(setSeconds);
        updateCountdownLabel(setSeconds);
        updateStatusLabel();
    }

    private void updateCountdownLabel(int secs) {
        if (countdownLabel != null) countdownLabel.setText(formatDialTime(secs));
    }

    private void updateStatusLabel() {
        if (timerStatusLabel == null) return;
        if (setSeconds <= 0) {
            timerStatusLabel.setStyle("-fx-text-fill: #999999;");
            timerStatusLabel.setText("Drag the ring to set your time, or scroll to add hours.");
        } else {
            timerStatusLabel.setStyle("-fx-text-fill: #657972;");
            timerStatusLabel.setText(
                "Set to " + formatDialTime(setSeconds)
                + (lapCount > 0 ? "  (" + (lapCount + (partialSeconds > 0 ? 1 : 0)) + " hr session)" : "")
                + " — press Start when ready.");
        }
    }

    private String formatDialTime(int secs) {
        int h = secs / 3600;
        int m = (secs % 3600) / 60;
        int s = secs % 60;
        return h > 0
                ? String.format("%d:%02d:%02d", h, m, s)
                : String.format("%02d:%02d", m, s);
    }


    // ============================================================
    //  Donut drawing  (clean, logo-style — no ticks or labels)
    // ============================================================

    /**
     * Draws a clean donut ring where one full revolution = 1 hour.
     *
     * @param current  remaining (or set) seconds
     */
    private void drawDonut(int current) {
        if (timerCanvas == null) return;
        GraphicsContext gc = timerCanvas.getGraphicsContext2D();
        double w  = timerCanvas.getWidth();
        double h  = timerCanvas.getHeight();
        double cx = w / 2;
        double cy = h / 2;

        // Geometry
        double outerR = Math.min(w, h) / 2.0 - 8;
        double innerR = outerR * 0.60;          // donut-hole radius
        double midR   = (outerR + innerR) / 2.0;
        double ringW  = outerR - innerR;

        gc.clearRect(0, 0, w, h);
        gc.setLineCap(StrokeLineCap.ROUND);

        // ── Background track (full ring, warm grey) ──────────────
        gc.setStroke(Color.web("#E5E0D9"));
        gc.setLineWidth(ringW);
        gc.strokeOval(cx - midR, cy - midR, midR * 2, midR * 2);

        // ── Coloured progress arc ─────────────────────────────────
        if (current > 0) {
            // (current-1) so that 3600 → lapIndex=0, secondsInLap=3600
            int lapIndex      = (current - 1) / 3600;
            int secondsInLap  = (current - 1) % 3600 + 1;   // 1 – 3600
            double arcDeg     = secondsInLap / 3600.0 * 360.0;

            String colorHex = LAP_COLORS[Math.min(lapIndex, LAP_COLORS.length - 1)];
            gc.setStroke(Color.web(colorHex));
            gc.setLineWidth(ringW);

            if (arcDeg >= 359.9) {
                // Full ring — use strokeOval to avoid arc-cap artefacts
                gc.strokeOval(cx - midR, cy - midR, midR * 2, midR * 2);
            } else {
                // Partial arc: start at 12 o'clock (90°), go clockwise (negative extent)
                gc.strokeArc(cx - midR, cy - midR, midR * 2, midR * 2,
                             90, -arcDeg, ArcType.OPEN);
            }
        }

        // ── Subtle start-position tick at 12 o'clock ─────────────
        gc.setStroke(Color.web("#FFFFFF"));
        gc.setLineWidth(3);
        gc.setLineCap(StrokeLineCap.BUTT);
        gc.strokeLine(cx, cy - innerR - 1, cx, cy - outerR + 1);
    }


    // ============================================================
    //  Timer start / stop
    // ============================================================

    @FXML
    private void handleStartTimer() {
        if (setSeconds <= 0) {
            timerStatusLabel.setStyle("-fx-text-fill: #7B4141;");
            timerStatusLabel.setText("Drag the ring or scroll to set a time first.");
            return;
        }
        remainingSeconds = setSeconds;

        engine.startTracking();
        isTracking = true;
        startTimerButton.setDisable(true);
        stopTimerButton.setDisable(false);
        timerStatusLabel.setStyle("-fx-text-fill: #657972;");
        timerStatusLabel.setText("Tracking active — session will save when the timer ends.");

        countdownTimeline = new Timeline(new KeyFrame(Duration.seconds(1), tick -> {
            remainingSeconds = Math.max(remainingSeconds - 1, 0);
            drawDonut(remainingSeconds);
            updateCountdownLabel(remainingSeconds);
            if (remainingSeconds <= 0) {
                countdownTimeline.stop();
                countdownTimeline = null;
                onTimerFinished();
            }
        }));
        countdownTimeline.setCycleCount(Timeline.INDEFINITE);
        countdownTimeline.play();
    }

    @FXML
    private void handleStopTimer() {
        if (countdownTimeline != null) { countdownTimeline.stop(); countdownTimeline = null; }
        engine.stopTracking();
        isTracking = false;
        startTimerButton.setDisable(false);
        stopTimerButton.setDisable(true);
        timerStatusLabel.setStyle("-fx-text-fill: #657972;");
        timerStatusLabel.setText("Timer stopped. Drag to reset, or press Start to continue.");
        updateSessionButtons();
    }

    private void onTimerFinished() {
        engine.stopTracking();
        isTracking = false;
        startTimerButton.setDisable(false);
        stopTimerButton.setDisable(true);
        timerStatusLabel.setStyle("-fx-text-fill: #5CC8A5;");
        timerStatusLabel.setText("Session complete! Great work.");
        updateSessionButtons();

        // Force window to front
        Stage stage = (Stage) timerCanvas.getScene().getWindow();
        stage.setAlwaysOnTop(true);
        stage.toFront();
        stage.requestFocus();

        // Completion popup
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(stage);
        popup.setTitle("Session Complete");
        popup.setResizable(false);

        Label doneLabel = new Label("Time's up! Great work.");
        doneLabel.getStyleClass().add("dashboard-card-label");

        Label subLabel = new Label("Your session has been recorded. Would you like to save it?");
        subLabel.setWrapText(true);
        subLabel.getStyleClass().add("dashboard-helper-text");

        Button saveNowButton = new Button("Save Session");
        saveNowButton.getStyleClass().add("dashboard-primary-button");
        saveNowButton.setOnAction(e -> { popup.close(); stage.setAlwaysOnTop(false); handleSaveSession(); });

        Button dismissButton = new Button("Dismiss");
        dismissButton.getStyleClass().add("dashboard-secondary-button");
        dismissButton.setOnAction(e -> { popup.close(); stage.setAlwaysOnTop(false); });

        HBox buttons = new HBox(10, saveNowButton, dismissButton);
        buttons.setAlignment(Pos.CENTER);

        VBox root = new VBox(16, doneLabel, subLabel, buttons);
        root.getStyleClass().add("dashboard-card");
        root.setPadding(new Insets(24));
        root.setPrefWidth(360);
        root.setAlignment(Pos.CENTER);

        Scene popScene = new Scene(root);
        popScene.getStylesheets().add(
                getClass().getResource("/com/example/cab302studyslice/styles.css").toExternalForm());
        popup.setScene(popScene);
        popup.showAndWait();
    }


    // ============================================================
    //  Session management (Save / Delete)
    // ============================================================

    private boolean hasSessionData() {
        return engine.getTotalSeconds() > 0 && !engine.getTimeSpent().isEmpty();
    }

    private void updateSessionButtons() {
        boolean hasData = hasSessionData();
        if (saveButton   != null) saveButton.setDisable(!hasData);
        if (deleteButton != null) deleteButton.setDisable(!hasData);
    }

    @FXML
    private void handleSaveSession() {
        if (!hasSessionData()) return;

        // Stop timer & tracking if still running
        if (countdownTimeline != null) { countdownTimeline.stop(); countdownTimeline = null; }
        if (isTracking) {
            engine.stopTracking();
            isTracking = false;
            startTimerButton.setDisable(false);
            stopTimerButton.setDisable(true);
        }

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (timerCanvas != null && timerCanvas.getScene() != null)
            dialog.initOwner(timerCanvas.getScene().getWindow());
        dialog.setTitle("Save Session");
        dialog.setResizable(false);

        Label titleLabel  = new Label("Save Session");
        titleLabel.getStyleClass().add("dashboard-card-label");

        Label helperLabel = new Label("Enter a session name to save this study session.");
        helperLabel.setWrapText(true);
        helperLabel.getStyleClass().add("dashboard-helper-text");

        TextField nameField = new TextField();
        nameField.setPromptText("Session name");
        nameField.getStyleClass().add("auth-field");

        Label msgLabel = new Label();
        msgLabel.getStyleClass().add("auth-message");

        Button confirmBtn = new Button("Confirm");
        confirmBtn.getStyleClass().add("dashboard-primary-button");
        confirmBtn.setOnAction(ev -> {
            String sessionName = nameField.getText().trim();
            if (sessionName.isEmpty()) {
                msgLabel.setStyle("-fx-text-fill: #7B4141;");
                msgLabel.setText("Please enter a session name.");
                return;
            }
            int uid = User.getCurrentUserId();
            if (uid <= 0) {
                msgLabel.setStyle("-fx-text-fill: #7B4141;");
                msgLabel.setText("Please log in again before saving.");
                return;
            }

            LocalDateTime endTime   = LocalDateTime.now();
            LocalDateTime startTime = endTime.minusSeconds(engine.getTotalSeconds());
            List<Activity> activities = new ArrayList<>();
            for (Map.Entry<String, Integer> e : engine.getTimeSpent().entrySet())
                activities.add(new Activity(e.getKey(), e.getValue()));

            boolean saved = DatabaseManager.saveFullSession(
                    uid, sessionName, startTime, endTime, engine.getTotalSeconds(), activities);
            if (!saved) {
                msgLabel.setStyle("-fx-text-fill: #7B4141;");
                msgLabel.setText("Failed to save session to database.");
                return;
            }

            HistoryStore.addSession(buildSessionText(sessionName));
            resetCurrentSession();
            dialog.close();

            // Silent AI wrap in background
            new Thread(() -> {
                int latestId = DatabaseManager.getLatestSessionId(uid);
                if (latestId <= 0) return;
                List<SessionHistoryEntry> all = DatabaseManager.getSessionHistoryByUserId(uid);
                SessionHistoryEntry newest = all.stream()
                        .filter(s -> s.getSessionId() == latestId).findFirst().orElse(null);
                if (newest == null) return;
                AiAPI.WrappedData data = AiAPI.analyzeSessionStructured(newest, all);
                if (data == null) return;
                DatabaseManager.insertWrappedData(newest.getSessionId(), data.recordTotalTime,
                        data.mostUsedApp, data.ranking, data.badHabit,
                        data.comparedToSessions, data.streakCurrent, data.score);
            }).start();
        });

        VBox root = new VBox(12, titleLabel, helperLabel, nameField, msgLabel, confirmBtn);
        root.getStyleClass().add("dashboard-card");
        root.setPadding(new Insets(20));
        root.setPrefWidth(360);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(
                getClass().getResource("/com/example/cab302studyslice/styles.css").toExternalForm());
        dialog.setScene(scene);
        dialog.showAndWait();
    }

    @FXML
    private void handleDeleteSession() {
        if (!hasSessionData()) return;
        if (countdownTimeline != null) { countdownTimeline.stop(); countdownTimeline = null; }
        if (isTracking) {
            engine.stopTracking();
            isTracking = false;
            startTimerButton.setDisable(false);
            stopTimerButton.setDisable(true);
        }
        resetCurrentSession();
    }

    private void resetCurrentSession() {
        engine.stopTracking();
        engine.reset();
        if (timerLabel    != null) timerLabel.setText("Total Study Time: 00:00:00");
        if (statusTextArea != null) statusTextArea.clear();
        isTracking = false;
        updateSessionButtons();
        // Reset dial to zero
        lapCount = 0; partialSeconds = 0; setSeconds = 0; remainingSeconds = 0;
        drawDonut(0);
        updateCountdownLabel(0);
        if (timerStatusLabel != null) {
            timerStatusLabel.setStyle("");
            timerStatusLabel.setText("Drag the ring to set your time, or scroll to add hours.");
        }
    }

    // ── Helpers ───────────────────────────────────────────────────

    private String buildSessionText(String sessionName) {
        StringBuilder sb = new StringBuilder();
        sb.append("Session Name: ").append(sessionName).append("\n");
        sb.append("Session Date: ")
          .append(LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")))
          .append("\n");
        sb.append("Total Study Time: ").append(formatTime(engine.getTotalSeconds())).append("\n\n");
        for (Map.Entry<String, Integer> e : engine.getTimeSpent().entrySet())
            sb.append(e.getKey()).append(" : ").append(formatTime(e.getValue())).append("\n");
        return sb.toString();
    }

    private String formatTime(int totalSeconds) {
        long h = totalSeconds / 3600;
        long m = (totalSeconds % 3600) / 60;
        long s = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    // Hidden toggle button — kept for engine compatibility
    @FXML private void handleToggleTracking() { /* unused on timer page */ }
}

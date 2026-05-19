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
import javafx.stage.StageStyle;
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
    @FXML private Button   editButton;

    @FXML private Label    timerLabel;
    @FXML private TextArea statusTextArea;
    @FXML private Button   toggleButton;   // hidden compatibility shim
    @FXML private Button   saveButton;
    @FXML private Button   deleteButton;

    // ── Dial state ────────────────────────────────────────────────
    private int    lapCount       = 0;   // complete hours already set
    private int    partialSeconds = 0;   // seconds within the current hour (0-3599)
    private double prevAngleDeg   = -1;  // -1 = no previous sample (press just happened)
    private boolean isEditMode    = false;

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
                if (!isEditMode || countdownTimeline != null) return;
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

            // Start locked — edit mode must be enabled before dragging
            timerCanvas.setCursor(javafx.scene.Cursor.DEFAULT);
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
    //  Edit mode toggle
    // ============================================================

    private static final String EDIT_BTN_IDLE =
            "-fx-font-size: 12px; -fx-padding: 7 18;" +
            "-fx-background-color: #819D93; -fx-text-fill: white;" +
            "-fx-background-radius: 999; -fx-cursor: hand;";
    private static final String EDIT_BTN_ACTIVE =
            "-fx-font-size: 12px; -fx-padding: 7 18;" +
            "-fx-background-color: #5CC8A5; -fx-text-fill: white;" +
            "-fx-background-radius: 999; -fx-cursor: hand;";

    @FXML
    private void handleEditButton() {
        isEditMode = !isEditMode;
        if (isEditMode) {
            editButton.setText("✓  Done");
            editButton.setStyle(EDIT_BTN_ACTIVE);
            timerCanvas.setCursor(javafx.scene.Cursor.HAND);
            timerStatusLabel.setStyle("-fx-text-fill: #5CC8A5;");
            timerStatusLabel.setText("Edit mode — drag the ring to set your time.");
        } else {
            exitEditMode();
        }
    }

    /** Leaves edit mode and restores button + status label. */
    private void exitEditMode() {
        isEditMode = false;
        if (editButton != null) {
            editButton.setText("✏  Edit Time");
            editButton.setStyle(EDIT_BTN_IDLE);
        }
        timerCanvas.setCursor(javafx.scene.Cursor.DEFAULT);
        updateStatusLabel();
    }


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
        if (!isEditMode || countdownTimeline != null) return;

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
        if (isEditMode) {
            timerStatusLabel.setStyle("-fx-text-fill: #5CC8A5;");
            timerStatusLabel.setText("Edit mode — drag the ring to set your time.");
        } else if (setSeconds <= 0) {
            timerStatusLabel.setStyle("-fx-text-fill: #999999;");
            timerStatusLabel.setText("Click ✏ Edit Time to set your timer.");
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
     * Draws a layered donut where one full revolution = 1 hour.
     *
     * Completed laps are painted as full rings from the bottom up so that
     * the current partial lap sits on top.  Counting down "unspools" the
     * outermost colour first, revealing the previous lap's colour beneath.
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

        // ── Geometry ─────────────────────────────────────────────
        double outerR = Math.min(w, h) / 2.0 - 8;
        double innerR = outerR * 0.60;
        double midR   = (outerR + innerR) / 2.0;
        double ringW  = outerR - innerR;

        gc.clearRect(0, 0, w, h);
        gc.setLineCap(StrokeLineCap.BUTT);

        // ── Background track ──────────────────────────────────────
        gc.setStroke(Color.web("#E5E0D9"));
        gc.setLineWidth(ringW);
        gc.strokeOval(cx - midR, cy - midR, midR * 2, midR * 2);

        if (current > 0) {
            // Split total seconds into complete laps + remainder within lap.
            // (current-1) trick ensures exactly 3600 s maps to lap 0, full arc.
            int fullLaps     = (current - 1) / 3600;
            int secondsInLap = (current - 1) % 3600 + 1;   // 1 – 3600
            double arcDeg    = secondsInLap / 3600.0 * 360.0;
            String curColor  = LAP_COLORS[Math.min(fullLaps, LAP_COLORS.length - 1)];

            // ── Completed laps: paint full rings, oldest first ────
            // Each successive ring sits on top of the one below it,
            // so only the most-recent complete lap colour is visible.
            // When the current lap unspools it reveals the layer beneath.
            for (int i = 0; i < fullLaps; i++) {
                gc.setStroke(Color.web(LAP_COLORS[Math.min(i, LAP_COLORS.length - 1)]));
                gc.setLineWidth(ringW);
                gc.strokeOval(cx - midR, cy - midR, midR * 2, midR * 2);
            }

            // ── Current partial lap arc ───────────────────────────
            gc.setStroke(Color.web(curColor));
            gc.setLineWidth(ringW);

            if (arcDeg >= 359.99) {
                gc.strokeOval(cx - midR, cy - midR, midR * 2, midR * 2);
            } else {
                // Arc: 12 o'clock (90°) → clockwise (negative extent)
                gc.strokeArc(cx - midR, cy - midR, midR * 2, midR * 2,
                             90, -arcDeg, ArcType.OPEN);

                // ── Rounded tip, strictly clipped to the arc's sector ──
                // Build a pie-sector clip from 12 o'clock clockwise to the
                // tip angle so the filled circle cannot bleed into the
                // preceding lap on either side of 12 o'clock.
                double tipRad = Math.toRadians(90 - arcDeg);
                double tipX   = cx + midR * Math.cos(tipRad);
                double tipY   = cy - midR * Math.sin(tipRad);
                double tipR   = ringW / 2.0;
                double clipR  = outerR + tipR; // radius large enough to contain the tip circle

                gc.save();
                gc.beginPath();
                gc.moveTo(cx, cy);
                gc.lineTo(cx, cy - clipR);                   // to 12 o'clock on clip circle
                gc.arc(cx, cy, clipR, clipR, 90, -arcDeg);  // clockwise arc to tip angle
                gc.closePath();                              // line back to centre
                gc.clip();

                gc.setFill(Color.web(curColor));
                gc.fillOval(tipX - tipR, tipY - tipR, tipR * 2, tipR * 2);
                gc.restore();
            }
        }

        // ── White notch at 12 o'clock — lap-boundary marker ──────
        gc.setStroke(Color.web("#FFFFFF"));
        gc.setLineWidth(4);
        gc.strokeLine(cx, cy - innerR + 1, cx, cy - outerR - 1);
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

        // Exit edit mode when the timer starts
        if (isEditMode) exitEditMode();

        engine.startTracking();
        isTracking = true;
        startTimerButton.setDisable(true);
        stopTimerButton.setDisable(false);
        if (editButton != null) editButton.setDisable(true);
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
        if (editButton != null) editButton.setDisable(false);
        timerStatusLabel.setStyle("-fx-text-fill: #657972;");
        timerStatusLabel.setText("Timer stopped. Drag to reset, or press Start to continue.");
        updateSessionButtons();
    }

    private void onTimerFinished() {
        engine.stopTracking();
        isTracking = false;
        startTimerButton.setDisable(false);
        stopTimerButton.setDisable(true);
        if (editButton != null) editButton.setDisable(false);
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
        popup.initStyle(StageStyle.TRANSPARENT);
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.initOwner(stage);
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
        root.setStyle("-fx-background-radius: 16; -fx-border-radius: 16;");
        root.setPadding(new Insets(24));
        root.setPrefWidth(360);
        root.setAlignment(Pos.CENTER);

        Scene popScene = new Scene(root);
        popScene.setFill(null);
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

        // Capture main window before the modal opens
        final Stage mainStage = (timerCanvas != null && timerCanvas.getScene() != null)
                ? (Stage) timerCanvas.getScene().getWindow() : null;

        Stage dialog = new Stage();
        dialog.initModality(Modality.APPLICATION_MODAL);
        if (mainStage != null) dialog.initOwner(mainStage);
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

            // Silent AI wrap in background — shows unwrap prompt when ready
            new Thread(() -> {
                int latestId = DatabaseManager.getLatestSessionId(uid);
                if (latestId <= 0) return;
                List<SessionHistoryEntry> all = DatabaseManager.getSessionHistoryByUserId(uid);
                SessionHistoryEntry newest = all.stream()
                        .filter(s -> s.getSessionId() == latestId).findFirst().orElse(null);
                if (newest == null) return;
                AiAPI.WrappedData data = AiAPI.analyzeSessionStructured(newest, all);
                if (data == null) return;
                data.totalSessions = all.size();
                DatabaseManager.insertWrappedData(newest.getSessionId(), data.recordTotalTime,
                        data.mostUsedApp, data.ranking, data.badHabit,
                        data.comparedToSessions, data.streakCurrent, data.score);
                final AiAPI.WrappedData finalData = data;
                final SessionHistoryEntry finalSession = newest;
                Platform.runLater(() -> showUnwrapPrompt(finalSession, finalData, mainStage));
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
        if (editButton != null) editButton.setDisable(false);
        updateSessionButtons();
        // Reset dial to zero
        lapCount = 0; partialSeconds = 0; setSeconds = 0; remainingSeconds = 0;
        drawDonut(0);
        updateCountdownLabel(0);
        if (timerStatusLabel != null) {
            timerStatusLabel.setStyle("");
            timerStatusLabel.setText("Drag the ring to set your time, or click ✏ Edit Time for precise hours.");
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

    // Hidden toggle button — kept for engine compatibility
    @FXML private void handleToggleTracking() { /* unused on timer page */ }
}

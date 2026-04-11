package com.example.cab302studyslice.Controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;
import com.example.cab302studyslice.View.ViewManager;
import com.example.cab302studyslice.Model.HistoryStore;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class HistoryController {

    // Ensure user navigates back to dashboard
    @FXML
    private void onBackClick() {
        ViewManager.switchScene("dashboard-view.fxml");
    }

    /** @FXML
    private TextArea historyTextArea;

    //When history page opens all the current saved session history will be loaded
    @FXML
    public void initialize() {
        if (historyTextArea != null) {
            historyTextArea.setText(HistoryStore.getHistoryText());
        }
    }

    public void setHistoryText(String historyText) {
        if (historyTextArea != null) {
            historyTextArea.setText(historyText);
        }
    }

    public void appendSession(String sessionText) {
        if (historyTextArea != null) {
            historyTextArea.appendText("\n\n" + sessionText);
        }
    } */
    @FXML
    private Label totalHoursLabel;

    @FXML
    private TilePane historyContainer;

    @FXML
    public void initialize() {
        loadHistoryCards();
        updateTotalHoursLogged();
    }

    // Show total hours user studied at the top of page
    private void updateTotalHoursLogged() {
        String fullHistory = HistoryStore.getHistoryText();

        if (fullHistory == null || fullHistory.equals("No study history available yet.")) {
            totalHoursLabel.setText("00:00:00");
            return;
        }

        String[] sessions =  fullHistory.split("\\n\\n------------------------------\\n\\n");

        int totalSeconds= 0;

        for (String sessionText : sessions) {
            String timeString = getTotalStudyTime(sessionText);
            totalSeconds += convertTimeToSeconds(timeString);
        }

        totalHoursLabel.setText(formatSeconds(totalSeconds));
    }

    private String getTotalStudyTime(String sessionText) {
        String[] lines = sessionText.split("\\n");

        for (String line : lines) {
            if (line.startsWith("Total Study Time:")) {
                return line.replace("Total Study Time:", "").trim();
            }
        }

        return "00:00:00";
    }

    private int convertTimeToSeconds(String time) {
        if (time == null || time.isBlank()) {
            return 0;
        }

        String[] parts = time.split(":");

        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        int seconds = Integer.parseInt(parts[2]);

        return hours * 3600 + minutes * 60 + seconds;
    }

    private String formatSeconds(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    // Display session data as Cards
    private void loadHistoryCards() {
        String fullHistory = HistoryStore.getHistoryText();

        if (fullHistory == null || fullHistory.equals("No study history available yet.")) {
            addHistoryCard("No sessions yet", "Complete a session to see it here.");
            return;
        }

        String[] sessions = fullHistory.split("\\n\\n------------------------------\\n\\n");

        for (String session : sessions) {
            // cannot actually click into the cards yet, will be done once wrapper pages are set up
            addHistoryCard(formatSessionPreview(session), "Click to view details");
        }
    }

    private void addHistoryCard(String mainText, String subText) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(220, 140);
        card.getStyleClass().add("history-tile");

        Label mainLabel = new Label(mainText);
        mainLabel.getStyleClass().add("history-time");
        mainLabel.setWrapText(true);

        Label subLabel = new Label(subText);
        subLabel.getStyleClass().add("history-label");
        subLabel.setWrapText(true);

        card.getChildren().addAll(mainLabel, subLabel);
        historyContainer.getChildren().add(card);
    }

    private String formatSessionPreview(String sessionText) {
        String[] lines = sessionText.split("\\n");

        String sessionDate = "";
        String totalStudyTime = "";

        for (String line: lines) {
            if (line.startsWith("Session Date:")) {
                sessionDate = line;

            }
            if (line.startsWith("Total Study Time:")) {
                totalStudyTime = line;
            }
        }

        return sessionDate + "\n" + totalStudyTime;
    }
}
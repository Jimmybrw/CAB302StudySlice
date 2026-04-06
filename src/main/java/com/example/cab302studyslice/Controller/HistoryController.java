package com.example.cab302studyslice.Controller;

import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.control.ListView;
import com.example.cab302studyslice.View.ViewManager;

public class HistoryController {

    @FXML
    private TilePane historyContainer;

    @FXML
    private Label totalHoursLabel;

    @FXML
    public void initialize() {
        totalHoursLabel.setText("65:27:31");

        addHistoryCard("01:20:11", "Week 1.1");
        addHistoryCard("03:00:07", "Week 1.2");
        addHistoryCard("00:12:43", "Week 1.3");
        addHistoryCard("02:15:10", "Week 2.1");
        addHistoryCard("01:47:55", "Week 2.2");
        addHistoryCard("00:55:28", "Week 2.3");
    }

    private void addHistoryCard(String sessionTime, String sessionLabel) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPrefSize(220, 140);
        card.getStyleClass().add("session-title");

        Label timeLabel = new Label(sessionTime);
        timeLabel.getStyleClass().add("session-title-text");

        Label label = new Label(sessionLabel);
        label.getStyleClass().add("session-title-text");

        card.getChildren().addAll(timeLabel, label);
        historyContainer.getChildren().add(card);
    }

    @FXML
    private void onBackClick() {
        ViewManager.switchScene("dashboard-view.fxml");
    }
}
package com.example.cab302studyslice.Controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class DashboardController {

    @FXML private HBox recentSessionsContainer;
    @FXML private PieChart pieChart;
    @FXML private BarChart<String, Number> barChart;

    @FXML
    public void initialize() {
        loadRecentSessions();
        loadPieChartData();
        loadBarChartData();
    }

    // -----------------------------
    // NAVIGATION BUTTONS
    // -----------------------------
    @FXML
    private void onTimerClicked(javafx.event.ActionEvent event) {
        switchScene(event, "timer-view.fxml");
    }

    @FXML
    private void onExploreClicked(javafx.event.ActionEvent event) {
        switchScene(event, "explore-view.fxml");
    }

    private void switchScene(javafx.event.ActionEvent event, String fxmlFile) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(fxmlFile));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // -----------------------------
    // RECENT SESSIONS (placeholder)
    // -----------------------------
    private void loadRecentSessions() {
        List<String> recentWeeks = List.of("Week 1", "Week 2", "Week 23");

        recentSessionsContainer.getChildren().clear();

        for (String week : recentWeeks) {
            StackPane tile = createSessionTile(week);
            recentSessionsContainer.getChildren().add(tile);
        }
    }

    private StackPane createSessionTile(String label) {
        StackPane pane = new StackPane();
        pane.getStyleClass().add("session-tile");
        pane.setPrefSize(120, 60);

        Label text = new Label(label);
        text.getStyleClass().add("session-tile-text");

        pane.getChildren().add(text);
        return pane;
    }

    // -----------------------------
    // PIE CHART
    // -----------------------------
    private void loadPieChartData() {
        pieChart.getData().clear();
        pieChart.getData().add(new PieChart.Data("Study", 65));
        pieChart.getData().add(new PieChart.Data("Breaks", 20));
        pieChart.getData().add(new PieChart.Data("Distractions", 15));
    }

    // -----------------------------
    // BAR CHART
    // -----------------------------
    private void loadBarChartData() {
        barChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Hours Studied");

        series.getData().add(new XYChart.Data<>("Week 1", 5));
        series.getData().add(new XYChart.Data<>("Week 2", 7));
        series.getData().add(new XYChart.Data<>("Week 23", 3));

        barChart.getData().add(series);
    }
}
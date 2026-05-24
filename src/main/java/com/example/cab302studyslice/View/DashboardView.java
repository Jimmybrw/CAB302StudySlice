package com.example.cab302studyslice.View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * View class for the dashboard scene.
 * Loads and displays the dashboard FXML template.
 */
public class DashboardView extends Application {

    /**
     * Starts the dashboard view with the specified stage.
     *
     * @param stage the primary stage to display the view on
     * @throws Exception if FXML loading fails
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DashboardView.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Dashboard View");
        stage.setScene(scene);


    }
}
package com.example.cab302studyslice.View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class DashboardView extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("DashboardView.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Dashboard View");
        stage.setScene(scene);


    }
}
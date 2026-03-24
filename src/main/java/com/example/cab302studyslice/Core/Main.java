package com.example.cab302studyslice.Core;

import com.example.cab302studyslice.View.ViewManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

public class Main extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        ViewManager.setStage(stage);
        try {
            Runtime.getRuntime().exec("powershell.exe -WindowStyle Hidden -ExecutionPolicy Bypass -File tracker.ps1");
        } catch (Exception e) {
            System.err.println("Could not launch tracker.ps1. Ensure it is in the project root.");
        }
        ViewManager.switchScene("home-view.fxml");
        stage.setTitle("StudySlice");

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }

    public static void main(String[] args) {
        launch();
    }
}
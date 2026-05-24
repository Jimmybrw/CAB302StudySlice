package com.example.cab302studyslice.Core;

import com.example.cab302studyslice.View.ViewManager;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;
import javafx.scene.image.Image;

/**
 * Entry point for the StudySlice JavaFX application.
 * Initializes the main window, launches the background window tracker, and displays the login view.
 */
public class Main extends Application {
    /**
     * Starts the JavaFX application.
     * Sets up the stage, launches the tracking process, and initializes the login view.
     *
     * @param stage the primary stage for this application
     * @throws Exception if scene loading fails
     */
    @Override
    public void start(Stage stage) throws Exception {
        ViewManager.setStage(stage);

        try {
            // Launch the tracker script
            ProcessBuilder pb = new ProcessBuilder("powershell.exe", "-WindowStyle", "Hidden", "-ExecutionPolicy", "Bypass", "-File", "tracker.ps1");
            pb.start();
        } catch (Exception e) {
            System.err.println("Could not launch tracker.ps1. Ensure it is in the project root.");
        }

        // Show login view first
        ViewManager.switchScene("login-view.fxml");
        stage.setTitle("StudySlice");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/com/example/cab302studyslice/Images/CAB302_StudySliceFullSize.png")));

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });
    }


    /**
     * Main method to launch the application.
     *
     * @param args command-line arguments (not used)
     */
    public static void main(String[] args) {
        launch();
    }
}

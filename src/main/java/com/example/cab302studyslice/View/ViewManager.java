package com.example.cab302studyslice.View;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

/**
 * Manager for switching between JavaFX scenes.
 * Centralizes stage management and FXML loading for scene transitions.
 */
public class ViewManager {
    private static Stage stage;
    private static final int WIDTH = 900;
    private static final int HEIGHT = 600;

    /**
     * Sets the primary stage for scene management.
     * Configures the stage as non-resizable.
     *
     * @param primaryStage the main application stage
     */
    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
        stage.setResizable(false);
    }

    /**
     * Switches the current scene to the specified FXML view.
     * Loads the FXML file, applies stylesheets, and displays it on the stage.
     *
     * @param fxmlFile the FXML filename to load (without path prefix)
     */
    public static void switchScene(String fxmlFile) {
        try {
            String resourcePath = "/com/example/cab302studyslice/FXML/" + fxmlFile;
            URL resource = ViewManager.class.getResource(resourcePath);

            if (resource == null) {
                throw new RuntimeException("CRITICAL: Could not find resource at " + resourcePath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            Scene scene = new Scene(loader.load(), WIDTH, HEIGHT);
            scene.getStylesheets().add(ViewManager.class.getResource("/com/example/cab302studyslice/styles.css").toExternalForm());
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
        }
    }
}

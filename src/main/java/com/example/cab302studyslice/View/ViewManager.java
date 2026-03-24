package com.example.cab302studyslice.View;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.net.URL;

public class ViewManager {
    private static Stage stage;

    public static void setStage(Stage primaryStage) {
        stage = primaryStage;
    }

    public static void switchScene(String fxmlFile) {
        try {
            String resourcePath = "/com/example/cab302studyslice/FXML/" + fxmlFile;
            URL resource = ViewManager.class.getResource(resourcePath);

            if (resource == null) {
                throw new RuntimeException("CRITICAL: Could not find resource at " + resourcePath);
            }

            FXMLLoader loader = new FXMLLoader(resource);
            stage.setScene(new Scene(loader.load(), 500, 500));
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
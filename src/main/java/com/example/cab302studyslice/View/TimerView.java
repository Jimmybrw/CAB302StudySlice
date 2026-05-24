
// this will have the mode you would like (start stop or set alarm)
// alarm notification


package com.example.cab302studyslice.View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * View class for the timer scene.
 * Loads and displays the timer FXML template.
 */
public class TimerView extends Application {

    /**
     * Starts the timer view with the specified stage.
     *
     * @param stage the primary stage to display the view on
     * @throws Exception if FXML loading fails
     */
    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("timer-view.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Timer View");
        stage.setScene(scene);


    }
}
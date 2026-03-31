
// this will have the mode you would like (start stop or set alarm)
// alarm notification


package com.example.cab302studyslice.View;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class TimerView extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("timer-view.fxml"));
        Scene scene = new Scene(loader.load());
        stage.setTitle("Timer View");
        stage.setScene(scene);


    }
}
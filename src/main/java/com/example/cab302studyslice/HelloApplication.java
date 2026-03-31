package com.example.cab302studyslice;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class HelloApplication extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(HelloApplication.class.getResource("launch-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 600, 400);
        stage.setTitle("hello!");
        stage.setScene(scene);
        stage.show();
    }
}
//<<<<<<< HEAD
//testjijisfsddfd
//=======
//testjijis
//>>>>>>> //origin/master

//another test chsiuvhson


//tesst 3

//test 4 and again
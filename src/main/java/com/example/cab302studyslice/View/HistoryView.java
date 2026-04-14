package com.example.cab302studyslice.View;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

public class HistoryView {
    public Parent getView(){
        try{
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/com/example/cab302studyslice/FXML/history-view.fxml")
            );
            return loader.load();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
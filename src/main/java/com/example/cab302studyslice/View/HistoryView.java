package com.example.cab302studyslice.View;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;

/**
 * View class for the history scene.
 * Loads and provides the history FXML template.
 */
public class HistoryView {
    /**
     * Loads and returns the history view Parent node.
     *
     * @return the loaded Parent node, or null if loading fails
     */
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
//nothing yet
package com.example.cab302studyslice.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import com.example.cab302studyslice.View.ViewManager;

public class HistoryController {

    @FXML
    private void onBackClick() {
        ViewManager.switchScene("dashboard-view.fxml");
    }
    @FXML
    private ListView<String> historyList;

    @FXML
    public void initialize(){
        // Dummies
        historyList.getItems().add("12 mar 2026 - 2h 15m - Word");
        historyList.getItems().add("11 mar 2026 - 2h - Youtube");
        historyList.getItems().add("10 mar 2026 - 1h 15m - intelliJ");
    }
}
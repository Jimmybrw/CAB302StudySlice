package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.View.ViewManager;
import javafx.fxml.FXML;

public class ViewController {
    @FXML
    private void goToDashboard() {
        ViewManager.switchScene("dashboard-view.fxml");
    }

    @FXML
    private void goToHistory() {
        ViewManager.switchScene("history-view.fxml");
    }
}
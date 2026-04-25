package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.View.ViewManager;
import javafx.event.ActionEvent;
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

    @FXML
    private void goToTimer() { ViewManager.switchScene("timer-view.fxml"); }

    public void handleSignIn(ActionEvent actionEvent) {
    }

    public void handleSignUp(ActionEvent actionEvent) {
    }


}
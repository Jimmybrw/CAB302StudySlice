package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.View.ViewManager;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

/**
 * Base navigation controller for switching between main application views.
 * Handles navigation to dashboard, history, and timer screens.
 */
public class ViewController {
    /**
     * Navigates to the dashboard view.
     */
    @FXML
    private void goToDashboard() {
        ViewManager.switchScene("dashboard-view.fxml");
    }

    /**
     * Navigates to the history view.
     */
    @FXML
    private void goToHistory() {
        ViewManager.switchScene("history-view.fxml");
    }

    /**
     * Navigates to the timer view.
     */
    @FXML
    private void goToTimer() { ViewManager.switchScene("timer-view.fxml"); }

    /**
     * Handles sign-in action.
     *
     * @param actionEvent the action event triggered by sign-in button
     */
    public void handleSignIn(ActionEvent actionEvent) {
    }

    /**
     * Handles sign-up action.
     *
     * @param actionEvent the action event triggered by sign-up button
     */
    public void handleSignUp(ActionEvent actionEvent) {
    }


}
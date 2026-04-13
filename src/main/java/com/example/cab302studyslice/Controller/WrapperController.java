package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.View.ViewManager;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class WrapperController {
    // Temporary labels, will update according to processed data.
    @FXML
    private Label sessionLabel;

    @FXML
    private Label summaryLabel;

    @FXML
    private Label totalTimeLabel;

    @FXML
    private Label mostUsedAppLabel;

    @FXML
    private Label focusScoreLabel;

    @FXML
    private Label insightLabel;

    @FXML
    public void initialize(){
        // Placeholder data for now
        //sessionLabel.setText("Session Summary");
        summaryLabel.setText("brief summary of the session");
        totalTimeLabel.setText("total time(00:00:00)");
        mostUsedAppLabel.setText("Most Used App");
        focusScoreLabel.setText("Focus Score");
        insightLabel.setText("something");
    }

    // Navigation back to dashboard
    @FXML
    private void onBackClick() {
        ViewManager.switchScene("dashboard-view.fxml");
    }

}

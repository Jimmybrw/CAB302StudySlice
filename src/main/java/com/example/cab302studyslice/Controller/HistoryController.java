package com.example.cab302studyslice.Controller;

import com.example.cab302studyslice.Model.HistoryStore;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class HistoryController {

    @FXML
    private TextArea historyTextArea;

    //When history page opens all the current saved session history will be loaded
    @FXML
    public void initialize() {
        if (historyTextArea != null) {
            historyTextArea.setText(HistoryStore.getHistoryText());
        }
    }

    public void setHistoryText(String historyText) {
        if (historyTextArea != null) {
            historyTextArea.setText(historyText);
        }
    }

    public void appendSession(String sessionText) {
        if (historyTextArea != null) {
            historyTextArea.appendText("\n\n" + sessionText);
        }
    }
}
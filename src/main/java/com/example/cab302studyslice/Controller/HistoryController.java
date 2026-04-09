package com.example.cab302studyslice.Controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

public class HistoryController {

    // Linked to  TextArea in history-view.fxml
    @FXML
    private TextArea historyTextArea;


     // Replaces all current history text with new content.
    public void setHistoryText(String historyText) {
        if (historyTextArea != null) {
            historyTextArea.setText(historyText);
        }
    }


     // Adds one session entry to the history page.
    public void appendSession(String sessionText) {
        if (historyTextArea != null) {
            historyTextArea.appendText("\n\n" + sessionText);
        }
    }
}
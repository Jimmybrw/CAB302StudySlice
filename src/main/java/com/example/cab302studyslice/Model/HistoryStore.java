package com.example.cab302studyslice.Model;


//Shared storage for study session history
    //stores history in memory early
public class HistoryStore {

    // Stores all session history text in one place
    private static final StringBuilder historyData = new StringBuilder();

    //Adds the completed study session to the storage
    public static void addSession(String sessionText) {
        if (historyData.length() > 0) {
            historyData.append("\n\n------------------------------\n\n");
        }
        historyData.append(sessionText);
    }


    //Will return the session history as a block of text
    public static String getHistoryText() {
        if (historyData.length() == 0) {
            return "No study history available yet.";
        }
        return historyData.toString();
    }
}
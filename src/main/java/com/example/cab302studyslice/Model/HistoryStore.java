package com.example.cab302studyslice.Model;


/**
 * In-memory storage for study session history.
 * Maintains a single StringBuilder for accumulated session data accessible across the application.
 */
public class HistoryStore {

    // Stores all session history text in one place
    private static final StringBuilder historyData = new StringBuilder();

    /**
     * Adds a completed study session to the history storage.
     * Separates sessions with a delimiter.
     *
     * @param sessionText the formatted session text to add
     */
    public static void addSession(String sessionText) {
        if (historyData.length() > 0) {
            historyData.append("\n\n------------------------------\n\n");
        }
        historyData.append(sessionText);
    }


    /**
     * Retrieves the complete session history as formatted text.
     *
     * @return the accumulated session history, or a placeholder message if no history exists
     */
    public static String getHistoryText() {
        if (historyData.length() == 0) {
            return "No study history available yet.";
        }
        return historyData.toString();
    }
}
// Nothing here yet
package com.example.cab302studyslice.Model;

public class Activity {
    private String sessionId;
    private String sessionLabel;
    private String totalTime;
    private String mostUsedApp;
    private String focusScore;
    private String summary;

    public Activity(String sessionId, String sessionLabel, String mostUsedApp, String focusScore, String summary) {
        this.sessionId = sessionId;
        this.sessionLabel = sessionLabel;
        this.mostUsedApp = mostUsedApp;
        this.focusScore = focusScore;
        this.summary = summary;
    }

    public String getSessionId() {
        return sessionId;
    }
    public String getSessionLabel() {
        return sessionLabel;
    }
    public String getMostUsedApp() {
        return mostUsedApp;
    }
    public String getFocusScore() {
        return focusScore;
    }
    public String getSummary() {
        return summary;
    }
}
package com.example.cab302studyslice.Model;

//Represents one tracked application and the amount of time spent using it
public class Activity {
    private String appName;
    private int duration;

    public Activity() {
    }

    public Activity(String appName, int duration) {
        this.appName = appName;
        this.duration = duration;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }
}

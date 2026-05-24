package com.example.cab302studyslice.Model;

/**
 * Represents a tracked application activity with duration.
 * Records the app name and the amount of time spent using it during a study session.
 */
public class Activity {
    private String appName;
    private int duration;

    /**
     * Creates an empty activity.
     */
    public Activity() {
    }

    /**
     * Creates an activity with the specified app name and duration.
     *
     * @param appName the name of the application
     * @param duration the time spent in seconds
     */
    public Activity(String appName, int duration) {
        this.appName = appName;
        this.duration = duration;
    }

    /**
     * Gets the application name.
     *
     * @return the app name
     */
    public String getAppName() {
        return appName;
    }

    /**
     * Sets the application name.
     *
     * @param appName the app name to set
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * Gets the duration of activity.
     *
     * @return the duration in seconds
     */
    public int getDuration() {
        return duration;
    }

    /**
     * Sets the duration of activity.
     *
     * @param duration the duration in seconds
     */
    public void setDuration(int duration) {
        this.duration = duration;
    }
}

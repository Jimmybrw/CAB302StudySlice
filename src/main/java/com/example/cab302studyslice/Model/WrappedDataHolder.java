package com.example.cab302studyslice.Model;

/**
 * Static singleton that carries WrappedData and the originating session
 * across scene switches in the Wrapped animation sequence.
 * Set it before navigating to wrapped-intro-view.fxml, read it in each slide.
 */
public class WrappedDataHolder {

    private static AiAPI.WrappedData wrappedData;
    private static SessionHistoryEntry session;
    private static String goodHabit;

    public static void set(AiAPI.WrappedData data,
                           SessionHistoryEntry entry,
                           String goodHabitText) {
        wrappedData   = data;
        session       = entry;
        goodHabit     = goodHabitText;
    }

    public static AiAPI.WrappedData getWrappedData() { return wrappedData; }
    public static SessionHistoryEntry getSession()   { return session; }
    public static String getGoodHabit() {
        return goodHabit != null ? goodHabit : "Consistent focus throughout the session";
    }

    public static boolean hasData() {
        return wrappedData != null && session != null;
    }

    public static void clear() {
        wrappedData = null;
        session     = null;
        goodHabit   = null;
    }
}

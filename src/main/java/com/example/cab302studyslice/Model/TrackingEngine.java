package com.example.cab302studyslice.Model;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

/**
 * Core engine that tracks study sessions in real-time.
 * Monitors active window titles from a file and records time spent per application.
 * Maintains a mapping of normalized app names to duration spent.
 */
public class TrackingEngine {
    //Stores the amount of time spend on each detected activity
    private final Map<String, Integer> timeSpent = new LinkedHashMap<>();

    // Remembers the last valid app detected
    private String lastSavedApp = "Desktop";

    // Controls whether the tracking loop is running
    private boolean isRunning = false;

    // Sends updated tracking text back to the UI
    private Consumer<String> uiUpdater;

    // Total study time for the current session in seconds
    private int totalSeconds = 0;

    // Maps raw app keywords to cleaner display names
    private static final Map<String, String> APP_KEYWORDS = new HashMap<>() {{
        put("WINWORD", "WORD");
        put("CODE", "VS CODE");
        put("POWERPNT", "POWERPOINT");
        put("OUTLOOK", "OUTLOOK");
        put("IDEA", "INTELLIJ IDEA");
    }};

    /**
     * Sets the UI updater callback for real-time tracking updates.
     *
     * @param uiUpdater a consumer that receives formatted tracking status strings
     */
    public void setUiUpdater(Consumer<String> uiUpdater) {
        this.uiUpdater = uiUpdater;
    }

    /**
     * Starts the tracking engine.
     * Begins monitoring the active window file in a background thread every second.
     */
    public void startTracking() {
        if (isRunning) return;
        isRunning = true;

        new Thread(() -> {
            String path = System.getProperty("user.dir") + File.separator + "active.txt";
            File file = new File(path);

            while (isRunning) {
                if (file.exists()) {
                    try {
                        List<String> lines = Files.readAllLines(file.toPath());
                        if (!lines.isEmpty()) {
                            String rawTitle = lines.get(0).trim();
                            recordTitle(rawTitle);
                        }
                    } catch (Exception e) {
                        System.err.println("Could not read active window file: " + e.getMessage());
                    }
                }
                try { Thread.sleep(1000); } catch (Exception e) {
                    System.err.println("Tracking thread interrupted: " + e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Gets the total tracked time for the current session.
     *
     * @return total time in seconds
     */
    public int getTotalSeconds() {
        return totalSeconds;
    }

    /**
     * Gets a copy of the activity time map.
     * Maps application names to time spent in seconds.
     *
     * @return a copy of the time spent per application
     */
    public Map<String, Integer> getTimeSpent() {
        return new LinkedHashMap<>(timeSpent);
    }

    /**
     * Stops the tracking engine.
     * Signals the background tracking thread to stop monitoring.
     */
    public void stopTracking() {
        isRunning = false;
    }

    /**
     * Clears all accumulated session data.
     * Resets totals and activity maps for a new tracking session.
     */
    public void reset() {
        timeSpent.clear();
        totalSeconds = 0;
        lastSavedApp = "Desktop";
    }

    void recordTitle(String rawTitle) {
        String currentApp = simplifyTitle(rawTitle);

        if (!currentApp.isEmpty()) {
            lastSavedApp = currentApp;
            int newTime = timeSpent.getOrDefault(lastSavedApp, 0) + 1;
            timeSpent.put(lastSavedApp, newTime);
            totalSeconds++;

            StringBuilder displayData = new StringBuilder();
            for (Map.Entry<String, Integer> entry : timeSpent.entrySet()) {
                displayData.append(entry.getKey()).append(" : ").append(formatTime(entry.getValue())).append("\n");
            }
            //sends updated session data to the UI
            if (uiUpdater != null) {
                uiUpdater.accept("Total Study Time: " + formatTime(totalSeconds) + "\n" + displayData.toString());
            }
        }
    }

    String formatTime(int totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    static String simplifyTitle(String title) {
        if (title == null || title.isEmpty() || title.equalsIgnoreCase("Desktop")) return "DESKTOP";
        String upperTitle = title.toUpperCase();
        
        // Handle Browsers
        if (upperTitle.contains("CHROME") || upperTitle.contains("EDGE") || upperTitle.contains("FIREFOX")) {
            String cleanTitle = title.split(" - ")[0].trim();
            
            // Remove "and X more page(s)" suffix
            if (cleanTitle.contains(" and ") && (cleanTitle.contains(" more page") || cleanTitle.contains(" other page"))) {
                cleanTitle = cleanTitle.replaceAll(" and \\d+ more page.*", "");
                cleanTitle = cleanTitle.replaceAll(" and \\d+ other page.*", "");
            }
            
            return "WEB: " + cleanTitle.trim();
        }

        // Handle specific keywords
        for (Map.Entry<String, String> entry : APP_KEYWORDS.entrySet()) {
            if (upperTitle.contains(entry.getKey())) return entry.getValue();
        }

        return upperTitle.trim();
    }
}

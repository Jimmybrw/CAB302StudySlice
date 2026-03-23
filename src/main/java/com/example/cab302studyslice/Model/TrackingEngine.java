package com.example.cab302studyslice.Model;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public class TrackingEngine {
    private Map<String, Integer> timeSpent = new LinkedHashMap<>();
    private String lastSavedApp = "Desktop";
    private boolean isRunning = false; // Starts as false now
    private Consumer<String> uiUpdater;

    private static final Map<String, String> APP_KEYWORDS = new HashMap<>() {{
        put("WINWORD", "WORD");
        put("CODE", "VS CODE");
        put("POWERPNT", "POWERPOINT");
    }};

    public void setUiUpdater(Consumer<String> uiUpdater) {
        this.uiUpdater = uiUpdater;
    }

    public void startTracking() {
        if (isRunning) return; // Prevent multiple threads from starting
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
                            String currentApp = simplifyTitle(rawTitle);

                            if (!currentApp.isEmpty() && !currentApp.contains("STUDY TRACKER") && !currentApp.contains("CAB302")) {
                                lastSavedApp = currentApp;
                                int newTime = timeSpent.getOrDefault(lastSavedApp, 0) + 1;
                                timeSpent.put(lastSavedApp, newTime);

                                StringBuilder displayData = new StringBuilder("Currently Tracking: " + lastSavedApp + "\n\n");
                                displayData.append("--- ACTIVITY LOG ---\n");
                                for (Map.Entry<String, Integer> entry : timeSpent.entrySet()) {
                                    displayData.append(entry.getKey()).append(" : ").append(entry.getValue()).append("s\n");
                                }

                                if (uiUpdater != null) {
                                    uiUpdater.accept(displayData.toString());
                                }
                            }
                        }
                    } catch (Exception e) {}
                }
                try { Thread.sleep(1000); } catch (Exception e) {}
            }
        }).start();
    }

    // New method to stop the loop
    public void stopTracking() {
        isRunning = false;
    }

    private static String simplifyTitle(String title) {
        if (title == null || title.isEmpty() || title.equalsIgnoreCase("Desktop")) return "DESKTOP";
        String upperTitle = title.toUpperCase();
        if (upperTitle.contains("CHROME") || upperTitle.contains("EDGE") || upperTitle.contains("FIREFOX")) {
            return "WEB: " + title.split(" - ")[0].trim();
        }
        for (Map.Entry<String, String> entry : APP_KEYWORDS.entrySet()) {
            if (upperTitle.contains(entry.getKey())) return entry.getValue();
        }
        return upperTitle.trim();
    }
}
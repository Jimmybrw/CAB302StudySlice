package com.example.cab302studyslice.Model;

import java.io.File;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public class TrackingEngine {
    private Map<String, Integer> timeSpent = new LinkedHashMap<>();
    private String lastSavedApp = "Desktop";
    private boolean isRunning = false;
    private Consumer<String> uiUpdater;
    private int totalSeconds = 0;

    private static final Map<String, String> APP_KEYWORDS = new HashMap<>() {{
        put("WINWORD", "WORD");
        put("CODE", "VS CODE");
        put("POWERPNT", "POWERPOINT");
        put("OUTLOOK", "OUTLOOK");
        put("IDEA", "INTELLIJ IDEA");
    }};

    public void setUiUpdater(Consumer<String> uiUpdater) {
        this.uiUpdater = uiUpdater;
    }

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

                                if (uiUpdater != null) {
                                    uiUpdater.accept("Total Study Time: " + formatTime(totalSeconds) + "\n" + displayData.toString());
                                }
                            }
                        }
                    } catch (Exception e) {}
                }
                try { Thread.sleep(1000); } catch (Exception e) {}
            }
        }).start();
    }

    public int getTotalSeconds() {
        return totalSeconds;
    }

    public void stopTracking() {
        isRunning = false;
    }

    private String formatTime(int totalSeconds) {
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static String simplifyTitle(String title) {
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

package com.example.cab302studyslice.Model;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class AiAPI {

    private static final String API_KEY = "AIzaSyD2SR2XTpOEQjUD-7e7VF32X5sYGLfCF3E";
    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + API_KEY;
    private static final HttpClient httpClient = HttpClient.newHttpClient();

    public static String analyzeSession(SessionHistoryEntry session) {
        try {
            StringBuilder prompt = new StringBuilder();
            prompt.append("Analyze this study session and give me a short, friendly summary.\n\n");
            prompt.append("Session: ").append(session.getTitle()).append("\n");
            prompt.append("Duration: ").append(session.getFormattedTotalTime()).append("\n");
            prompt.append("Apps used:\n");
            for (Activity a : session.getActivities()) {
                prompt.append("- ").append(a.getAppName()).append(": ").append(a.getDuration()).append("s\n");
            }
            prompt.append("\nTell me: most used app, one bad habit noticed, and whether this was a good or bad session. Keep it under 100 words.");

            String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapeJson(prompt.toString()) + "\"}]}]}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return "API Error " + response.statusCode() + ": " + response.body();
            }

            return extractText(response.body());

        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }

    private static String extractText(String body) {
        int start = body.indexOf("\"text\":");
        if (start == -1) return "Could not parse response.";
        start = body.indexOf("\"", start + 7) + 1;

        StringBuilder result = new StringBuilder();
        int i = start;
        while (i < body.length()) {
            char c = body.charAt(i);
            if (c == '\\' && i + 1 < body.length()) {
                char next = body.charAt(i + 1);
                if (next == '"') { result.append('"'); i += 2; continue; }
                if (next == 'n') { result.append('\n'); i += 2; continue; }
                if (next == 't') { result.append('\t'); i += 2; continue; }
                if (next == '\\') { result.append('\\'); i += 2; continue; }
            }
            if (c == '"') break;
            result.append(c);
            i++;
        }
        return result.toString();
    }

    public static class WrappedData {
        public boolean recordTotalTime;
        public String mostUsedApp;
        public int ranking;
        public int totalSessions;
        public String badHabit;
        public String comparedToSessions;
        public int streakCurrent;
        public int score;
    }

    public static WrappedData analyzeSessionStructured(SessionHistoryEntry session, List<SessionHistoryEntry> allSessions) {
        try {
            int totalSessions = allSessions.size();
            int longestSession = allSessions.stream().mapToInt(SessionHistoryEntry::getTotalSeconds).max().orElse(0);
            boolean isLongest = session.getTotalSeconds() >= longestSession;

            // Find most used app from session data
            String mostUsedApp = session.getActivities().stream()
                    .max(java.util.Comparator.comparingInt(Activity::getDuration))
                    .map(Activity::getAppName)
                    .orElse("Unknown");

            StringBuilder prompt = new StringBuilder();
            prompt.append("Analyze this study session. Return ONLY pure JSON, no markdown, no extra text.\n\n");
            prompt.append("Session title: ").append(session.getTitle()).append("\n");
            prompt.append("Duration: ").append(session.getFormattedTotalTime()).append("\n");
            prompt.append("Total sessions by this user: ").append(totalSessions).append("\n");
            prompt.append("Is this the longest session ever: ").append(isLongest).append("\n");
            prompt.append("Apps used:\n");
            for (Activity a : session.getActivities()) {
                prompt.append("- ").append(a.getAppName()).append(": ").append(a.getDuration()).append("s\n");
            }
            prompt.append("\nReturn this exact JSON with no other text:\n");
            prompt.append("{");
            prompt.append("\"score\": integer 0-100 (productivity score),");
            prompt.append("\"ranking\": integer 1-").append(totalSessions).append(" (rank among all sessions, 1=best),");
            prompt.append("\"badHabit\": \"string max 50 chars describing one specific bad habit\",");
            prompt.append("\"comparedToSessions\": \"good\" or \"bad\",");
            prompt.append("\"streakCurrent\": integer (estimated consecutive productive sessions)");
            prompt.append("}");

            String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapeJson(prompt.toString()) + "\"}]}]}";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(API_URL))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return null;

            String text = extractText(response.body());
            int start = text.indexOf("{");
            int end = text.lastIndexOf("}");
            if (start == -1 || end == -1) return null;
            String json = text.substring(start, end + 1);

            WrappedData data = new WrappedData();
            data.score = extractJsonInt(json, "score", 50);
            data.ranking = extractJsonInt(json, "ranking", totalSessions);
            data.totalSessions = totalSessions;
            data.recordTotalTime = isLongest;
            data.mostUsedApp = mostUsedApp;
            data.badHabit = extractJsonString(json, "badHabit");
            data.comparedToSessions = extractJsonString(json, "comparedToSessions");
            data.streakCurrent = extractJsonInt(json, "streakCurrent", 1);
            return data;

        } catch (Exception e) {
            return null;
        }
    }

    private static String extractJsonString(String json, String key) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*)\"").matcher(json);
        return m.find() ? m.group(1) : "";
    }

    private static int extractJsonInt(String json, String key, int defaultVal) {
        java.util.regex.Matcher m = java.util.regex.Pattern.compile("\"" + key + "\"\\s*:\\s*(\\d+)").matcher(json);
        return m.find() ? Integer.parseInt(m.group(1)) : defaultVal;
    }

    private static String escapeJson(String text) {
        return text.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "").replace("\t", " ");
    }
}

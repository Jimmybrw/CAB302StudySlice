package com.example.cab302studyslice.Model;

public class User {
    private static int currentUserId = -1;
    private static String currentUsername = "";

    public static int getCurrentUserId() {
        return currentUserId;
    }

    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    public static String getCurrentUsername() {
        return currentUsername;
    }

    public static void setCurrentUsername(String username) {
        currentUsername = username == null ? "" : username;
    }
}

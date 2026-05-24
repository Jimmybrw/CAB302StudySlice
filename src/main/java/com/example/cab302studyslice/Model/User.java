package com.example.cab302studyslice.Model;

/**
 * Manages the currently logged-in user's session information.
 * Stores the user ID and username as static state for access across the application.
 */
public class User {
    private static int currentUserId = -1;
    private static String currentUsername = "";

    /**
     * Gets the ID of the currently logged-in user.
     *
     * @return the current user ID, or -1 if no user is logged in
     */
    public static int getCurrentUserId() {
        return currentUserId;
    }

    /**
     * Sets the ID of the currently logged-in user.
     *
     * @param userId the user ID to set
     */
    public static void setCurrentUserId(int userId) {
        currentUserId = userId;
    }

    /**
     * Gets the username of the currently logged-in user.
     *
     * @return the current username, or empty string if no user is logged in
     */
    public static String getCurrentUsername() {
        return currentUsername;
    }

    /**
     * Sets the username of the currently logged-in user.
     *
     * @param username the username to set (null is converted to empty string)
     */
    public static void setCurrentUsername(String username) {
        currentUsername = username == null ? "" : username;
    }
}

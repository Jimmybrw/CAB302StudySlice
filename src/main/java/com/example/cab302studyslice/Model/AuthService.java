package com.example.cab302studyslice.Model;

/**
 * Service for handling user authentication logic.
 * Manages login and registration validation, separating authentication concerns from UI controllers.
 */
public class AuthService {
    /**
     * Status codes for authentication operations.
     */
    public enum Status {
        SUCCESS,
        MISSING_FIELDS,
        PASSWORD_MISMATCH,
        USERNAME_EXISTS,
        REGISTER_FAILED,
        INVALID_CREDENTIALS
    }

    /**
     * Result of an authentication operation (login or registration).
     * Contains the outcome status, user ID, username, and a message.
     */
    public record AuthResult(Status status, int userId, String username, String message) {
        /**
         * Checks if the authentication was successful.
         *
         * @return true if status is SUCCESS, false otherwise
         */
        public boolean isSuccess() {
            return status == Status.SUCCESS;
        }
    }

    /**
     * Gateway interface for user data operations.
     * Allows authentication logic to be decoupled from database implementation.
     */
    public interface UserGateway {
        /**
         * Checks if a user exists in the system.
         *
         * @param username the username to check
         * @return true if the user exists, false otherwise
         */
        boolean userExists(String username);

        /**
         * Registers a new user in the system.
         *
         * @param username the username to register
         * @param password the password for the user
         * @return true if registration was successful, false otherwise
         */
        boolean registerUser(String username, String password);

        /**
         * Retrieves the user ID for the given credentials.
         *
         * @param username the username
         * @param password the password
         * @return the user ID if credentials are valid, -1 or less otherwise
         */
        int getUserIdByCredentials(String username, String password);
    }

    /**
     * Creates a UserGateway implementation using the database.
     *
     * @return a UserGateway that delegates to DatabaseManager
     */
    public static UserGateway databaseGateway() {
        return new UserGateway() {
            @Override
            public boolean userExists(String username) {
                return DatabaseManager.userExists(username);
            }

            @Override
            public boolean registerUser(String username, String password) {
                return DatabaseManager.registerUser(username, password);
            }

            @Override
            public int getUserIdByCredentials(String username, String password) {
                return DatabaseManager.getUserIdByCredentials(username, password);
            }
        };
    }
    /**
     * Validates and processes user registration.
     * Checks for empty fields, matching passwords, and username availability.
     *
     * @param username the desired username
     * @param password the desired password
     * @param confirmPassword the password confirmation
     * @param gateway the user data gateway for validation
     * @return an AuthResult indicating success or specific error
     */
    public AuthResult register(String username, String password, String confirmPassword, UserGateway gateway) {
        String cleanUsername = clean(username);
        String cleanPassword = clean(password);
        String cleanConfirmPassword = clean(confirmPassword);

        if (cleanUsername.isEmpty() || cleanPassword.isEmpty() || cleanConfirmPassword.isEmpty()) {
            return new AuthResult(Status.MISSING_FIELDS, -1, cleanUsername, "Please fill in all fields.");
        }

        if (!cleanPassword.equals(cleanConfirmPassword)) {
            return new AuthResult(Status.PASSWORD_MISMATCH, -1, cleanUsername, "Passwords do not match.");
        }

        if (gateway.userExists(cleanUsername)) {
            return new AuthResult(Status.USERNAME_EXISTS, -1, cleanUsername, "Username already exists.");
        }

        if (!gateway.registerUser(cleanUsername, cleanPassword)) {
            return new AuthResult(Status.REGISTER_FAILED, -1, cleanUsername, "Could not create account.");
        }

        return new AuthResult(Status.SUCCESS, -1, cleanUsername, "Account created successfully. Please log in.");
    }
    /**
     * Validates user credentials for login.
     *
     * @param username the username to validate
     * @param password the password to validate
     * @param gateway the user data gateway for credential verification
     * @return an AuthResult with user ID if successful, or error status if validation fails
     */
    public AuthResult login(String username, String password, UserGateway gateway) {
        String cleanUsername = clean(username);
        String cleanPassword = clean(password);

        if (cleanUsername.isEmpty() || cleanPassword.isEmpty()) {
            return new AuthResult(Status.MISSING_FIELDS, -1, cleanUsername, "Please enter both username and password.");
        }

        int userId = gateway.getUserIdByCredentials(cleanUsername, cleanPassword);
        if (userId <= 0) {
            return new AuthResult(Status.INVALID_CREDENTIALS, -1, cleanUsername, "Username or password was incorrect.");
        }

        return new AuthResult(Status.SUCCESS, userId, cleanUsername, "");
    }
    /**
     * Cleans input by trimming whitespace and handling null values.
     *
     * @param value the string to clean
     * @return trimmed string, or empty string if null
     */
    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}

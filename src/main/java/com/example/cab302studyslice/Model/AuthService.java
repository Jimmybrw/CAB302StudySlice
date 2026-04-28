package com.example.cab302studyslice.Model;

//Handles user authentication logic, including login and registration validation.
//This class separates authentication rules from the JavaFX controllers
public class AuthService {
    public enum Status {
        SUCCESS,
        MISSING_FIELDS,
        PASSWORD_MISMATCH,
        USERNAME_EXISTS,
        REGISTER_FAILED,
        INVALID_CREDENTIALS
    }

    public record AuthResult(Status status, int userId, String username, String message) {
        public boolean isSuccess() {
            return status == Status.SUCCESS;
        }
    }

    public interface UserGateway {
        boolean userExists(String username);

        boolean registerUser(String username, String password);

        int getUserIdByCredentials(String username, String password);
    }

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
    //Check that registration fields are valid before creating a new account
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
    //Validates login details and returns the matching user ID if successful
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
    //Removes extra spaces and safely handle null input
    private String clean(String value) {
        return value == null ? "" : value.trim();
    }
}

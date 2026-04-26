package com.example.cab302studyslice.Model;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthServiceTest {
    private final AuthService authService = new AuthService();

    @Test
    void registrationRejectsMismatchedPasswordsBeforeCallingDatabase() {
        FakeUserGateway gateway = new FakeUserGateway();

        AuthService.AuthResult result = authService.register("alex", "secret", "different", gateway);

        assertEquals(AuthService.Status.PASSWORD_MISMATCH, result.status());
        assertEquals("Passwords do not match.", result.message());
        assertFalse(gateway.wasDatabaseCalled());
    }

    @Test
    void loginTrimsCredentialsAndReturnsUserIdForValidUser() {
        FakeUserGateway gateway = new FakeUserGateway();
        gateway.users.put("sam", new FakeUser(42, "study"));

        AuthService.AuthResult result = authService.login("  sam  ", "  study  ", gateway);

        assertTrue(result.isSuccess());
        assertEquals(42, result.userId());
        assertEquals("sam", result.username());
    }

    private static class FakeUserGateway implements AuthService.UserGateway {
        private final Map<String, FakeUser> users = new HashMap<>();
        private boolean databaseCalled = false;

        @Override
        public boolean userExists(String username) {
            databaseCalled = true;
            return users.containsKey(username);
        }

        @Override
        public boolean registerUser(String username, String password) {
            databaseCalled = true;
            users.put(username, new FakeUser(users.size() + 1, password));
            return true;
        }

        @Override
        public int getUserIdByCredentials(String username, String password) {
            databaseCalled = true;
            FakeUser user = users.get(username);
            if (user == null || !user.password().equals(password)) {
                return -1;
            }
            return user.userId();
        }

        boolean wasDatabaseCalled() {
            return databaseCalled;
        }
    }

    private record FakeUser(int userId, String password) {
    }
}

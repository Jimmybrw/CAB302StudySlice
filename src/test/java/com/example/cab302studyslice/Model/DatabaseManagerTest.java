package com.example.cab302studyslice.Model;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabaseManagerTest {
    private String databaseUrl;

    @BeforeEach
    void setUp() throws Exception {
        databaseUrl = "jdbc:h2:mem:studyslice_" + UUID.randomUUID() + ";MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1";
        Class.forName("org.h2.Driver");
        DatabaseManager.setConnectionProviderForTesting(() -> DriverManager.getConnection(databaseUrl));

        try (Connection connection = DriverManager.getConnection(databaseUrl);
             Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE users (
                        user_id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(100) NOT NULL UNIQUE,
                        password VARCHAR(100) NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE sessions (
                        ID INT AUTO_INCREMENT PRIMARY KEY,
                        title VARCHAR(255) NOT NULL,
                        start_time TIMESTAMP,
                        end_time TIMESTAMP,
                        total_time INT,
                        User_ID INT NOT NULL
                    )
                    """);
            statement.execute("""
                    CREATE TABLE session_activities (
                        activity_ID INT AUTO_INCREMENT PRIMARY KEY,
                        session_ID INT NOT NULL,
                        app_name VARCHAR(255) NOT NULL,
                        duration INT NOT NULL
                    )
                    """);
        }
    }

    @AfterEach
    void tearDown() {
        DatabaseManager.resetConnectionProviderForTesting();
    }

    @Test
    void registersUsersAndValidatesCredentialsInIsolatedDatabase() {
        assertFalse(DatabaseManager.userExists("alex"));

        assertTrue(DatabaseManager.registerUser("alex", "secret"));

        assertTrue(DatabaseManager.userExists("alex"));
        assertTrue(DatabaseManager.validateLogin("alex", "secret"));
        assertFalse(DatabaseManager.validateLogin("alex", "wrong-password"));
        assertTrue(DatabaseManager.getUserIdByCredentials("alex", "secret") > 0);
        assertEquals(-1, DatabaseManager.getUserIdByCredentials("missing", "secret"));
    }

    @Test
    void savesFullSessionAndLoadsItBackWithActivities() {
        DatabaseManager.registerUser("sam", "study");
        int userId = DatabaseManager.getUserIdByCredentials("sam", "study");
        LocalDateTime start = LocalDateTime.of(2026, 4, 27, 9, 0);
        LocalDateTime end = LocalDateTime.of(2026, 4, 27, 9, 30);

        boolean saved = DatabaseManager.saveFullSession(
                userId,
                "Assignment block",
                start,
                end,
                1800,
                List.of(
                        new Activity("WORD", 600),
                        new Activity("WEB: Research", 1200)
                )
        );

        List<SessionHistoryEntry> sessions = DatabaseManager.getSessionHistoryByUserId(userId);

        assertTrue(saved);
        assertEquals(1, sessions.size());
        SessionHistoryEntry session = sessions.getFirst();
        assertEquals("Assignment block", session.getTitle());
        assertEquals(start, session.getStartTime());
        assertEquals(end, session.getEndTime());
        assertEquals(1800, session.getTotalSeconds());
        assertEquals("00:30:00", session.getFormattedTotalTime());
        assertEquals(2, session.getActivities().size());
        assertEquals("WEB: Research", session.getActivities().getFirst().getAppName());
        assertEquals(1200, session.getActivities().getFirst().getDuration());
    }

    @Test
    void historyOnlyShowsTheRequestedUsersSessionsNewestFirst() {
        DatabaseManager.registerUser("jordan", "study");
        DatabaseManager.registerUser("taylor", "study");
        int jordanId = DatabaseManager.getUserIdByCredentials("jordan", "study");
        int taylorId = DatabaseManager.getUserIdByCredentials("taylor", "study");

        DatabaseManager.saveFullSession(
                jordanId,
                "Older session",
                LocalDateTime.of(2026, 4, 27, 8, 0),
                LocalDateTime.of(2026, 4, 27, 8, 10),
                600,
                List.of(new Activity("WORD", 600))
        );
        DatabaseManager.saveFullSession(
                taylorId,
                "Other user's session",
                LocalDateTime.of(2026, 4, 27, 9, 0),
                LocalDateTime.of(2026, 4, 27, 9, 20),
                1200,
                List.of(new Activity("OUTLOOK", 1200))
        );
        DatabaseManager.saveFullSession(
                jordanId,
                "Newest session",
                LocalDateTime.of(2026, 4, 27, 10, 0),
                LocalDateTime.of(2026, 4, 27, 10, 30),
                1800,
                List.of(new Activity("WEB: Research", 1800))
        );

        List<SessionHistoryEntry> jordanSessions = DatabaseManager.getSessionHistoryByUserId(jordanId);

        assertEquals(2, jordanSessions.size());
        assertEquals("Newest session", jordanSessions.get(0).getTitle());
        assertEquals("Older session", jordanSessions.get(1).getTitle());
        assertTrue(jordanSessions.stream().noneMatch(session -> session.getTitle().equals("Other user's session")));
    }

    @Test
    void historyLookupReturnsEmptyListForInvalidUserWithoutTouchingDatabase() {
        assertTrue(DatabaseManager.getSessionHistoryByUserId(0).isEmpty());
        assertTrue(DatabaseManager.getSessionHistoryByUserId(-10).isEmpty());
    }
}

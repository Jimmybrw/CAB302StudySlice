package com.example.cab302studyslice.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://studyslice-studyslice.d.aivencloud.com:27251/studyslice?sslMode=REQUIRED";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_dH9MQkrjlY36qW9GWBe";
    private static ConnectionProvider connectionProvider = DatabaseManager::openProductionConnection;

    @FunctionalInterface
    interface ConnectionProvider {
        Connection getConnection() throws Exception;
    }

    // Opens a connection to the hosted database
    public static Connection getConnection() throws Exception {
        return connectionProvider.getConnection();
    }

    static void setConnectionProviderForTesting(ConnectionProvider provider) {
        connectionProvider = provider == null ? DatabaseManager::openProductionConnection : provider;
    }

    static void resetConnectionProviderForTesting() {
        connectionProvider = DatabaseManager::openProductionConnection;
    }

    private static Connection openProductionConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Checks whether the entered login details exist in the users table
    public static boolean validateLogin(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();

        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // Returns the matching user_id for valid login credentials, otherwise -1
    public static int getUserIdByCredentials(String username, String password) {
        String sql = "SELECT user_id FROM users WHERE username = ? AND password = ? LIMIT 1";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("user_id");
            }
            return -1;
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            return -1;
        }
    }

    // Checks whether a username is already taken
    public static boolean userExists(String username) {
        String sql = "SELECT * FROM users WHERE username = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();

        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // Adds a new user to the database
    public static boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users (username, password) VALUES (?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            return statement.executeUpdate() > 0;

        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // Saves one full session and all activities as a single transaction
    public static boolean saveFullSession(int currentUserId,
                                          String title,
                                          LocalDateTime startTime,
                                          LocalDateTime endTime,
                                          int totalTime,
                                          List<Activity> activities) {

        try (Connection connection = getConnection()) {
            boolean originalAutoCommit = connection.getAutoCommit();
            //// Disable auto-commit so the session and activities are saved together
            connection.setAutoCommit(false);
            //// Check whether the database automatically generates IDs
            boolean sessionIdAutoIncrement = isAutoIncrementColumn(connection, "sessions", "ID");
            boolean activityIdAutoIncrement = isAutoIncrementColumn(connection, "session_activities", "activity_ID");
            String sessionSql = sessionIdAutoIncrement
                    ? "INSERT INTO sessions (title, start_time, end_time, total_time, User_ID) VALUES (?, ?, ?, ?, ?)"
                    : "INSERT INTO sessions (ID, title, start_time, end_time, total_time, User_ID) VALUES (?, ?, ?, ?, ?, ?)";
            String activitySql = activityIdAutoIncrement
                    ? "INSERT INTO session_activities (session_ID, app_name, duration) VALUES (?, ?, ?)"
                    : "INSERT INTO session_activities (activity_ID, session_ID, app_name, duration) VALUES (?, ?, ?, ?)";

            try (PreparedStatement sessionStatement = sessionIdAutoIncrement
                         ? connection.prepareStatement(sessionSql, Statement.RETURN_GENERATED_KEYS)
                         : connection.prepareStatement(sessionSql);
                 PreparedStatement activityStatement = connection.prepareStatement(activitySql)) {
                int newSessionId = sessionIdAutoIncrement ? -1 : getNextNumericId(connection, "sessions", "ID");
                if (sessionIdAutoIncrement) {
                    sessionStatement.setString(1, title);
                    sessionStatement.setTimestamp(2, Timestamp.valueOf(startTime));
                    sessionStatement.setTimestamp(3, Timestamp.valueOf(endTime));
                    bindTotalTimeValue(connection, sessionStatement, 4, totalTime);
                    sessionStatement.setInt(5, currentUserId);
                } else {
                    sessionStatement.setInt(1, newSessionId);
                    sessionStatement.setString(2, title);
                    sessionStatement.setTimestamp(3, Timestamp.valueOf(startTime));
                    sessionStatement.setTimestamp(4, Timestamp.valueOf(endTime));
                    bindTotalTimeValue(connection, sessionStatement, 5, totalTime);
                    sessionStatement.setInt(6, currentUserId);
                }

                int sessionInsertCount = sessionStatement.executeUpdate();
                if (sessionInsertCount != 1) {
                    throw new Exception("Session insert failed.");
                }
                if (sessionIdAutoIncrement) {
                    try (ResultSet generatedKeys = sessionStatement.getGeneratedKeys()) {
                        if (!generatedKeys.next()) {
                            throw new Exception("No generated session ID returned.");
                        }
                        newSessionId = generatedKeys.getInt(1);
                    }
                }
                if (newSessionId <= 0) {
                    throw new Exception("Invalid session ID generated.");
                }

                if (activities != null) {
                    int nextActivityId = activityIdAutoIncrement ? -1 : getNextNumericId(connection, "session_activities", "activity_ID");
                    // Save each tracked application as an activity linked to the session
                    for (Activity activity : activities) {
                        if (!activityIdAutoIncrement) {
                            activityStatement.setInt(1, nextActivityId++);
                        }
                        int sessionIndex = activityIdAutoIncrement ? 1 : 2;
                        int appNameIndex = activityIdAutoIncrement ? 2 : 3;
                        int durationIndex = activityIdAutoIncrement ? 3 : 4;
                        activityStatement.setInt(sessionIndex, newSessionId);
                        activityStatement.setString(appNameIndex, activity.getAppName());
                        activityStatement.setInt(durationIndex, activity.getDuration());
                        activityStatement.executeUpdate();
                    }
                }

                connection.commit();
                return true;
            } catch (Exception e) {
                // If anything fails, undo the whole save so partial data is not stored
                connection.rollback();
                System.err.println("Database error: " + e.getMessage());
                return false;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            return false;
        }
    }

    // Loads all saved sessions and related activities for one user
    public static List<SessionHistoryEntry> getSessionHistoryByUserId(int userId) {
        List<SessionHistoryEntry> sessions = new ArrayList<>();
        if (userId <= 0) {
            return sessions;
        }

        String sessionsSql = "SELECT ID, title, start_time, end_time, total_time AS total_time_text " +
                "FROM sessions WHERE User_ID = ? ORDER BY start_time DESC, ID DESC";
        String activitiesSql = "SELECT sa.session_ID, sa.app_name, sa.duration AS duration_text " +
                "FROM session_activities sa INNER JOIN sessions s ON s.ID = sa.session_ID " +
                "WHERE s.User_ID = ? ORDER BY sa.session_ID DESC, sa.duration DESC";

        try (Connection connection = getConnection();
             PreparedStatement sessionsStatement = connection.prepareStatement(sessionsSql);
             PreparedStatement activitiesStatement = connection.prepareStatement(activitiesSql)) {

            Map<Integer, SessionHistoryEntry> sessionsById = new LinkedHashMap<>();

            sessionsStatement.setInt(1, userId);
            try (ResultSet sessionRows = sessionsStatement.executeQuery()) {
                while (sessionRows.next()) {
                    int sessionId = sessionRows.getInt("ID");
                    String title = sessionRows.getString("title");

                    Timestamp startTimestamp = sessionRows.getTimestamp("start_time");
                    Timestamp endTimestamp = sessionRows.getTimestamp("end_time");
                    LocalDateTime startDateTime = startTimestamp == null ? null : startTimestamp.toLocalDateTime();
                    LocalDateTime endDateTime = endTimestamp == null ? null : endTimestamp.toLocalDateTime();

                    int totalSeconds = parseTotalTimeSeconds(sessionRows.getString("total_time_text"));

                    SessionHistoryEntry entry = new SessionHistoryEntry(
                            sessionId,
                            title,
                            startDateTime,
                            endDateTime,
                            totalSeconds
                    );
                    sessionsById.put(sessionId, entry);
                }
            }

            activitiesStatement.setInt(1, userId);
            try (ResultSet activityRows = activitiesStatement.executeQuery()) {
                while (activityRows.next()) {
                    int sessionId = activityRows.getInt("session_ID");
                    SessionHistoryEntry entry = sessionsById.get(sessionId);
                    if (entry != null) {
                        String appName = activityRows.getString("app_name");
                        int duration = parseTotalTimeSeconds(activityRows.getString("duration_text"));
                        entry.addActivity(new Activity(appName, duration));
                    }
                }
            }

            sessions.addAll(sessionsById.values());
            return sessions;
        } catch (Exception e) {
            System.err.println("Database error: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    private static void bindTotalTimeValue(Connection connection,
                                           PreparedStatement statement,
                                           int parameterIndex,
                                           int totalSeconds) throws Exception {
        int columnType = getSessionsTotalTimeColumnType(connection);
        int safeSeconds = Math.max(0, totalSeconds);

        switch (columnType) {
            case Types.TINYINT:
            case Types.SMALLINT:
            case Types.INTEGER:
            case Types.BIGINT:
                statement.setInt(parameterIndex, safeSeconds);
                break;
            case Types.TIME:
                statement.setString(parameterIndex, formatDurationAsTimeString(safeSeconds));
                break;
            case Types.TIMESTAMP:
            case Types.TIMESTAMP_WITH_TIMEZONE:
                statement.setTimestamp(parameterIndex, durationAsTimestamp(safeSeconds));
                break;
            default:
                statement.setString(parameterIndex, formatDurationAsTimeString(safeSeconds));
                break;
        }
    }

    private static int getSessionsTotalTimeColumnType(Connection connection) {
        try (ResultSet columns = connection.getMetaData()
                .getColumns(connection.getCatalog(), null, "sessions", "total_time")) {
            if (columns.next()) {
                return columns.getInt("DATA_TYPE");
            }
        } catch (Exception ignored) {
        }
        return Types.INTEGER;
    }

    private static boolean isAutoIncrementColumn(Connection connection, String tableName, String columnName) {
        try (ResultSet columns = connection.getMetaData()
                .getColumns(connection.getCatalog(), null, tableName, columnName)) {
            if (columns.next()) {
                String autoIncrement = columns.getString("IS_AUTOINCREMENT");
                return "YES".equalsIgnoreCase(autoIncrement);
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    private static int getNextNumericId(Connection connection, String tableName, String idColumn) throws Exception {
        String sql = "SELECT " + idColumn + " FROM " + tableName + " ORDER BY " + idColumn + " DESC LIMIT 1 FOR UPDATE";
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            if (resultSet.next()) {
                return resultSet.getInt(1) + 1;
            }
            return 1;
        }
    }

    private static String formatDurationAsTimeString(int totalSeconds) {
        long hours = totalSeconds / 3600L;
        long minutes = (totalSeconds % 3600L) / 60L;
        long seconds = totalSeconds % 60L;
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private static Timestamp durationAsTimestamp(int totalSeconds) {
        LocalDateTime anchor = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        return Timestamp.valueOf(anchor.plusSeconds(totalSeconds));
    }

    private static int parseTotalTimeSeconds(String rawTotalTime) {
        if (rawTotalTime == null || rawTotalTime.isBlank()) {
            return 0;
        }

        String value = rawTotalTime.trim();
        if (value.matches("\\d+")) {
            return Integer.parseInt(value);
        }

        int spaceIndex = value.indexOf(' ');
        if (spaceIndex >= 0 && spaceIndex < value.length() - 1) {
            value = value.substring(spaceIndex + 1).trim();
        }

        int dotIndex = value.indexOf('.');
        if (dotIndex > 0) {
            value = value.substring(0, dotIndex);
        }

        return parseTimePartsToSeconds(value);
    }

    private static int parseTimePartsToSeconds(String value) {
        if (value == null || value.isBlank()) {
            return 0;
        }

        String[] parts = value.split(":");
        if (parts.length != 3) {
            return 0;
        }

        try {
            int hours = Integer.parseInt(parts[0]);
            int minutes = Integer.parseInt(parts[1]);
            int seconds = Integer.parseInt(parts[2]);
            return (hours * 3600) + (minutes * 60) + seconds;
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    public static List<String> getTopApps(int userId, int limit) {
        String sql = "SELECT sa.app_name, SUM(sa.duration) AS total " +
                     "FROM session_activities sa " +
                     "INNER JOIN sessions s ON s.ID = sa.session_ID " +
                     "WHERE s.User_ID = ? " +
                     "GROUP BY sa.app_name ORDER BY total DESC LIMIT ?";
        List<String> apps = new ArrayList<>();
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            stmt.setInt(2, limit);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) apps.add(rs.getString("app_name"));
            }
        } catch (Exception e) {
            System.err.println("Error getting top apps: " + e.getMessage());
        }
        return apps;
    }

    public static int getLatestWrappedScore(int userId) {
        String sql = "SELECT w.score FROM wrapped w " +
                     "INNER JOIN sessions s ON s.ID = w.session_ID " +
                     "WHERE s.User_ID = ? ORDER BY w.wrapped_ID DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("score");
            }
        } catch (Exception e) {
            System.err.println("Error getting latest score: " + e.getMessage());
        }
        return -1;
    }

    public static int getBestSessionSeconds(int userId) {
        String sql = "SELECT TIMESTAMPDIFF(SECOND, start_time, end_time) AS secs " +
                     "FROM sessions WHERE User_ID = ? AND start_time IS NOT NULL AND end_time IS NOT NULL " +
                     "ORDER BY secs DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("secs");
            }
        } catch (Exception e) {
            System.err.println("Error getting best session: " + e.getMessage());
        }
        return 0;
    }

    public static int getTodaySessionSeconds(int userId) {
        String sql = "SELECT SUM(TIMESTAMPDIFF(SECOND, start_time, end_time)) AS today_secs " +
                     "FROM sessions WHERE User_ID = ? AND DATE(start_time) = CURDATE() " +
                     "AND start_time IS NOT NULL AND end_time IS NOT NULL";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt("today_secs");
            }
        } catch (Exception e) {
            System.err.println("Error getting today's session time: " + e.getMessage());
        }
        return 0;
    }

    public static int getLatestSessionId(int userId) {
        String sql = "SELECT ID FROM sessions WHERE User_ID = ? ORDER BY ID DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (Exception e) {
            System.err.println("Error getting latest session ID: " + e.getMessage());
        }
        return -1;
    }

    /**
     * Deletes a session and all linked data (activities + wrapped) in one transaction.
     */
    public static boolean deleteSession(int sessionId) {
        try (Connection conn = getConnection()) {
            boolean originalAutoCommit = conn.getAutoCommit();
            conn.setAutoCommit(false);
            try {
                // Delete child rows first to respect foreign-key constraints
                try (PreparedStatement s = conn.prepareStatement(
                        "DELETE FROM wrapped WHERE session_ID = ?")) {
                    s.setInt(1, sessionId);
                    s.executeUpdate();
                }
                try (PreparedStatement s = conn.prepareStatement(
                        "DELETE FROM session_activities WHERE session_ID = ?")) {
                    s.setInt(1, sessionId);
                    s.executeUpdate();
                }
                try (PreparedStatement s = conn.prepareStatement(
                        "DELETE FROM sessions WHERE ID = ?")) {
                    s.setInt(1, sessionId);
                    s.executeUpdate();
                }
                conn.commit();
                return true;
            } catch (Exception e) {
                conn.rollback();
                System.err.println("Error deleting session: " + e.getMessage());
                return false;
            } finally {
                conn.setAutoCommit(originalAutoCommit);
            }
        } catch (Exception e) {
            System.err.println("Error deleting session: " + e.getMessage());
            return false;
        }
    }

    /**
     * Fetches the most-recent wrapped record for a given session from the DB.
     * Returns null if no record exists yet.
     */
    public static AiAPI.WrappedData getWrappedDataForSession(int sessionId) {
        String sql = "SELECT score, record_total_time, most_used_app, ranking, " +
                     "bad_habit, compared_to_sessions, streak_current " +
                     "FROM wrapped WHERE session_ID = ? ORDER BY wrapped_ID DESC LIMIT 1";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, sessionId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    AiAPI.WrappedData data = new AiAPI.WrappedData();
                    data.score            = rs.getInt("score");
                    data.recordTotalTime  = rs.getBoolean("record_total_time");
                    data.mostUsedApp      = rs.getString("most_used_app");
                    data.ranking          = rs.getInt("ranking");
                    data.badHabit         = rs.getString("bad_habit");
                    data.comparedToSessions = rs.getString("compared_to_sessions");
                    data.streakCurrent    = rs.getInt("streak_current");
                    return data;
                }
            }
        } catch (Exception e) {
            System.err.println("Error getting wrapped data for session: " + e.getMessage());
        }
        return null;
    }

    public static boolean insertWrappedData(int sessionId, boolean recordTotalTime, String mostUsedApp,
                                            int ranking, String badHabit, String comparedToSessions, int streakCurrent, int score) {
        String sql = "INSERT INTO wrapped (wrapped_ID, session_ID, score, record_total_time, most_used_app, ranking, bad_habit, compared_to_sessions, streak_current) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection connection = getConnection()) {
            int nextId = getNextNumericId(connection, "wrapped", "wrapped_ID");
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setInt(1, nextId);
                statement.setInt(2, sessionId);
                statement.setInt(3, score);
                statement.setBoolean(4, recordTotalTime);
                statement.setString(5, mostUsedApp);
                statement.setInt(6, ranking);
                statement.setString(7, badHabit);
                statement.setString(8, comparedToSessions);
                statement.setInt(9, streakCurrent);
                return statement.executeUpdate() > 0;
            }
        } catch (Exception e) {
            System.err.println("Error inserting wrapped data: " + e.getMessage());
            return false;
        }
    }
}

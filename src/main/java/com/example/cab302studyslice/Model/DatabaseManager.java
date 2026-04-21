package com.example.cab302studyslice.Model;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;

public class DatabaseManager {

    private static final String URL = "jdbc:mysql://studyslice-studyslice.d.aivencloud.com:27251/studyslice?sslMode=REQUIRED";
    private static final String USER = "avnadmin";
    private static final String PASSWORD = "AVNS_dH9MQkrjlY36qW9GWBe";

    // Opens a connection to the hosted database
    public static Connection getConnection() throws Exception {
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            e.printStackTrace();
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
            connection.setAutoCommit(false);
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
                connection.rollback();
                e.printStackTrace();
                return false;
            } finally {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false;
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
}
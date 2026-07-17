package model.database;

import java.sql.*;
import java.util.ArrayList;

public final class DatabaseManager {
    private static final String URL = "jdbc:sqlite:game.db";
    private static final String DEFAULT_SOUND_SETTINGS = "1,1,1,1";

    private DatabaseManager() {
    }

    static {
        initializeDatabase();
    }

    public static void initializeDatabase() {
        String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                "username TEXT PRIMARY KEY, " +
                "password TEXT NOT NULL, " +
                "music_enabled INTEGER NOT NULL DEFAULT 1, " +
                "shot_enabled INTEGER NOT NULL DEFAULT 1, " +
                "collision_enabled INTEGER NOT NULL DEFAULT 1, " +
                "gameover_enabled INTEGER NOT NULL DEFAULT 1" +
                ");";

        String createGameRecordsTable = "CREATE TABLE IF NOT EXISTS game_records (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT NOT NULL, " +
                "final_score INTEGER NOT NULL, " +
                "last_level INTEGER NOT NULL CHECK(last_level BETWEEN 1 AND 8), " +
                "timestamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
                "sound_settings TEXT NOT NULL, " +
                "FOREIGN KEY(username) REFERENCES users(username)" +
                ");";

        String createHighScoreIndex =
                "CREATE INDEX IF NOT EXISTS idx_game_records_high_scores " +
                        "ON game_records(username, final_score DESC)";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement()) {

            statement.execute(createUsersTable);

            migrateUsersTable(connection);

            statement.execute(createGameRecordsTable);
            statement.execute(createHighScoreIndex);

            System.out.println("دیتابیس SQLite متصل شد و جداول بررسی شدند.");
        } catch (SQLException e) {
            System.err.println("خطا در راه‌اندازی اولیه دیتابیس: " + e.getMessage());
        }
    }

    private static Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);

        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }

    private static void migrateUsersTable(Connection connection) throws SQLException {
        boolean musicAdded = addColumnIfMissing(
                connection,
                "users",
                "music_enabled",
                "INTEGER NOT NULL DEFAULT 1"
        );

        boolean shotAdded = addColumnIfMissing(
                connection,
                "users",
                "shot_enabled",
                "INTEGER NOT NULL DEFAULT 1"
        );

        boolean collisionAdded = addColumnIfMissing(
                connection,
                "users",
                "collision_enabled",
                "INTEGER NOT NULL DEFAULT 1"
        );

        boolean gameOverAdded = addColumnIfMissing(
                connection,
                "users",
                "gameover_enabled",
                "INTEGER NOT NULL DEFAULT 1"
        );

        if (!columnExists(connection, "users", "sound_settings")) {
            return;
        }

        if (musicAdded) {
            migrateOldSoundSetting(connection, "music_enabled");
        }

        if (shotAdded) {
            migrateOldSoundSetting(connection, "shot_enabled");
        }

        if (collisionAdded) {
            migrateOldSoundSetting(connection, "collision_enabled");
        }

        if (gameOverAdded) {
            migrateOldSoundSetting(connection, "gameover_enabled");
        }
    }

    private static boolean addColumnIfMissing(Connection connection, String tableName, String columnName, String definition) throws SQLException {
        if (columnExists(connection, tableName, columnName)) {
            return false;
        }

        String sql = "ALTER TABLE " + tableName +
                " ADD COLUMN " + columnName + " " + definition;

        try (Statement statement = connection.createStatement()) {
            statement.execute(sql);
        }

        return true;
    }

    private static boolean columnExists(Connection connection, String tableName, String columnName) throws SQLException {
        String sql = "PRAGMA table_info(" + tableName + ")";

        try (Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                if (columnName.equalsIgnoreCase(resultSet.getString("name"))) {
                    return true;
                }
            }
        }

        return false;
    }

    private static void migrateOldSoundSetting(Connection connection, String newColumn) throws SQLException {
        String sql = "UPDATE users SET " + newColumn + " = " +
                "CASE " +
                "WHEN UPPER(TRIM(COALESCE(sound_settings, 'ON'))) " +
                "IN ('OFF', '0', '0,0,0,0') THEN 0 " +
                "ELSE 1 " +
                "END";

        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        }
    }

    public static boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            if (e.getErrorCode() == 19) {
                System.out.println("نام کاربری '" + username + "' قبلاً ثبت شده است.");
            } else {
                System.err.println("خطا در ثبت‌نام کاربر: " + e.getMessage());
            }

            return false;
        }
    }

    public static boolean loginUser(String username, String password) {
        String sql = "SELECT 1 FROM users WHERE username = ? AND password = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, password);

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            System.err.println("خطا در ورود کاربر: " + e.getMessage());
            return false;
        }
    }

    public static void saveGameRecord(String username, int score, int level, String soundSettings) {
        if (username == null || username.trim().isEmpty()) {
            System.err.println("رکورد بازی ذخیره نشد: کاربری وارد نشده است.");
            return;
        }

        if (level < 1 || level > 8) {
            System.err.println("رکورد بازی ذخیره نشد: سطح باید بین 1 و 8 باشد.");
            return;
        }

        String normalizedSettings = normalizeSoundSettings(soundSettings);

        String sql = "INSERT INTO game_records(" + "username, final_score, last_level, sound_settings" + ") VALUES(?, ?, ?, ?)";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setInt(2, score);
            statement.setInt(3, level);
            statement.setString(4, normalizedSettings);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("خطا در ذخیره رکورد بازی: " + e.getMessage());
        }
    }

    private static String normalizeSoundSettings(String soundSettings) {
        if (soundSettings == null || soundSettings.trim().isEmpty()) {
            return DEFAULT_SOUND_SETTINGS;
        }

        String settings = soundSettings.trim();

        if ("ON".equalsIgnoreCase(settings)) {
            return DEFAULT_SOUND_SETTINGS;
        }

        if ("OFF".equalsIgnoreCase(settings)) {
            return "0,0,0,0";
        }

        String[] parts = settings.split(",");

        if (parts.length != 4) {
            return DEFAULT_SOUND_SETTINGS;
        }

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].trim();

            if (!"0".equals(parts[i]) && !"1".equals(parts[i])) {
                return DEFAULT_SOUND_SETTINGS;
            }
        }

        return String.join(",", parts);
    }

    public static ArrayList<String[]> getHighScores() {
        ArrayList<String[]> highScores = new ArrayList<>();

        String sql = "SELECT username, final_score, last_level, timestamp " +
                "FROM game_records AS record " +
                "WHERE id = (" +
                "SELECT id FROM game_records " +
                "WHERE username = record.username " +
                "ORDER BY final_score DESC, timestamp DESC, id DESC " +
                "LIMIT 1" +
                ") " +
                "ORDER BY final_score DESC, username ASC";

        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(sql)) {

            while (resultSet.next()) {
                highScores.add(new String[]{
                        resultSet.getString("username"),
                        String.valueOf(resultSet.getInt("final_score")),
                        String.valueOf(resultSet.getInt("last_level")),
                        resultSet.getString("timestamp")
                });
            }
        } catch (SQLException e) {
            System.err.println("خطا در استخراج جدول امتیازات: " + e.getMessage());
        }

        return highScores;
    }

    public static void updateSoundSettings(String username, int music, int shot, int collision, int gameOver) {
        String sql = "UPDATE users SET music_enabled = ?, shot_enabled = ?, " +
                "collision_enabled = ?, gameover_enabled = ? " +
                "WHERE username = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, normalizeSoundValue(music));
            statement.setInt(2, normalizeSoundValue(shot));
            statement.setInt(3, normalizeSoundValue(collision));
            statement.setInt(4, normalizeSoundValue(gameOver));
            statement.setString(5, username);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.err.println("خطا در ذخیره تنظیمات صدا: " + e.getMessage());
        }
    }

    private static int normalizeSoundValue(int value) {
        return value == 0 ? 0 : 1;
    }

    public static int[] getSoundSettings(String username) {
        String sql = "SELECT music_enabled, shot_enabled, " + "collision_enabled, gameover_enabled " + "FROM users WHERE username = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new int[]{
                            resultSet.getInt("music_enabled"),
                            resultSet.getInt("shot_enabled"),
                            resultSet.getInt("collision_enabled"),
                            resultSet.getInt("gameover_enabled")
                    };
                }
            }
        } catch (SQLException e) {
            System.err.println("خطا در خواندن تنظیمات صدا: " + e.getMessage());
        }

        return new int[]{1, 1, 1, 1};
    }

    public static int getUserScore(String username) {
        String sql = "SELECT COALESCE(MAX(final_score), 0) AS highest_score " + "FROM game_records WHERE username = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getInt("highest_score");
                }
            }
        } catch (SQLException e) {
            System.err.println("خطا در خواندن امتیاز کاربر: " + e.getMessage());
        }

        return 0;
    }
}
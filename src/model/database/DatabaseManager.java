package model.database;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    private static final String URL = "jdbc:sqlite:game.db"; // یا university.db بر اساس نیازت

    static {
        // ایجاد جدول‌ها در صورت عدم وجود هنگام لود شدن کلاس
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // ۱. جدول کاربران
            String createUsersTable = "CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY, " +
                    "password TEXT NOT NULL, " +
                    "high_score INTEGER DEFAULT 0, " +
                    "music_on INTEGER DEFAULT 1, " +
                    "shoot_sound_on INTEGER DEFAULT 1, " +
                    "hit_sound_on INTEGER DEFAULT 1, " +
                    "gameover_sound_on INTEGER DEFAULT 1, " +
                    "last_level INTEGER DEFAULT 1" +
                    ");";
            stmt.execute(createUsersTable);

            // ۲. جدول تاریخچه بازی‌ها (برای بند ۲.۳)
            String createGamesTable = "CREATE TABLE IF NOT EXISTS games (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT, " +
                    "final_score INTEGER, " +
                    "last_level INTEGER, " +
                    "play_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "settings_summary TEXT, " +
                    "FOREIGN KEY(username) REFERENCES users(username)" +
                    ");";
            stmt.execute(createGamesTable);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // متد ثبت‌نام کاربر جدید (بررسی تکراری نبودن نام کاربری)
    public static boolean registerUser(String username, String password) {
        String query = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true; // ثبت نام موفق
        } catch (SQLException e) {
            // اگر نام کاربری تکراری باشد SQLException رخ می‌دهد
            return false;
        }
    }

    // متد ورود کاربر
    public static boolean loginUser(String username, String password) {
        String query = "SELECT password FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();
            return rs.next(); // اگر رکوردی پیدا شد یعنی یوزر و پس درست است
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // ثبت رکورد بازی پس از شکست یا پیروزی (بند ۲.۳)
    public static void saveGameRecord(String username, int score, int level, String soundSettings) {
        // ۱. ثبت در جدول تاریخچه بازی‌ها
        String insertGame = "INSERT INTO games(username, final_score, last_level, settings_summary) VALUES(?, ?, ?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(insertGame)) {
            pstmt.setString(1, username);
            pstmt.setInt(2, score);
            pstmt.setInt(3, level);
            pstmt.setString(4, soundSettings);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // ۲. به‌روزرسانی بالاترین امتیاز در جدول کاربران (در صورت بیشتر بودن)
        String updateHighScore = "UPDATE users SET high_score = MAX(high_score, ?) WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(updateHighScore)) {
            pstmt.setInt(1, score);
            pstmt.setString(2, username);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // استخراج جدول High Scores (به ازای هر کاربر فقط بالاترین امتیاز - بند ۲.۳)
    public static ArrayList<String[]> getHighScores() {
        ArrayList<String[]> list = new ArrayList<>();
        String query = "SELECT username, MAX(final_score) as top_score, last_level, play_time " +
                "FROM games GROUP BY username ORDER BY top_score DESC LIMIT 10";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("username"),
                        String.valueOf(rs.getInt("top_score")),
                        String.valueOf(rs.getInt("last_level")),
                        rs.getString("play_time")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
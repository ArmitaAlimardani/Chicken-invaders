package model.database;

import java.sql.*;
import java.util.ArrayList;

public class DatabaseManager {
    // آدرس فایل دیتابیس در ریشه پروژه (بند ۲.۲)
    private static final String URL = "jdbc:sqlite:game.db";

    static {
        // بلوک استاتیک برای ساخت خودکار جداول به محض لود شدن کلاس در برنامه
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            // ۱. جدول کاربران (ذخیره اطلاعات پایه و تنظیمات - بند ۲.۲)
            stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "username TEXT PRIMARY KEY, " +
                    "password TEXT NOT NULL, " +
                    "sound_settings TEXT DEFAULT 'ON', " +
                    "last_level INTEGER DEFAULT 1" +
                    ");");

            // ۲. جدول رکوردهای هر بار اجرای بازی (جزئیات پیروزی/شکست - بند ۲.۳)
            stmt.execute("CREATE TABLE IF NOT EXISTS game_records (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "username TEXT, " +
                    "final_score INTEGER, " +
                    "last_level INTEGER, " +
                    "timestamp DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                    "sound_settings TEXT" +
                    ");");

            System.out.println(" دیتابیس SQLite متصل شد و جداول بررسی/ساخته شدند.");
        } catch (SQLException e) {
            System.err.println(" خطا در راه‌اندازی اولیه دیتابیس:");
            e.printStackTrace();
        }
    }

    /**
     * ثبت‌نام کاربر جدید با بررسی تکراری نبودن نام کاربری (بند ۲.۲)
     */
    public static boolean registerUser(String username, String password) {
        String sql = "INSERT INTO users(username, password) VALUES(?, ?)";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            pstmt.executeUpdate();
            return true; // ثبت‌نام موفقیت‌آمیز

        } catch (SQLException e) {
            // در صورت تکراری بودن نام کاربری، SQLite خطای یکتا بودن (Constraint) صادر می‌کند
            System.out.println(" نام کاربری '" + username + "' قبلاً ثبت شده است.");
            return false;
        }
    }

    /**
     * بررسی صحت اطلاعات ورود کاربر (بند ۲.۲)
     */
    public static boolean loginUser(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);
            pstmt.setString(2, password);
            ResultSet rs = pstmt.executeQuery();

            return rs.next(); // اگر رکوردی پیدا شد یعنی نام کاربری و رمز عبور صحیح است

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * ذخیره جزئیات هر بار اجرای بازی پس از پایان (چه پیروزی و چه شکست - بند ۲.۳)
     */
    public static void saveGameRecord(String username, int score, int level, String soundSettings) {
        String insertRecordSql = "INSERT INTO game_records(username, final_score, last_level, sound_settings) VALUES(?, ?, ?, ?)";
        String updateCheckUserSql = "UPDATE users SET highest_score = ?, last_level = ? WHERE username = ? AND highest_score < ?";

        try (Connection conn = DriverManager.getConnection(URL)) {
            conn.setAutoCommit(false); // استفاده از Transaction برای ثبت همزمان

            // ۱. ذخیره جزئیات اجرای بازی
            try (PreparedStatement pstmt1 = conn.prepareStatement(insertRecordSql)) {
                pstmt1.setString(1, username);
                pstmt1.setInt(2, score);
                pstmt1.setInt(3, level);
                pstmt1.setString(4, soundSettings);
                pstmt1.executeUpdate();
            }

            // ۲. به‌روزرسانی بالاترین امتیاز در جدول اصلی کاربران
            try (PreparedStatement pstmt2 = conn.prepareStatement(updateCheckUserSql)) {
                pstmt2.setInt(1, score);
                pstmt2.setInt(2, level);
                pstmt2.setString(3, username);
                pstmt2.setInt(4, score); // فقط اگر امتیاز جدید بیشتر از highest_score فعلی باشد
                pstmt2.executeUpdate();
            }

            conn.commit();
            System.out.println("اطلاعات بازی در هر دو جدول به روز رسانی شد.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * استخراج جدول High Scores همراه با تاریخ آخرین رکورد برتر (بند ۲.۳)
     */
    public static ArrayList<String[]> getHighScores() {
        ArrayList<String[]> list = new ArrayList<>();

        // کوئری هوشمند: گرفتن بالاترین امتیاز هر کاربر همراه با سطح و زمان ثبت آن رکورد
        String sql = "SELECT username, MAX(final_score) AS top_score, last_level, timestamp " +
                "FROM game_records " +
                "GROUP BY username " +
                "ORDER BY top_score DESC";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new String[]{
                        rs.getString("username"),
                        String.valueOf(rs.getInt("top_score")),
                        String.valueOf(rs.getInt("last_level")),
                        rs.getString("timestamp") // دریافت تاریخ و زمان بازی
                });
            }
        } catch (SQLException e) {
            System.err.println(" خطا در استخراج جدول امتیازات با تاریخ:");
            e.printStackTrace();
        }
        return list;
    }

    /**
     * به‌روزرسانی ۴ وضعیت صدا به صورت مستقیم در ستون‌های مجزا
     */
    public static void updateSoundSettings(String username, int music, int shot, int collision, int gameOver) {
        String sql = "UPDATE users SET music_enabled = ?, shot_enabled = ?, collision_enabled = ?, gameover_enabled = ? WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, music);
            pstmt.setInt(2, shot);
            pstmt.setInt(3, collision);
            pstmt.setInt(4, gameOver);
            pstmt.setString(5, username);
            pstmt.executeUpdate();
            System.out.println("ستون‌های صوتی کاربر در دیتابیس به‌روزرسانی شدند.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * دریافت وضعیت ۴ صدا به صورت آرایه عددی [music, shot, collision, gameover]
     */
    public static int[] getSoundSettings(String username) {
        String sql = "SELECT music_enabled, shot_enabled, collision_enabled, gameover_enabled FROM users WHERE username = ?";
        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new int[]{
                        rs.getInt("music_enabled"),
                        rs.getInt("shot_enabled"),
                        rs.getInt("collision_enabled"),
                        rs.getInt("gameover_enabled")
                };
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new int[]{1, 1, 1, 1}; // مقدار پیش‌فرض در صورت نبود رکورد
    }
}
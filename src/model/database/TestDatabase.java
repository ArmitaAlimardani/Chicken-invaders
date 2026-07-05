package model.database;

import java.sql.*;

public class TestDatabase {
    private static final String URL = "jdbc:sqlite:game.db";

    public static void main(String[] args) {
        System.out.println("======  محتویات جدول کاربران (users) ======");
        printTable("SELECT * FROM users");

        System.out.println("\n====== محتویات جدول رکوردها (game_records) ======");
        printTable("SELECT * FROM game_records");
    }



    private static void printTable(String query) {
        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            // چاپ نام ستون‌ها
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(metaData.getColumnName(i) + "\t\t");
            }
            System.out.println("\n------------------------------------------------------------------------------------");

            // چاپ سطرها
            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    System.out.print(rs.getString(i) + "\t\t");
                }
                System.out.println();
            }

        } catch (SQLException e) {
            System.out.println(" خطایی در خواندن دیتابیس رخ داد: " + e.getMessage());
        }
    }
}
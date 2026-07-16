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

            int[] columnWidths = new int[columnCount + 1];
            for (int i = 1; i <= columnCount; i++) {
                columnWidths[i] = Math.max(metaData.getColumnName(i).length(), 15);
            }

            for (int i = 1; i <= columnCount; i++) {
                System.out.print("+");
                for (int j = 0; j < columnWidths[i] + 2; j++) System.out.print("-");
            }
            System.out.println("+");

            for (int i = 1; i <= columnCount; i++) {
                System.out.printf("| %-" + columnWidths[i] + "s ", metaData.getColumnName(i));
            }
            System.out.println("|");

            for (int i = 1; i <= columnCount; i++) {
                System.out.print("+");
                for (int j = 0; j < columnWidths[i] + 2; j++) System.out.print("-");
            }
            System.out.println("+");

            while (rs.next()) {
                for (int i = 1; i <= columnCount; i++) {
                    String columnName = metaData.getColumnName(i);
                    String val = rs.getString(i);

                    if (val == null) {
                        val = "NULL";
                    }
                    else if (columnName.contains("enabled") || columnName.contains("sound") || columnName.contains("music")) {
                        if (val.equals("1")) val = "ON";
                        else if (val.equals("0")) val = "OFF";
                    }

                    System.out.printf("| %-" + columnWidths[i] + "s ", val);
                }
                System.out.println("|");
            }

            for (int i = 1; i <= columnCount; i++) {
                System.out.print("+");
                for (int j = 0; j < columnWidths[i] + 2; j++) System.out.print("-");
            }
            System.out.println("+");

        } catch (SQLException e) {
            System.out.println(" خطایی در خواندن دیتابیس رخ داد: " + e.getMessage());
        }
    }
}
package model.database;

import java.sql.*;

public final class TestDatabase {
    private static final String URL = "jdbc:sqlite:game.db";
    private static final int MIN_COLUMN_WIDTH = 15;

    private TestDatabase() {
    }

    public static void main(String[] args) {
        System.out.println("====== محتویات جدول کاربران (users) ======");
        printTable("SELECT * FROM users");

        System.out.println("\n====== محتویات جدول رکوردها (game_records) ======");
        printTable("SELECT * FROM game_records");
    }

    private static void printTable(String query) {
        try (Connection connection = DriverManager.getConnection(URL);
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {

            ResultSetMetaData metadata = resultSet.getMetaData();
            int columnCount = metadata.getColumnCount();
            int[] columnWidths = calculateColumnWidths(metadata, columnCount);

            printSeparator(columnWidths, columnCount);
            printHeader(metadata, columnWidths, columnCount);
            printSeparator(columnWidths, columnCount);
            printRows(resultSet, metadata, columnWidths, columnCount);
            printSeparator(columnWidths, columnCount);

        } catch (SQLException e) {
            System.out.println("خطایی در خواندن دیتابیس رخ داد: " + e.getMessage());
        }
    }

    private static int[] calculateColumnWidths(ResultSetMetaData metadata, int columnCount)
            throws SQLException {
        int[] columnWidths = new int[columnCount + 1];

        for (int i = 1; i <= columnCount; i++) {
            columnWidths[i] = Math.max(metadata.getColumnName(i).length(), MIN_COLUMN_WIDTH);
        }

        return columnWidths;
    }

    private static void printHeader(ResultSetMetaData metadata, int[] columnWidths,
                                    int columnCount) throws SQLException {
        for (int i = 1; i <= columnCount; i++) {
            System.out.printf("| %-" + columnWidths[i] + "s ", metadata.getColumnName(i));
        }

        System.out.println("|");
    }

    private static void printRows(ResultSet resultSet, ResultSetMetaData metadata,
                                  int[] columnWidths, int columnCount) throws SQLException {
        while (resultSet.next()) {
            for (int i = 1; i <= columnCount; i++) {
                String columnName = metadata.getColumnName(i);
                String value = formatValue(columnName, resultSet.getString(i));
                System.out.printf("| %-" + columnWidths[i] + "s ", value);
            }

            System.out.println("|");
        }
    }

    private static String formatValue(String columnName, String value) {
        if (value == null) {
            return "NULL";
        }

        if (isSoundColumn(columnName)) {
            if (value.equals("1")) {
                return "ON";
            }

            if (value.equals("0")) {
                return "OFF";
            }
        }

        return value;
    }

    private static boolean isSoundColumn(String columnName) {
        return columnName.contains("enabled")
                || columnName.contains("sound")
                || columnName.contains("music");
    }

    private static void printSeparator(int[] columnWidths, int columnCount) {
        for (int i = 1; i <= columnCount; i++) {
            System.out.print("+");
            System.out.print("-".repeat(columnWidths[i] + 2));
        }

        System.out.println("+");
    }
}
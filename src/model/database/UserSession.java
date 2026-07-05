package model.database;

public class UserSession {
    private static String loggedInUser = null;

    public static void setUsername(String username) {
        loggedInUser = username;
    }

    public static String getUsername() {
        return loggedInUser;
    }

    public static boolean isLoggedIn() {
        return loggedInUser != null;
    }

    public static void logout() {
        loggedInUser = null;
    }
}
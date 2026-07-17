import model.database.DatabaseManager;
import view.MainMenu;

import javax.swing.SwingUtilities;

public final class GameMain {

    public static void main(String[] args) {
        DatabaseManager.initializeDatabase();

        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
    }
}
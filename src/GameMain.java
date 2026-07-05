import view.MainMenu;
import javax.swing.SwingUtilities;

public class GameMain {
    public static void main(String[] args) {
        // ۱. ابتدا لود دیتابیس و ساخت جداول
        try {
            Class.forName("model.database.DatabaseManager");
            System.out.println(" دیتابیس با موفقیت آماده‌سازی شد.");
        } catch (ClassNotFoundException e) {
            System.out.println(" خطا در لود درایور دیتابیس!");
            e.printStackTrace();
        }

        // ۲. باز کردن منوی اصلی به جای ورود مستقیم به بازی
        SwingUtilities.invokeLater(() -> {
            MainMenu menu = new MainMenu();
            menu.setVisible(true);
        });
    }
}
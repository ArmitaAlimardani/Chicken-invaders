import javax.swing.*;
import view.GamePanel; // اصلاح آدرس ایمپورت بر اساس ساختار جدید پکیج‌ها

public class GameMain extends JFrame {
    public GameMain() {
        setTitle("Chicken Invaders - AP Project");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // اضافه کردن مستقیم پنل بازی جهت تست
        GamePanel gamePanel = new GamePanel();
        add(gamePanel);

        pack(); // تنظیم اندازه فریم بر اساس PreferredSize پنل (۸۰۰x۶۰۰)
        setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new GameMain().setVisible(true);
        });
    }
}
package view;

import model.database.DatabaseManager;
import model.database.UserSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class MainMenu extends JFrame {

    public MainMenu() {
        // تنظیمات اصلی پنجره منو
        setTitle("منوی اصلی - Chicken Invaders");
        setSize(500, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // وسط‌چین کردن پنجره
        setResizable(false);

        // پنل اصلی با چیدمان BoxLayout عمودی
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(Color.BLACK);
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // عنوان بازی
        JLabel titleLabel = new JLabel("CHICKEN INVADERS");
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // ایجاد فاصله
        mainPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 50)));

        // ساخت دکمه‌ها طبق بند ۲.۱
        JButton btnNewGame = createMenuButton("New Game");
        JButton btnHighScores = createMenuButton("High Scores");
        JButton btnSettings = createMenuButton("Settings");
        JButton btnHowToPlay = createMenuButton("How to Play");
        JButton btnExit = createMenuButton("Exit");

        // اضافه کردن دکمه‌ها به پنل
        mainPanel.add(btnNewGame);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(btnHighScores);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(btnSettings);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(btnHowToPlay);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(btnExit);

        add(mainPanel);

        // ---- پیاده‌سازی اکشن دکمه‌ها ----

        // ۱. دکمه شروع بازی جدید (New Game)
        btnNewGame.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (!UserSession.isLoggedIn()) {
                    JOptionPane.showMessageDialog(MainMenu.this, "ابتدا باید وارد حساب کاربری خود شوید یا ثبت‌نام کنید.");
                    // 👈 اینجا بعداً فرم ورود/ثبت‌نام را باز می‌کنیم
                    openLoginRegisterDialog();
                } else {
                    // کاربر وارد شده، بازی اصلی را استارت می‌زنیم
                    startGame();
                }
            }
        });

        // ۲. جدول بالاترین امتیازها (High Scores)
        btnHighScores.addActionListener(e -> showHighScoresDialog());

        // ۳. تنظیمات صدا (Settings)
        btnSettings.addActionListener(e -> showSettingsDialog());

        // ۴. راهنمای بازی (How to Play)
        btnHowToPlay.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Controls:\n" +
                        "- Move Left/Right: A / D or Arrow Keys\n" +
                        "- Shoot: Spacebar\n\n" +
                        "Defeat the chickens and collect powers to level up!",
                "How to Play", JOptionPane.INFORMATION_MESSAGE));

        // ۵. خروج (Exit)
        btnExit.addActionListener(e -> System.exit(0));
    }

    // متد کمکی برای استایل‌دهی یکدست به دکمه‌ها
    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(Color.DARK_GRAY);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(200, 40));
        return button;
    }

    // متد موقت برای باز کردن فرم لاگین
    private void openLoginRegisterDialog() {
        // گرفتن نام کاربری
        String user = JOptionPane.showInputDialog(this, "نام کاربری:");
        if (user == null || user.trim().isEmpty()) return;

        // گرفتن رمز عبور
        String pass = JOptionPane.showInputDialog(this, "رمز عبور:");
        if (pass == null || pass.trim().isEmpty()) return;

        user = user.trim();
        pass = pass.trim();

        // تلاش برای ثبت‌نام (اگر کاربر از قبل نباشد، ثبت‌نام می‌شود)
        DatabaseManager.registerUser(user, pass);

        // بررسی ورود
        boolean loggedIn = DatabaseManager.loginUser(user, pass);

        if (loggedIn) {
            UserSession.setUsername(user);
            JOptionPane.showMessageDialog(this, "ورود موفقیت‌آمیز بود! خوش آمدید " + user);

            // 🚀 شلیک نهایی: اجرای بازی بلافاصله بعد از تایید پیام لاگین
            startGame();
        } else {
            JOptionPane.showMessageDialog(this, "خطا در ورود یا اطلاعات نادرست است!");
        }
    }

    // نمایش جدول امتیازات
    private void showHighScoresDialog() {
        ArrayList<String[]> scores = DatabaseManager.getHighScores();
        StringBuilder sb = new StringBuilder("🏆 Top High Scores 🏆\n\n");
        sb.append(String.format("%-15s %-10s %-10s %-20s\n", "User", "Score", "Level", "Date"));
        sb.append("-------------------------------------------------------------\n");
        for (String[] row : scores) {
            sb.append(String.format("%-15s %-10s %-10s %-20s\n", row[0], row[1], row[2], row[3]));
        }

        JTextArea textArea = new JTextArea(sb.toString());
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        textArea.setEditable(false);
        JOptionPane.showMessageDialog(this, new JScrollPane(textArea), "High Scores", JOptionPane.PLAIN_MESSAGE);
    }

    // پنجره تنظیمات صدا
    private void showSettingsDialog() {
        // فعلاً یک دیالوگ ساده؛ بعداً چک‌باکس‌های دقیق برای بند ۲.۱ می‌سازیم
        JOptionPane.showMessageDialog(this, "تنظیمات صدا در بخش بعد پیاده‌سازی کامل می‌شود.", "Settings", JOptionPane.INFORMATION_MESSAGE);
    }

    // متد شروع بازی اصلی
    private void startGame() {
        this.setVisible(false); // مخفی کردن منوی اصلی

        JFrame gameFrame = new JFrame("Chicken Invaders - Game");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.setResizable(false);

        // ساخت پنل بازی شما
        view.GamePanel gamePanel = new view.GamePanel(this);

        gameFrame.add(gamePanel);
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);

        // انتقال فوکوس برای کارکرد کیبورد
        gamePanel.requestFocusInWindow();
    }
}
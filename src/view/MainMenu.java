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
        controller.SoundManager.playBackgroundMusic(); // شروع موزیک متن در منو

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
        btnNewGame.addActionListener(e -> {
            // اگر کاربر قبلاً لاگین کرده، مستقیم بازی شروع شود
            if (model.database.UserSession.isLoggedIn()) {
                startGame();
            } else {
                // در غیر این صورت، فرم ورود/ثبت‌نام باز شود
                openLoginRegisterDialog();
            }
        });

        // ۲. جدول بالاترین امتیازها (High Scores) - در کلاس MainMenu
        btnHighScores.addActionListener(e -> {
            // ساخت پنل امتیازات و پاس دادن خودِ منو (this) و پنل اصلی منو (mainPanel)
            HighScorePanel highScorePanel = new HighScorePanel(this, mainPanel);

            this.getContentPane().removeAll();
            this.add(highScorePanel);
            this.revalidate();
            this.repaint();
        });

        // ۳. تنظیمات صدا (Settings)
        btnSettings.addActionListener(e -> {
            SettingsPanel settingsPanel = new SettingsPanel(this, mainPanel);
            this.getContentPane().removeAll();
            this.add(settingsPanel);
            this.revalidate();
            this.repaint();
        });

        // ۴. راهنمای بازی (How to Play)
        btnHowToPlay.addActionListener(e -> JOptionPane.showMessageDialog(this,
                "Controls:\n" +
                        "- Move Left/Right: A / D or Arrow Keys\n" +
                        "- Shoot: Spacebar\n\n" +
                        "Defeat the chickens and collect powers to level up!",
                "How to Play", JOptionPane.INFORMATION_MESSAGE));

        // ۵. خروج (Exit)
        btnExit.addActionListener(e -> System.exit(0));

        //-----------------------------
        //(Setting)
        // ۱. ابتدا تمام اکشن‌های قدیمی و موازی که ممکن است روی این دکمه چسبیده باشند را پاک کن
        for (ActionListener al : btnSettings.getActionListeners()) {
            btnSettings.removeActionListener(al);
        }

        // ۲. حالا اکشن اصلی و کنترل‌شده را قرار بده
        btnSettings.addActionListener(e -> {
            if (model.database.UserSession.isLoggedIn()) {
                SettingsPanel settingsPanel = new SettingsPanel(this, mainPanel);
                this.getContentPane().removeAll();
                this.add(settingsPanel);
                this.revalidate();
                this.repaint();
            } else {
                JOptionPane.showMessageDialog(
                        this,
                        "⚠️ برای دسترسی به تنظیمات صدا، ابتدا باید وارد حساب کاربری خود شوید!",
                        "خطای عدم ورود",
                        JOptionPane.WARNING_MESSAGE
                );
            }
        });

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

    private void openLoginRegisterDialog() {
        AuthDialog authDlg = new AuthDialog(this);
        authDlg.setVisible(true);

        // اگر لاگین در LoginPanel موفق بود، بازی استارت می‌خورد
        if (authDlg.isLoginSucceeded()) {
            startGame();
        }
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
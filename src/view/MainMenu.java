package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainMenu extends JFrame {

    private JButton btnAuth;

    public MainMenu() {
        controller.SoundManager.playBackgroundMusic();

        setTitle("منوی اصلی - Chicken Invaders");
        setSize(500, 650);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // استفاده از پنل سفارشی برای پس‌زمینه
        BackgroundPanel mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // عنوان بازی
        JLabel titleLabel = new JLabel("CHICKEN INVADERS");
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setFont(new Font("Impact", Font.BOLD, 40));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        // ایجاد دکمه‌ها
        btnAuth = createMenuButton("");
        updateAuthButtonText();

        JButton btnNewGame = createMenuButton("Start Game");
        JButton btnHighScores = createMenuButton("High Scores");
        JButton btnSettings = createMenuButton("Settings");
        JButton btnHowToPlay = createMenuButton("How to Play");
        JButton btnExit = createMenuButton("Exit");

        // اضافه کردن به پنل
        mainPanel.add(btnAuth);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(btnNewGame);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(btnHighScores);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(btnSettings);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(btnHowToPlay);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        mainPanel.add(btnExit);

        setContentPane(mainPanel);

        // اکشن‌ها
        btnAuth.addActionListener(e -> handleAuth());
        btnNewGame.addActionListener(e -> { if(model.database.UserSession.isLoggedIn()) startGame(); else JOptionPane.showMessageDialog(this, "ابتدا وارد شوید!"); });
        btnHighScores.addActionListener(e -> { HighScorePanel p = new HighScorePanel(this, mainPanel); setContentPane(p); revalidate(); repaint(); });
        btnSettings.addActionListener(e -> { if(model.database.UserSession.isLoggedIn()) { SettingsPanel p = new SettingsPanel(this, mainPanel); setContentPane(p); revalidate(); repaint(); } });
        btnHowToPlay.addActionListener(e -> { JDialog d = new JDialog(this, "How to Play", true); d.setSize(800, 600); d.add(new HowToPlayPanel(d)); d.setLocationRelativeTo(this); d.setVisible(true); });
        btnExit.addActionListener(e -> showExitConfirmation());

        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) { showExitConfirmation(); }
        });
    }

    //  این کلاس جایگزین BackgroundPanel قبلی در کلاس MainMenu می‌شود
    private class BackgroundPanel extends JPanel {
        private Image backgroundImage;
        private int bgY = 0; // متغیر برای کنترل حرکت عمودی

        public BackgroundPanel() {
            this.backgroundImage = new ImageIcon("icon/background.jpg").getImage();

            // تایمر برای حرکت دادن پس‌زمینه (حدود ۶۰ فریم بر ثانیه)
            Timer timer = new Timer(20, e -> {
                bgY += 1; // سرعت حرکت (هر چه عدد بیشتر باشد، سریع‌تر حرکت می‌کند)
                if (bgY >= getHeight()) {
                    bgY = 0; // بازگشت به شروع برای ایجاد حالت لوپ (Loop)
                }
                repaint(); // درخواست رسم مجدد پنل
            });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (backgroundImage != null) {
                // رسم دو تصویر پشت سر هم برای ایجاد حرکت بی‌وقفه
                g.drawImage(backgroundImage, 0, bgY, getWidth(), getHeight(), this);
                g.drawImage(backgroundImage, 0, bgY - getHeight(), getWidth(), getHeight(), null);
            }
        }
    }

    private void handleAuth() {
        if (model.database.UserSession.isLoggedIn()) {
            if (JOptionPane.showConfirmDialog(this, "خروج از حساب؟", "Logout", JOptionPane.YES_NO_OPTION) == 0) {
                model.database.UserSession.logout();
                updateAuthButtonText();
            }
        } else {
            AuthDialog d = new AuthDialog(this);
            d.setVisible(true);
            updateAuthButtonText();
        }
    }

    private void updateAuthButtonText() {
        if (model.database.UserSession.isLoggedIn()) {
            btnAuth.setText(model.database.UserSession.getUsername() + " (Logout)");
            btnAuth.setBackground(new Color(46, 139, 87));
        } else {
            btnAuth.setText("Login / Register");
            btnAuth.setBackground(new Color(70, 130, 180));
        }
    }

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

    private void showExitConfirmation() {
        // ایجاد دایلاگ مودال بدون حاشیه پیش‌فرض سیستم‌عامل
        JDialog exitDialog = new JDialog(this, "خروج از کهکشان", true);
        exitDialog.setUndecorated(true); // حذف نوار عنوان پیش‌فرض ویندوز
        exitDialog.setSize(450, 220);
        exitDialog.setLocationRelativeTo(this);
        exitDialog.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 3)); // حاشیه نئونی

        // پنل اصلی با گرادینت یا رنگ تیره یکدست
        JPanel panel = new JPanel();
        panel.setBackground(new Color(20, 20, 35));
        panel.setLayout(new BorderLayout(10, 10));

        // متن سوال
        JLabel lblMsg = new JLabel("<html><div style='text-align: center; color: white; font-family: Tahoma;'>" +
                "<b>آیا مطمئن هستید؟</b><br>پیشرفت‌های شما در آخرین مرحله ذخیره خواهد شد.</div></html>", SwingConstants.CENTER);
        lblMsg.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        panel.add(lblMsg, BorderLayout.CENTER);

        // پنل دکمه‌ها
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        btnPanel.setOpaque(false);

        JButton btnYes = createStyledButton("بله، خروج", new Color(200, 50, 50));
        JButton btnNo = createStyledButton("خیر، بازگشت", new Color(50, 150, 50));

        btnYes.addActionListener(e -> System.exit(0));
        btnNo.addActionListener(e -> exitDialog.dispose());

        btnPanel.add(btnYes);
        btnPanel.add(btnNo);
        panel.add(btnPanel, BorderLayout.SOUTH);

        exitDialog.add(panel);
        exitDialog.setVisible(true);
    }

    // متد کمکی برای استایل دادن به دکمه‌ها (می‌توانی به کلاس MainMenu اضافه کنی)
    private JButton createStyledButton(String text, Color color) {
        JButton btn = new JButton(text);
        btn.setPreferredSize(new Dimension(120, 40));
        btn.setBackground(color);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createRaisedBevelBorder());
        btn.setFont(new Font("Tahoma", Font.BOLD, 12));
        return btn;
    }
    private void startGame() {
        this.setVisible(false);
        JFrame gameFrame = new JFrame("Chicken Invaders - Game");
        gameFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        gameFrame.add(new view.GamePanel(this));
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setVisible(true);
    }
}
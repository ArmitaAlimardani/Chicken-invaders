package view;

import model.database.DatabaseManager;
import model.database.UserSession;
import javax.swing.*;
import java.awt.*;

public class SettingsPanel extends JPanel {
    private JCheckBox chkMusic, chkShot, chkCollision, chkGameOver;
    private MainMenu mainMenu;
    private JPanel menuPanel;

    public SettingsPanel(MainMenu mainMenu, JPanel menuPanel) {
        this.mainMenu = mainMenu;
        this.menuPanel = menuPanel;

        setLayout(new BorderLayout());
        setBackground(Color.BLACK);

        // عنوان صفحه
        JLabel titleLabel = new JLabel(" تنظیمات صدای بازی ️", JLabel.CENTER);
        titleLabel.setForeground(Color.CYAN);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
        add(titleLabel, BorderLayout.NORTH);

        // پنل چک‌باکس‌ها
        JPanel centerPanel = new JPanel(new GridLayout(4, 1, 15, 15));
        centerPanel.setBackground(Color.BLACK);
        centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50));

        chkMusic = createStyledCheckBox(" موسیقی زمینه (Background Music)");
        chkShot = createStyledCheckBox(" افکت شلیک (Shot Sound)");
        chkCollision = createStyledCheckBox(" افکت برخورد / انفجار (Crash / Explosion Sound)");
        chkGameOver = createStyledCheckBox(" صدای پایان بازی (Game Over / Win Sound)");

        centerPanel.add(chkMusic);
        centerPanel.add(chkShot);
        centerPanel.add(chkCollision);
        centerPanel.add(chkGameOver);
        add(centerPanel, BorderLayout.CENTER);

        // دکمه ذخیره و بازگشت
        JButton btnSave = new JButton("ذخیره و بازگشت");
        btnSave.setFont(new Font("Arial", Font.BOLD, 16));
        btnSave.setBackground(Color.DARK_GRAY);
        btnSave.setForeground(Color.WHITE);
        btnSave.setFocusPainted(false);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(Color.BLACK);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 30, 0));
        buttonPanel.add(btnSave);
        add(buttonPanel, BorderLayout.SOUTH);

        // بارگذاری تنظیمات فعلی کاربر از دیتابیس
        loadCurrentSettings();

        // اکشن دکمه ذخیره
        btnSave.addActionListener(e -> {
            saveSettings();
            // بازگشت امن به منوی اصلی
            this.mainMenu.getContentPane().removeAll();
            this.mainMenu.add(this.menuPanel);
            this.mainMenu.revalidate();
            this.mainMenu.repaint();
        });
    }

    private JCheckBox createStyledCheckBox(String text) {
        JCheckBox checkBox = new JCheckBox(text);
        checkBox.setFont(new Font("Arial", Font.PLAIN, 16));
        checkBox.setForeground(Color.WHITE);
        checkBox.setBackground(Color.BLACK);
        checkBox.setFocusPainted(false);
        return checkBox;
    }

    private void loadCurrentSettings() {
        if (UserSession.isLoggedIn()) {
            int[] sounds = DatabaseManager.getSoundSettings(UserSession.getUsername());
            chkMusic.setSelected(sounds[0] == 1);
            chkShot.setSelected(sounds[1] == 1);
            chkCollision.setSelected(sounds[2] == 1);
            chkGameOver.setSelected(sounds[3] == 1);
        } else {
            chkMusic.setSelected(true);
            chkShot.setSelected(true);
            chkCollision.setSelected(true);
            chkGameOver.setSelected(true);
        }
    }

    private void saveSettings() {
        int m = chkMusic.isSelected() ? 1 : 0;
        int s = chkShot.isSelected() ? 1 : 0;
        int c = chkCollision.isSelected() ? 1 : 0;
        int g = chkGameOver.isSelected() ? 1 : 0;

        if (UserSession.isLoggedIn()) {
            DatabaseManager.updateSoundSettings(UserSession.getUsername(), m, s, c, g);
        }

        String formatStr = m + "," + s + "," + c + "," + g;
        controller.SoundManager.updateSettings(formatStr);
    }
}
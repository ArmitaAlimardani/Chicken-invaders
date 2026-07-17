package view;

import controller.SoundManager;
import model.database.DatabaseManager;
import model.database.UserSession;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private final JTextField usernameField;
    private final JPasswordField passwordField;
    private final JDialog parentDialog;

    private boolean succeeded;

    public LoginPanel(JDialog parentDialog) {
        this.parentDialog = parentDialog;

        setLayout(new GridLayout(3, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new JLabel("نام کاربری:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("رمز عبور:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton loginButton = new JButton("ورود به بازی");
        loginButton.addActionListener(e -> login());
        add(loginButton);
    }

    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "لطفاً تمام فیلدها را پر کنید.", "خطا", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!DatabaseManager.loginUser(username, password)) {
            JOptionPane.showMessageDialog(this, "نام کاربری یا رمز عبور اشتباه است!", "خطا", JOptionPane.ERROR_MESSAGE);
            return;
        }

        UserSession.setUsername(username);
        loadSoundSettings(username);

        succeeded = true;
        JOptionPane.showMessageDialog(this, "ورود موفقیت‌آمیز بود! خوش آمدید.");

        if (parentDialog != null) {
            parentDialog.dispose();
        }
    }

    private void loadSoundSettings(String username) {
        int[] soundSettings = DatabaseManager.getSoundSettings(username);
        String settings = soundSettings[0] + "," + soundSettings[1] + "," + soundSettings[2] + "," + soundSettings[3];
        SoundManager.updateSettings(settings);
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
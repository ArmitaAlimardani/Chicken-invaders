package view;

import model.database.DatabaseManager;
import model.database.UserSession;
import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {
    private JTextField txtUsername;
    private JPasswordField txtPassword;
    private JDialog parentDialog;
    private boolean succeeded = false;

    public LoginPanel(JDialog parentDialog) {
        this.parentDialog = parentDialog;
        setLayout(new GridLayout(3, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new JLabel("نام کاربری:"));
        txtUsername = new JTextField();
        add(txtUsername);

        add(new JLabel("رمز عبور:"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        JButton btnLogin = new JButton("ورود به بازی");
        add(btnLogin);

        btnLogin.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "لطفاً تمام فیلدها را پر کنید.", "خطا", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (DatabaseManager.loginUser(username, password)) {
                UserSession.setUsername(username); // ذخیره در حافظه تا پایان برنامه (بند ۲.۲)
                succeeded = true;
                JOptionPane.showMessageDialog(this, "ورود موفقیت‌آمیز بود! خوش آمدید.");
                parentDialog.dispose(); // بستن دیالوگ
            } else {
                JOptionPane.showMessageDialog(this, "نام کاربری یا رمز عبور اشتباه است!", "خطا", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    public boolean isSucceeded() {
        return succeeded;
    }
}
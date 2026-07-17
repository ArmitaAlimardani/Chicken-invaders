package view;

import model.database.DatabaseManager;

import javax.swing.*;
import java.awt.*;

public class RegisterPanel extends JPanel {
    private final JTextField usernameField;
    private final JPasswordField passwordField;

    public RegisterPanel() {
        setLayout(new GridLayout(3, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new JLabel("نام کاربری جدید:"));
        usernameField = new JTextField();
        add(usernameField);

        add(new JLabel("رمز عبور:"));
        passwordField = new JPasswordField();
        add(passwordField);

        JButton registerButton = new JButton("ثبت نام حساب");
        registerButton.addActionListener(e -> register());
        add(registerButton);
    }

    private void register() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "لطفاً تمام فیلدها را پر کنید.", "خطا", JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!DatabaseManager.registerUser(username, password)) {
            JOptionPane.showMessageDialog(this, "این نام کاربری قبلاً انتخاب شده است!", "خطا", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "ثبت‌نام با موفقیت انجام شد! حالا می‌توانید از تب ورود وارد شوید.");

        usernameField.setText("");
        passwordField.setText("");
    }
}
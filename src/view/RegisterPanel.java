package view;

import model.database.DatabaseManager;
import javax.swing.*;
import java.awt.*;

public class RegisterPanel extends JPanel {
    private JTextField txtUsername;
    private JPasswordField txtPassword;

    public RegisterPanel() {
        setLayout(new GridLayout(3, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        add(new JLabel("نام کاربری جدید:"));
        txtUsername = new JTextField();
        add(txtUsername);

        add(new JLabel("رمز عبور:"));
        txtPassword = new JPasswordField();
        add(txtPassword);

        JButton btnRegister = new JButton("ثبت نام حساب");
        add(btnRegister);

        btnRegister.addActionListener(e -> {
            String username = txtUsername.getText().trim();
            String password = new String(txtPassword.getPassword()).trim();

            if (username.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(this, "لطفاً تمام فیلدها را پر کنید.", "خطا", JOptionPane.WARNING_MESSAGE);
                return;
            }

            if (DatabaseManager.registerUser(username, password)) {
                JOptionPane.showMessageDialog(this, "ثبت‌نام با موفقیت انجام شد! حالا می‌توانید از تب ورود وارد شوید.");
                txtUsername.setText("");
                txtPassword.setText("");
            } else {
                JOptionPane.showMessageDialog(this, "این نام کاربری قبلاً انتخاب شده است!", "خطا", JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}
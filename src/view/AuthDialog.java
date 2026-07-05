package view;

import javax.swing.*;
import java.awt.*;

public class AuthDialog extends JDialog {
    private LoginPanel loginPanel;
    private RegisterPanel registerPanel;

    public AuthDialog(Frame parent) {
        super(parent, "احراز هویت بازیکن", true);
        setSize(400, 220);
        setLocationRelativeTo(parent);
        setResizable(false);

        JTabbedPane tabbedPane = new JTabbedPane();

        // مقداردهی پنل‌های موجود در پروژه شما
        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel();

        // اضافه کردن پنل‌ها به تب‌ها
        tabbedPane.addTab("ورود (Login)", loginPanel);
        tabbedPane.addTab("ثبت‌نام (Register)", registerPanel);

        add(tabbedPane);
    }

    public boolean isLoginSucceeded() {
        return loginPanel.isSucceeded();
    }
}
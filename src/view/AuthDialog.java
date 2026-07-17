package view;

import javax.swing.*;
import java.awt.*;

public class AuthDialog extends JDialog {
    private final LoginPanel loginPanel;
    private final RegisterPanel registerPanel;

    public AuthDialog(Frame parent) {
        super(parent, "احراز هویت بازیکن", true);

        setSize(400, 220);
        setLocationRelativeTo(parent);
        setResizable(false);

        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel();

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("ورود (Login)", loginPanel);
        tabbedPane.addTab("ثبت‌نام (Register)", registerPanel);

        add(tabbedPane);
    }
}
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

        loginPanel = new LoginPanel(this);
        registerPanel = new RegisterPanel();

        tabbedPane.addTab("ورود (Login)", loginPanel);
        tabbedPane.addTab("ثبت‌نام (Register)", registerPanel);

        add(tabbedPane);
    }

}
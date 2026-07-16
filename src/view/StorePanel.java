package view;

import model.GameConfig;
import model.database.DatabaseManager;
import model.database.UserSession;
import javax.swing.*;
import java.awt.*;

public class StorePanel extends JPanel {

    private final String[] planeNames = {"default", "Fast", "Heavy", "Sniper"};
    private final int[] planeCosts = {0, 5000, 8000, 10000};

    public StorePanel() {
        setLayout(new GridLayout(0, 2, 10, 10));
        setBackground(new Color(20, 20, 35));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        refreshStore();
    }

    public void refreshStore() {
        removeAll();
        for (int i = 0; i < planeNames.length; i++) {
            add(createPlaneCard(planeNames[i], planeCosts[i]));
        }
        revalidate();
        repaint();
    }

    private JPanel createPlaneCard(String name, int cost) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(new Color(40, 40, 60));
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY, 1));

        //  نمایش تصویر
        String fileName = name + ".png";
        ImageIcon icon = new ImageIcon("icon/" + fileName);
        Image img = icon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        card.add(new JLabel(new ImageIcon(img)), BorderLayout.CENTER);

        //  ایجاد پنل برای دکمه و متن قیمت
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(new Color(40, 40, 60));

        //  اضافه کردن متن قیمت زیر دکمه
        String costText = (cost == 0) ? "Free" : "Cost: " + cost + " Points";
        JLabel costLabel = new JLabel(costText, SwingConstants.CENTER);
        costLabel.setForeground(cost == 0 ? Color.GREEN : Color.YELLOW);
        costLabel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        //  ایجاد دکمه Select
        JButton selectBtn = new JButton("Select");
        selectBtn.setBackground(new Color(50, 150, 50));
        selectBtn.setForeground(Color.WHITE);

        selectBtn.addActionListener(e -> {
            int userScore = DatabaseManager.getUserScore(UserSession.getUsername());
            if (userScore >= cost) {
                model.GameConfig.activePlaneName = name;
                JOptionPane.showMessageDialog(this, name + " activated successfully!");
            } else {
                JOptionPane.showMessageDialog(this,
                        "امتیاز شما کافی نیست! مورد نیاز: " + cost, "خطا", JOptionPane.ERROR_MESSAGE);
            }
        });

        bottomPanel.add(selectBtn, BorderLayout.NORTH);
        bottomPanel.add(costLabel, BorderLayout.SOUTH);

        card.add(bottomPanel, BorderLayout.SOUTH);
        return card;
    }
}
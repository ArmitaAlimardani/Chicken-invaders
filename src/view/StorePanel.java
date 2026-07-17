package view;

import model.GameConfig;
import model.database.DatabaseManager;
import model.database.UserSession;

import javax.swing.*;
import java.awt.*;

public class StorePanel extends JPanel {
    private static final String[] PLANE_NAMES = {"default", "Fast", "Heavy", "Sniper"};
    private static final int[] PLANE_COSTS = {0, 5000, 8000, 10000};

    private static final Color BACKGROUND_COLOR = new Color(20, 20, 35);
    private static final Color CARD_COLOR = new Color(40, 40, 60);
    private static final Color BUTTON_COLOR = new Color(50, 150, 50);

    private static final Font PRICE_FONT = new Font("Tahoma", Font.BOLD, 13);
    private static final Font BUTTON_FONT = new Font("Tahoma", Font.BOLD, 14);

    public StorePanel() {
        setLayout(new GridLayout(0, 2, 10, 10));
        setBackground(BACKGROUND_COLOR);
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        refreshStore();
    }

    public void refreshStore() {
        removeAll();

        for (int i = 0; i < PLANE_NAMES.length; i++) {
            add(createPlaneCard(PLANE_NAMES[i], PLANE_COSTS[i]));
        }

        revalidate();
        repaint();
    }

    private JPanel createPlaneCard(String planeName, int cost) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createLineBorder(Color.GRAY));

        card.add(createPlaneImage(planeName), BorderLayout.CENTER);
        card.add(createBottomPanel(planeName, cost), BorderLayout.SOUTH);

        return card;
    }

    private JLabel createPlaneImage(String planeName) {
        ImageIcon planeIcon = new ImageIcon("icon/" + planeName + ".png");
        Image scaledImage = planeIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
        return new JLabel(new ImageIcon(scaledImage), SwingConstants.CENTER);
    }

    private JPanel createBottomPanel(String planeName, int cost) {
        JPanel bottomPanel = new JPanel(new BorderLayout());
        bottomPanel.setBackground(CARD_COLOR);

        JButton selectButton = createSelectButton(planeName, cost);
        JLabel costLabel = createCostLabel(cost);

        bottomPanel.add(selectButton, BorderLayout.NORTH);
        bottomPanel.add(costLabel, BorderLayout.SOUTH);

        return bottomPanel;
    }

    private JLabel createCostLabel(int cost) {
        String costText = cost == 0 ? "Free" : "Cost: " + cost + " Points";

        JLabel costLabel = new JLabel(costText, SwingConstants.CENTER);
        costLabel.setFont(PRICE_FONT);
        costLabel.setForeground(cost == 0 ? Color.GREEN : Color.YELLOW);
        costLabel.setBorder(BorderFactory.createEmptyBorder(7, 0, 7, 0));

        return costLabel;
    }

    private JButton createSelectButton(String planeName, int cost) {
        JButton selectButton = new JButton("Select");
        selectButton.setFont(BUTTON_FONT);
        selectButton.setBackground(BUTTON_COLOR);
        selectButton.setForeground(Color.WHITE);
        selectButton.setFocusPainted(false);
        selectButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        selectButton.addActionListener(e -> selectPlane(planeName, cost));
        return selectButton;
    }

    private void selectPlane(String planeName, int cost) {
        if (!UserSession.isLoggedIn()) {
            JOptionPane.showMessageDialog(this,
                    "برای انتخاب هواپیما ابتدا وارد حساب کاربری شوید.",
                    "خطا", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int userScore = DatabaseManager.getUserScore(UserSession.getUsername());

        if (userScore < cost) {
            JOptionPane.showMessageDialog(this,
                    "امتیاز شما کافی نیست! مورد نیاز: " + cost,
                    "خطا", JOptionPane.ERROR_MESSAGE);
            return;
        }

        GameConfig.activePlaneName = planeName;
        JOptionPane.showMessageDialog(this, planeName + " activated successfully!");
    }
}
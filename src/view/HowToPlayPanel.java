package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class HowToPlayPanel extends JPanel {
    private final JDialog dialog;
    private final Timer animationTimer;
    private Image backgroundImage;
    private int backgroundY;

    public HowToPlayPanel(JDialog dialog) {
        this.dialog = dialog;

        setPreferredSize(new Dimension(800, 600));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        loadBackgroundImage();

        add(Box.createVerticalStrut(25));
        add(createTitleLabel());
        add(Box.createVerticalStrut(20));
        add(createGuidePanel());
        add(Box.createVerticalStrut(20));
        add(createBackButton());

        animationTimer = new Timer(16, e -> {
            backgroundY++;

            if (backgroundY >= getHeight()) {
                backgroundY = 0;
            }

            repaint();
        });

        animationTimer.start();
    }

    private void loadBackgroundImage() {
        ImageIcon backgroundIcon = new ImageIcon("icon/background.jpg");

        if (backgroundIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            backgroundImage = backgroundIcon.getImage();
        }
    }

    private JLabel createTitleLabel() {
        JLabel titleLabel = new JLabel("HOW TO PLAY");
        titleLabel.setFont(new Font("Impact", Font.PLAIN, 40));
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return titleLabel;
    }

    private JPanel createGuidePanel() {
        JPanel guidePanel = new JPanel();
        guidePanel.setLayout(new BoxLayout(guidePanel, BoxLayout.Y_AXIS));
        guidePanel.setOpaque(false);
        guidePanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        guidePanel.add(createSectionTitle("CONTROLS", new Color(0, 191, 255)));
        guidePanel.add(Box.createVerticalStrut(8));
        guidePanel.add(createControlRow("Move Left:", "LEFT Arrow / A"));
        guidePanel.add(createControlRow("Move Right:", "RIGHT Arrow / D"));
        guidePanel.add(createControlRow("Move Up:", "UP Arrow / W"));
        guidePanel.add(createControlRow("Move Down:", "DOWN Arrow / S"));
        guidePanel.add(createControlRow("Shoot:", "SPACEBAR"));
        guidePanel.add(createControlRow("Pause Game:", "P Key"));

        guidePanel.add(Box.createVerticalStrut(20));
        guidePanel.add(createSectionTitle("GAME RULES", new Color(255, 69, 0)));
        guidePanel.add(Box.createVerticalStrut(8));
        guidePanel.add(createRuleLabel("مرغ‌ها را قبل از رسیدن به انتهای صفحه نابود کنید."));
        guidePanel.add(createRuleLabel("با جمع‌آوری پاورآپ‌ها، قدرت شلیک خود را ارتقا دهید."));
        guidePanel.add(createRuleLabel("بمب یخ‌زن دشمنان را به مدت ۳ ثانیه منجمد می‌کند."));
        guidePanel.add(createRuleLabel("در مراحل ۴ و ۸ آماده رویارویی با غول بزرگ باشید!"));

        return guidePanel;
    }

    private JLabel createSectionTitle(String text, Color color) {
        JLabel titleLabel = new JLabel(text);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(color);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        return titleLabel;
    }

    private JPanel createControlRow(String action, String key) {
        JPanel rowPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));
        rowPanel.setOpaque(false);
        rowPanel.setMaximumSize(new Dimension(500, 28));

        JLabel actionLabel = new JLabel(action);
        actionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        actionLabel.setForeground(new Color(224, 224, 224));

        JLabel keyLabel = new JLabel(key);
        keyLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        keyLabel.setForeground(Color.WHITE);

        rowPanel.add(actionLabel);
        rowPanel.add(keyLabel);
        return rowPanel;
    }

    private JLabel createRuleLabel(String text) {
        JLabel ruleLabel = new JLabel(text, SwingConstants.CENTER);
        ruleLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        ruleLabel.setForeground(new Color(224, 224, 224));
        ruleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        ruleLabel.setMaximumSize(new Dimension(650, 28));
        return ruleLabel;
    }

    private JButton createBackButton() {
        JButton backButton = new JButton("BACK");
        backButton.setFont(new Font("Arial", Font.BOLD, 15));
        backButton.setBackground(new Color(45, 45, 45));
        backButton.setForeground(Color.WHITE);
        backButton.setFocusable(false);
        backButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        backButton.setMaximumSize(new Dimension(140, 40));
        backButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        backButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                backButton.setBackground(Color.DARK_GRAY);
            }

            @Override
            public void mouseExited(MouseEvent event) {
                backButton.setBackground(new Color(45, 45, 45));
            }
        });

        backButton.addActionListener(e -> closeDialog());
        return backButton;
    }

    private void closeDialog() {
        animationTimer.stop();

        if (dialog != null) {
            dialog.dispose();
        }
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics.create();

        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics2D.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        if (backgroundImage != null) {
            graphics2D.drawImage(backgroundImage, 0, backgroundY, getWidth(), getHeight(), this);
            graphics2D.drawImage(backgroundImage, 0, backgroundY - getHeight(), getWidth(), getHeight(), this);
        } else {
            graphics2D.setColor(new Color(10, 10, 20));
            graphics2D.fillRect(0, 0, getWidth(), getHeight());
        }

        graphics2D.dispose();
    }
}
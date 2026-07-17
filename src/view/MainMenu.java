package view;

import controller.SoundManager;
import model.database.UserSession;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class MainMenu extends JFrame {
    private static final int WINDOW_WIDTH = 500;
    private static final int WINDOW_HEIGHT = 650;
    private static final int MENU_BUTTON_WIDTH = 200;
    private static final int MENU_BUTTON_HEIGHT = 40;
    private static final String BACKGROUND_PATH = "icon/background.jpg";

    private final JButton authButton;
    private final BackgroundPanel mainPanel;

    public MainMenu() {
        SoundManager.playBackgroundMusic();
        configureFrame();

        mainPanel = new BackgroundPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        authButton = createMenuButton("");
        initializeMenu();
        updateAuthButtonText();

        setContentPane(mainPanel);
        addWindowClosingListener();
    }

    private void configureFrame() {
        setTitle("منوی اصلی - Chicken Invaders");
        setSize(WINDOW_WIDTH, WINDOW_HEIGHT);
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    private void initializeMenu() {
        JLabel titleLabel = new JLabel("CHICKEN INVADERS");
        titleLabel.setForeground(Color.YELLOW);
        titleLabel.setFont(new Font("Impact", Font.BOLD, 40));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton newGameButton = createMenuButton("Start Game");
        JButton storeButton = createMenuButton("Store");
        JButton highScoresButton = createMenuButton("High Scores");
        JButton settingsButton = createMenuButton("Settings");
        JButton howToPlayButton = createMenuButton("How to Play");
        JButton exitButton = createMenuButton("Exit");

        mainPanel.add(Box.createRigidArea(new Dimension(0, 50)));
        mainPanel.add(titleLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 40)));

        addMenuButton(authButton);
        addMenuButton(newGameButton);
        addMenuButton(storeButton);
        addMenuButton(highScoresButton);
        addMenuButton(settingsButton);
        addMenuButton(howToPlayButton);
        mainPanel.add(exitButton);

        authButton.addActionListener(e -> handleAuth());
        newGameButton.addActionListener(e -> handleNewGame());
        storeButton.addActionListener(e -> handleStore());
        highScoresButton.addActionListener(e -> showPanel(new HighScorePanel(this, mainPanel)));
        settingsButton.addActionListener(e -> handleSettings());
        howToPlayButton.addActionListener(e -> showHowToPlayDialog());
        exitButton.addActionListener(e -> showExitConfirmation());
    }

    private void addMenuButton(JButton button) {
        mainPanel.add(button);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 15)));
    }

    private void addWindowClosingListener() {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                showExitConfirmation();
            }
        });
    }

    private void handleAuth() {
        if (UserSession.isLoggedIn()) {
            int selectedOption = JOptionPane.showConfirmDialog(this, "خروج از حساب؟", "Logout", JOptionPane.YES_NO_OPTION);

            if (selectedOption == JOptionPane.YES_OPTION) {
                UserSession.logout();
                updateAuthButtonText();
            }
        } else {
            AuthDialog authDialog = new AuthDialog(this);
            authDialog.setVisible(true);
            updateAuthButtonText();
        }
    }

    private void handleNewGame() {
        if (ensureUserLoggedIn()) {
            startGame();
        }
    }

    private void handleStore() {
        if (!ensureUserLoggedIn()) {
            return;
        }

        JDialog storeDialog = new JDialog(this, "Store", true);
        storeDialog.setSize(500, 400);
        storeDialog.add(new StorePanel());
        storeDialog.setLocationRelativeTo(this);
        storeDialog.setVisible(true);
    }

    private void handleSettings() {
        if (ensureUserLoggedIn()) {
            showPanel(new SettingsPanel(this, mainPanel));
        }
    }

    private boolean ensureUserLoggedIn() {
        if (UserSession.isLoggedIn()) {
            return true;
        }

        JOptionPane.showMessageDialog(this, "ابتدا وارد شوید!");
        return false;
    }

    private void showPanel(JPanel panel) {
        setContentPane(panel);
        revalidate();
        repaint();
    }

    private void showHowToPlayDialog() {
        JDialog howToPlayDialog = new JDialog(this, "How to Play", true);
        howToPlayDialog.setSize(800, 600);
        howToPlayDialog.add(new HowToPlayPanel(howToPlayDialog));
        howToPlayDialog.setLocationRelativeTo(this);
        howToPlayDialog.setVisible(true);
    }

    private void updateAuthButtonText() {
        if (UserSession.isLoggedIn()) {
            authButton.setText(UserSession.getUsername() + " (Logout)");
            authButton.setBackground(new Color(46, 139, 87));
        } else {
            authButton.setText("Login / Register");
            authButton.setBackground(new Color(70, 130, 180));
        }
    }

    private JButton createMenuButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Arial", Font.BOLD, 18));
        button.setForeground(Color.WHITE);
        button.setBackground(Color.DARK_GRAY);
        button.setFocusPainted(false);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setMaximumSize(new Dimension(MENU_BUTTON_WIDTH, MENU_BUTTON_HEIGHT));
        return button;
    }

    private void showExitConfirmation() {
        JDialog exitDialog = new JDialog(this, "خروج از کهکشان", true);
        exitDialog.setUndecorated(true);
        exitDialog.setSize(450, 220);
        exitDialog.setLocationRelativeTo(this);
        exitDialog.getRootPane().setBorder(BorderFactory.createLineBorder(new Color(100, 149, 237), 3));

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(new Color(20, 20, 35));

        String message = "<html><div style='text-align: center; color: white; font-family: Tahoma;'>" +
                "<b>آیا مطمئن هستید؟</b><br>پیشرفت‌های شما در آخرین مرحله ذخیره خواهد شد.</div></html>";

        JLabel messageLabel = new JLabel(message, SwingConstants.CENTER);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(20, 0, 10, 0));
        panel.add(messageLabel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        buttonPanel.setOpaque(false);

        JButton yesButton = createStyledButton("بله، خروج", new Color(200, 50, 50));
        JButton noButton = createStyledButton("خیر، بازگشت", new Color(50, 150, 50));

        yesButton.addActionListener(e -> System.exit(0));
        noButton.addActionListener(e -> exitDialog.dispose());

        buttonPanel.add(yesButton);
        buttonPanel.add(noButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        exitDialog.add(panel);
        exitDialog.setVisible(true);
    }

    private JButton createStyledButton(String text, Color color) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(120, 40));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createRaisedBevelBorder());
        button.setFont(new Font("Tahoma", Font.BOLD, 12));
        return button;
    }

    private void startGame() {
        setVisible(false);

        JFrame gameFrame = new JFrame("Chicken Invaders - Game");
        gameFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        gameFrame.add(new GamePanel(this));
        gameFrame.pack();
        gameFrame.setLocationRelativeTo(null);
        gameFrame.setResizable(false);
        gameFrame.setVisible(true);
    }

    private static class BackgroundPanel extends JPanel {
        private static final int TIMER_DELAY = 20;
        private static final int SCROLL_SPEED = 1;

        private final Image backgroundImage;
        private int backgroundY;

        public BackgroundPanel() {
            backgroundImage = new ImageIcon(BACKGROUND_PATH).getImage();

            Timer timer = new Timer(TIMER_DELAY, e -> {
                backgroundY += SCROLL_SPEED;

                if (backgroundY >= getHeight()) {
                    backgroundY = 0;
                }

                repaint();
            });

            timer.start();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);

            if (backgroundImage == null) {
                return;
            }

            graphics.drawImage(backgroundImage, 0, backgroundY, getWidth(), getHeight(), this);
            graphics.drawImage(backgroundImage, 0, backgroundY - getHeight(), getWidth(), getHeight(), this);
        }
    }
}
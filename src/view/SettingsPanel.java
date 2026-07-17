package view;

import controller.SoundManager;
import model.database.DatabaseManager;
import model.database.UserSession;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

public class SettingsPanel extends JPanel {
    private final JCheckBox musicCheckBox;
    private final JCheckBox shotCheckBox;
    private final JCheckBox collisionCheckBox;
    private final JCheckBox gameOverCheckBox;
    private final MainMenu mainMenu;
    private final JPanel menuPanel;

    public SettingsPanel(MainMenu mainMenu, JPanel menuPanel) {
        this.mainMenu = mainMenu;
        this.menuPanel = menuPanel;

        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(30, 60, 35, 60));

        JLabel titleLabel = new JLabel("SOUND SETTINGS", JLabel.CENTER);
        titleLabel.setFont(new Font("Impact", Font.PLAIN, 30));
        titleLabel.setForeground(new Color(70, 215, 255));
        titleLabel.setBorder(new EmptyBorder(10, 0, 25, 0));
        add(titleLabel, BorderLayout.NORTH);

        musicCheckBox = createCheckBox();
        shotCheckBox = createCheckBox();
        collisionCheckBox = createCheckBox();
        gameOverCheckBox = createCheckBox();

        JPanel settingsCard = new JPanel();
        settingsCard.setLayout(new BoxLayout(settingsCard, BoxLayout.Y_AXIS));
        settingsCard.setOpaque(false);
        settingsCard.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(65, 115, 180), 2),
                new EmptyBorder(22, 25, 22, 25)
        ));

        settingsCard.add(createSoundRow(musicCheckBox, "موسیقی زمینه", "Background Music"));
        settingsCard.add(Box.createVerticalStrut(12));
        settingsCard.add(createSoundRow(shotCheckBox, "صدای شلیک", "Shot Sound"));
        settingsCard.add(Box.createVerticalStrut(12));
        settingsCard.add(createSoundRow(collisionCheckBox, "صدای برخورد و انفجار", "Crash / Explosion Sound"));
        settingsCard.add(Box.createVerticalStrut(12));
        settingsCard.add(createSoundRow(gameOverCheckBox, "صدای برد و پایان بازی", "Game Over / Win Sound"));

        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);
        centerPanel.add(settingsCard);
        add(centerPanel, BorderLayout.CENTER);

        JButton saveButton = createSaveButton();

        JPanel buttonPanel = new JPanel();
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(new EmptyBorder(25, 0, 0, 0));
        buttonPanel.add(saveButton);
        add(buttonPanel, BorderLayout.SOUTH);

        loadCurrentSettings();
    }

    private JCheckBox createCheckBox() {
        JCheckBox checkBox = new JCheckBox();
        checkBox.setOpaque(false);
        checkBox.setFocusPainted(false);
        checkBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        checkBox.setPreferredSize(new Dimension(28, 28));
        return checkBox;
    }

    private JPanel createSoundRow(JCheckBox checkBox, String persianText, String englishText) {
        JPanel rowPanel = new JPanel(new BorderLayout(15, 0));
        rowPanel.setBackground(new Color(24, 55, 75));
        rowPanel.setBorder(new EmptyBorder(9, 18, 9, 18));
        rowPanel.setPreferredSize(new Dimension(620, 70));
        rowPanel.setMaximumSize(new Dimension(620, 70));

        JLabel persianLabel = new JLabel(persianText, JLabel.RIGHT);
        persianLabel.setFont(new Font("Tahoma", Font.BOLD, 15));
        persianLabel.setForeground(new Color(105, 230, 255));
        persianLabel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        JLabel englishLabel = new JLabel(englishText, JLabel.RIGHT);
        englishLabel.setFont(new Font("Tahoma", Font.PLAIN, 13));
        englishLabel.setForeground(new Color(185, 205, 220));

        JPanel textPanel = new JPanel(new GridLayout(2, 1, 0, 3));
        textPanel.setOpaque(false);
        textPanel.add(persianLabel);
        textPanel.add(englishLabel);

        JPanel checkBoxPanel = new JPanel(new GridBagLayout());
        checkBoxPanel.setOpaque(false);
        checkBoxPanel.add(checkBox);

        rowPanel.add(textPanel, BorderLayout.CENTER);
        rowPanel.add(checkBoxPanel, BorderLayout.EAST);

        return rowPanel;
    }

    private JButton createSaveButton() {
        JButton saveButton = new JButton("ذخیره و بازگشت");
        saveButton.setFont(new Font("Tahoma", Font.BOLD, 14));
        saveButton.setForeground(Color.WHITE);
        saveButton.setBackground(new Color(40, 110, 160));
        saveButton.setFocusPainted(false);
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.setPreferredSize(new Dimension(190, 44));
        saveButton.setBorder(BorderFactory.createLineBorder(new Color(85, 210, 255)));

        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent event) {
                saveButton.setBackground(new Color(50, 140, 195));
            }

            @Override
            public void mouseExited(MouseEvent event) {
                saveButton.setBackground(new Color(40, 110, 160));
            }
        });

        saveButton.addActionListener(e -> {
            saveSettings();
            returnToMainMenu();
        });

        return saveButton;
    }

    private void loadCurrentSettings() {
        if (!UserSession.isLoggedIn()) {
            setAllSoundsEnabled();
            return;
        }

        int[] soundSettings = DatabaseManager.getSoundSettings(UserSession.getUsername());
        musicCheckBox.setSelected(soundSettings[0] == 1);
        shotCheckBox.setSelected(soundSettings[1] == 1);
        collisionCheckBox.setSelected(soundSettings[2] == 1);
        gameOverCheckBox.setSelected(soundSettings[3] == 1);
    }

    private void setAllSoundsEnabled() {
        musicCheckBox.setSelected(true);
        shotCheckBox.setSelected(true);
        collisionCheckBox.setSelected(true);
        gameOverCheckBox.setSelected(true);
    }

    private void saveSettings() {
        int music = musicCheckBox.isSelected() ? 1 : 0;
        int shot = shotCheckBox.isSelected() ? 1 : 0;
        int collision = collisionCheckBox.isSelected() ? 1 : 0;
        int gameOver = gameOverCheckBox.isSelected() ? 1 : 0;

        if (UserSession.isLoggedIn()) {
            DatabaseManager.updateSoundSettings(UserSession.getUsername(), music, shot, collision, gameOver);
        }

        String settings = music + "," + shot + "," + collision + "," + gameOver;
        SoundManager.updateSettings(settings);
    }

    private void returnToMainMenu() {
        mainMenu.setContentPane(menuPanel);
        mainMenu.revalidate();
        mainMenu.repaint();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);

        Graphics2D graphics2D = (Graphics2D) graphics.create();
        GradientPaint gradient = new GradientPaint(
                0, 0, new Color(4, 9, 25),
                0, getHeight(), new Color(16, 30, 60)
        );

        graphics2D.setPaint(gradient);
        graphics2D.fillRect(0, 0, getWidth(), getHeight());
        graphics2D.dispose();
    }
}
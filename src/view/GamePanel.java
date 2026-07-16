package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

import controller.SoundManager;
import model.*;
import model.enemy.Boss;
import model.enemy.Enemy;

import model.database.DatabaseManager;
import model.database.UserSession;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;
    private static final int FRAME_DELAY_MS = 16;
    private static final int BACKGROUND_SCROLL_SPEED = 2;

    private static final int START_LEVEL = 1;
    private static final int MID_BOSS_LEVEL = 4;
    private static final int FINAL_BOSS_LEVEL = 8;

    private static final int NORMAL_LEVEL_BONUS = 200;
    private static final int MID_BOSS_BONUS = 500;
    private static final int FINAL_BOSS_BONUS = 1000;

    private static final int FREEZE_DURATION_MS = 3_000;
    private static final int LIFE_MESSAGE_DURATION_MS = 1_500;
    private static final double POWER_UP_DROP_CHANCE = 0.20;
    private static final int BULLET_SPACING = 25;

    private static final String BACKGROUND_PATH = "icon/background.jpg";
    private static final String SOUND_SETTINGS_SNAPSHOT = "ON";

    private final MainMenu mainMenu;
    private final Timer gameTimer;
    private final Plane plane;
    private final GridManager gridManager;

    private final ArrayList<Bullet> bullets = new ArrayList<>();
    private final ArrayList<Enemy> enemies = new ArrayList<>();
    private final ArrayList<Egg> eggs = new ArrayList<>();
    private final ArrayList<PowerUp> powerUps = new ArrayList<>();
    private final ArrayList<Explosion> explosions = new ArrayList<>();

    private Image backgroundImage;
    private int backgroundY;
    private int score;
    private int currentLevel = START_LEVEL;

    private boolean paused;
    private boolean victory;
    private boolean scoreSaved;
    private boolean frozen;
    private long freezeEndTime;

    private boolean showLifeGainedMessage;
    private long lifeMessageEndTime;

    public GamePanel(MainMenu mainMenu) {
        this.mainMenu = mainMenu;
        configurePanel();
        loadBackgroundImage();

        plane = new Plane(362, 480, GameConfig.activePlaneName);
        gridManager = new GridManager(currentLevel, enemies, eggs);

        configureKeyBindings();

        gameTimer = new Timer(FRAME_DELAY_MS, this);
        gameTimer.start();
    }

    private void configurePanel() {
        setPreferredSize(new Dimension(PANEL_WIDTH, PANEL_HEIGHT));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);
    }

    private void loadBackgroundImage() {
        ImageIcon backgroundIcon = new ImageIcon(BACKGROUND_PATH);
        if (backgroundIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            backgroundImage = backgroundIcon.getImage().getScaledInstance(
                    PANEL_WIDTH,
                    PANEL_HEIGHT,
                    Image.SCALE_SMOOTH
            );
        }
    }

    private void configureKeyBindings() {
        bindKey(KeyEvent.VK_ESCAPE, "returnToMenu", this::returnToMainMenu);
        bindKey(KeyEvent.VK_ENTER, "returnAfterGame", () -> {
            if (isGameFinished()) {
                returnToMainMenu();
            }
        });
    }

    private void bindKey(int keyCode, String actionName, Runnable action) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0), actionName);
        getActionMap().put(actionName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                action.run();
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (!paused) {
            updateGame();
        }
        repaint();
    }

    private void updateGame() {
        if (plane.getLives() <= 0) {
            finishGame(false);
            return;
        }

        updateBackground();
        updatePlane();
        updateFreezeState();
        updateEnemiesAndGrid();
        updateProjectiles();
        updatePowerUps();
        updateExplosions();

        handlePlaneEnemyCollisions();
        handleBulletEnemyCollisions();
        handleEggPlaneCollisions();
        handlePlanePowerUpCollisions();

        updateLifeMessageState();
        checkFinalVictory();
    }

    private void updateBackground() {
        backgroundY = (backgroundY + BACKGROUND_SCROLL_SPEED) % PANEL_HEIGHT;
    }

    private void updatePlane() {
        plane.update();
    }

    private void updateFreezeState() {
        if (frozen && System.currentTimeMillis() >= freezeEndTime) {
            frozen = false;
        }
    }

    private void updateEnemiesAndGrid() {
        if (frozen) {
            return;
        }

        int previousLevel = currentLevel;
        gridManager.update();
        currentLevel = gridManager.getCurrentLevel();
        addLevelClearBonus(previousLevel, currentLevel);

        for (Enemy enemy : new ArrayList<>(enemies)) {
            if (enemy instanceof Boss) {
                ((Boss) enemy).updateAttack(eggs);
            }

            enemy.update();
            handleEnemyReachingBottom(enemy);
        }

        enemies.removeIf(enemy -> !enemy.isActive());
    }

    private void addLevelClearBonus(int previousLevel, int newLevel) {
        if (newLevel > previousLevel && previousLevel != MID_BOSS_LEVEL && previousLevel != FINAL_BOSS_LEVEL) {
            score += NORMAL_LEVEL_BONUS;
        }
    }

    private void handleEnemyReachingBottom(Enemy enemy) {
        if (enemy.y <= 500) {
            return;
        }

        damagePlane(Color.ORANGE);
        enemy.y = 100;
    }

    private void updateProjectiles() {
        updateBullets();
        if (!frozen) {
            updateEggs();
        }
    }

    private void updateBullets() {
        for (Bullet bullet : new ArrayList<>(bullets)) {
            bullet.update();
        }
        bullets.removeIf(bullet -> !bullet.isActive());
    }

    private void updateEggs() {
        for (Egg egg : new ArrayList<>(eggs)) {
            egg.update();
        }
        eggs.removeIf(egg -> !egg.isActive());
    }

    private void updatePowerUps() {
        for (PowerUp powerUp : new ArrayList<>(powerUps)) {
            powerUp.update();
        }
        powerUps.removeIf(powerUp -> !powerUp.isActive());
    }

    private void updateExplosions() {
        for (Explosion explosion : new ArrayList<>(explosions)) {
            explosion.update();
        }
        explosions.removeIf(explosion -> !explosion.isActive());
    }

    private void handlePlaneEnemyCollisions() {
        if (plane.isShieldActive()) {
            return;
        }

        Rectangle planeBounds = plane.getBounds();
        for (Enemy enemy : new ArrayList<>(enemies)) {
            if (planeBounds.intersects(enemy.getBounds())) {
                damagePlane(Color.RED);
                enemies.remove(enemy);
            }
        }
    }

    private void handleBulletEnemyCollisions() {
        for (Bullet bullet : new ArrayList<>(bullets)) {
            Enemy hitEnemy = findHitEnemy(bullet);
            if (hitEnemy == null) {
                continue;
            }

            bullets.remove(bullet);
            hitEnemy.takeDamage();

            if (!hitEnemy.isActive() || hitEnemy.getLives() <= 0) {
                handleEnemyDestroyed(hitEnemy);
            }
        }
    }

    private Enemy findHitEnemy(Bullet bullet) {
        for (Enemy enemy : enemies) {
            if (bullet.getBounds().intersects(enemy.getBounds())) {
                return enemy;
            }
        }
        return null;
    }

    private void handleEnemyDestroyed(Enemy enemy) {
        addExplosionAtEnemy(enemy);
        SoundManager.playCollisionSound();

        if (enemy instanceof Boss) {
            handleBossDestroyed(enemy);
            return;
        }

        score += getEnemyScore(enemy);
        createPowerUpIfLucky(enemy);
        gridManager.handleEnemyDeath(enemy);
        enemies.remove(enemy);
    }

    private void handleBossDestroyed(Enemy boss) {
        enemies.remove(boss);

        if (currentLevel == MID_BOSS_LEVEL) {
            score += MID_BOSS_BONUS;
            gridManager.handleEnemyDeath(boss);
            currentLevel = gridManager.getCurrentLevel();
            return;
        }

        if (currentLevel == FINAL_BOSS_LEVEL) {
            score += FINAL_BOSS_BONUS;
            finishGame(true);
        }
    }

    private int getEnemyScore(Enemy enemy) {
        String enemyType = enemy.getClass().getSimpleName();
        if (enemyType.contains("Shooter")) {
            return 25;
        }
        if (enemyType.contains("Zigzag")) {
            return 20;
        }
        if (enemyType.contains("Fast")) {
            return 15;
        }
        return 10;
    }

    private void createPowerUpIfLucky(Enemy enemy) {
        if (Math.random() >= POWER_UP_DROP_CHANCE) {
            return;
        }

        PowerUpType[] types = PowerUpType.values();
        int randomIndex = (int) (Math.random() * types.length);
        powerUps.add(new PowerUp(enemy.getX(), enemy.getY(), types[randomIndex]));
    }

    private void addExplosionAtEnemy(Enemy enemy) {
        Rectangle bounds = enemy.getBounds();
        explosions.add(new Explosion(
                enemy.getX() + bounds.width / 2,
                enemy.getY() + bounds.height / 2,
                Color.RED
        ));
    }

    private void handleEggPlaneCollisions() {
        for (Egg egg : new ArrayList<>(eggs)) {
            if (egg.getBounds().intersects(plane.getBounds())) {
                eggs.remove(egg);
                damagePlane(Color.YELLOW);
            }
        }
    }

    private void damagePlane(Color explosionColor) {
        if (plane.isShieldActive()) {
            return;
        }

        plane.setLives(plane.getLives() - 1);
        SoundManager.playCollisionSound();
        explosions.add(new Explosion(
                plane.getX() + plane.getWidth() / 2,
                plane.getY() + plane.getHeight() / 2,
                explosionColor
        ));
    }

    private void handlePlanePowerUpCollisions() {
        for (PowerUp powerUp : new ArrayList<>(powerUps)) {
            if (!powerUp.getBounds().intersects(plane.getBounds())) {
                continue;
            }

            applyPowerUp(powerUp);
            powerUps.remove(powerUp);
        }
    }

    private void applyPowerUp(PowerUp powerUp) {
        plane.applyPowerUp(powerUp.getType());

        if (powerUp.getType() == PowerUpType.FREEZE_BOMB) {
            frozen = true;
            freezeEndTime = System.currentTimeMillis() + FREEZE_DURATION_MS;
        } else if (powerUp.getType() == PowerUpType.EXTRA_LIFE) {
            showLifeGainedMessage = true;
            lifeMessageEndTime = System.currentTimeMillis() + LIFE_MESSAGE_DURATION_MS;
        }
    }

    private void updateLifeMessageState() {
        if (showLifeGainedMessage && System.currentTimeMillis() >= lifeMessageEndTime) {
            showLifeGainedMessage = false;
        }
    }

    private void checkFinalVictory() {
        if (currentLevel == FINAL_BOSS_LEVEL && enemies.isEmpty() && !victory) {
            finishGame(true);
        }
    }

    private void finishGame(boolean playerWon) {
        if (!gameTimer.isRunning() && scoreSaved) {
            return;
        }

        victory = playerWon;
        gameTimer.stop();
        SoundManager.stopBackgroundMusic();
        SoundManager.playGameOverSound();
        saveGameResult();
        requestFocusInWindow();

        String message = playerWon
                ? "شما برنده شدید!\nامتیاز نهایی: " + score
                : "بازی تمام شد!\nامتیاز نهایی شما: " + score;
        String title = playerWon ? "پیروزی" : "Game Over";
        int messageType = playerWon ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;

        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(this, message, title, messageType));
    }

    private void saveGameResult() {
        if (scoreSaved || !UserSession.isLoggedIn()) {
            return;
        }

        DatabaseManager.saveGameRecord(
                UserSession.getUsername(),
                score,
                currentLevel,
                SOUND_SETTINGS_SNAPSHOT
        );
        scoreSaved = true;
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D g2d = (Graphics2D) graphics.create();
        try {
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawBackground(g2d);
            drawGameObjects(g2d);
            drawHud(g2d);
            drawStateOverlays(g2d);
        } finally {
            g2d.dispose();
        }
    }

    private void drawBackground(Graphics2D g2d) {
        if (backgroundImage == null) {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
            return;
        }

        g2d.drawImage(backgroundImage, 0, backgroundY, null);
        g2d.drawImage(backgroundImage, 0, backgroundY - PANEL_HEIGHT, null);
    }

    private void drawGameObjects(Graphics2D g2d) {
        for (Explosion explosion : explosions) {
            explosion.draw(g2d);
        }
        plane.draw(g2d);
        for (Bullet bullet : bullets) {
            bullet.draw(g2d);
        }
        for (Enemy enemy : enemies) {
            enemy.draw(g2d);
        }
        for (Egg egg : eggs) {
            egg.draw(g2d);
        }
        for (PowerUp powerUp : powerUps) {
            powerUp.draw(g2d);
        }
    }

    private void drawHud(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        String username = UserSession.isLoggedIn() ? UserSession.getUsername() : "Guest";
        g2d.drawString("USER: " + username, 20, 30);
        g2d.drawString("SCORE: " + score, 180, 30);
        g2d.drawString("LIVES: " + plane.getLives(), 320, 30);
        g2d.drawString("GUNS SYNC: [ " + plane.getFireLevel() + " BULLETS ]", 450, 30);
        g2d.drawString("LEVEL: " + currentLevel, 710, 30);

        drawActivePowerUps(g2d, 65);
    }

    private void drawActivePowerUps(Graphics2D g2d, int startY) {
        int hudY = startY;

        if (plane.isShieldActive()) {
            g2d.setColor(Color.CYAN);
            g2d.drawString("SHIELD ACTIVE", 20, hudY);
            hudY += 22;
        }
        if (plane.isRapidFireActive()) {
            g2d.setColor(Color.MAGENTA);
            g2d.drawString("RAPID FIRE ACTIVE", 20, hudY);
            hudY += 22;
        }
        if (frozen) {
            g2d.setColor(new Color(30, 144, 255));
            g2d.drawString("FREEZE BOMB ACTIVE", 20, hudY);
            hudY += 22;
        }
        if (plane.getFireLevel() > 1) {
            g2d.setColor(Color.ORANGE);
            g2d.drawString("WEAPON BOOST ACTIVE (Level " + plane.getFireLevel() + ")", 20, hudY);
            hudY += 22;
        }
        if (showLifeGainedMessage) {
            g2d.setColor(new Color(50, 205, 50));
            g2d.drawString("EXTRA LIFE GAINED! (+1 LIFE)", 20, hudY);
        }
    }

    private void drawStateOverlays(Graphics2D g2d) {
        if (frozen) {
            g2d.setColor(new Color(0, 191, 255, 35));
            g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        }
        if (paused) {
            drawCenteredOverlay(g2d, new Color(0, 0, 0, 150), Color.YELLOW, "PAUSED", 300);
        }
        if (plane.getLives() <= 0) {
            drawGameOverOverlay(g2d);
        }
        if (victory) {
            drawVictoryOverlay(g2d);
        }
    }

    private void drawCenteredOverlay(Graphics2D g2d, Color background, Color foreground, String text, int y) {
        g2d.setColor(background);
        g2d.fillRect(0, 0, PANEL_WIDTH, PANEL_HEIGHT);
        g2d.setColor(foreground);
        g2d.setFont(new Font("Arial", Font.BOLD, 36));
        int x = (PANEL_WIDTH - g2d.getFontMetrics().stringWidth(text)) / 2;
        g2d.drawString(text, x, y);
    }

    private void drawGameOverOverlay(Graphics2D g2d) {
        drawCenteredOverlay(g2d, new Color(150, 0, 0, 180), Color.WHITE, "GAME OVER", 280);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.drawString("Press SPACE to Replay / Restart Game", 245, 330);
        g2d.setFont(new Font("Arial", Font.PLAIN, 16));
        g2d.drawString("Press ESC or ENTER to return to Main Menu", 235, 370);
    }

    private void drawVictoryOverlay(Graphics2D g2d) {
        drawCenteredOverlay(g2d, new Color(0, 150, 0, 180), Color.WHITE, "VICTORY!", 300);
        g2d.setFont(new Font("Arial", Font.PLAIN, 18));
        g2d.drawString("Press ENTER to return to Main Menu", 255, 350);
    }

    @Override
    public void keyPressed(KeyEvent event) {
        int keyCode = event.getKeyCode();

        if (keyCode == KeyEvent.VK_P && !isGameFinished()) {
            paused = !paused;
            return;
        }

        if (keyCode == KeyEvent.VK_SPACE) {
            handleSpaceKey();
            return;
        }

        if (isGameFinished() || paused) {
            return;
        }

        handleMovementKeyPressed(keyCode);

        if (keyCode == KeyEvent.VK_M) {
            openSoundMenu();
        }
    }

    private void handleSpaceKey() {
        if (plane.getLives() <= 0) {
            restartGame();
            return;
        }
        if (victory || paused || !plane.canShoot()) {
            return;
        }

        SoundManager.playShotSound();
        createPlayerBullets();
        plane.shootMock();
    }

    private void createPlayerBullets() {
        int bulletCount = Math.max(1, plane.getFireLevel());
        int centerX = plane.getX() + plane.getWidth() / 2;
        int bulletY = plane.getY();
        double centerIndex = (bulletCount - 1) / 2.0;

        for (int index = 0; index < bulletCount; index++) {
            int offset = (int) Math.round((index - centerIndex) * BULLET_SPACING);
            bullets.add(new Bullet(centerX + offset, bulletY));
        }
    }

    private void handleMovementKeyPressed(int keyCode) {
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A) {
            plane.setDx(-plane.getSpeed());
        } else if (keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            plane.setDx(plane.getSpeed());
        } else if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W) {
            plane.setDy(-plane.getSpeed());
        } else if (keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            plane.setDy(plane.getSpeed());
        }
    }

    private void openSoundMenu() {
        paused = true;
        Window owner = SwingUtilities.getWindowAncestor(this);
        JDialog soundMenu = new JDialog(owner, "تنظیمات صدا", Dialog.ModalityType.APPLICATION_MODAL);
        soundMenu.setSize(300, 200);
        soundMenu.setLayout(new GridLayout(3, 1, 10, 10));
        soundMenu.setLocationRelativeTo(this);

        JLabel title = new JLabel("تنظیمات صوتی حین بازی", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 14));

        JButton toggleSoundButton = new JButton(getSoundButtonText());
        toggleSoundButton.addActionListener(event -> {
            SoundManager.toggleAllSounds();
            toggleSoundButton.setText(getSoundButtonText());
        });

        JButton closeButton = new JButton("بازگشت به بازی");
        closeButton.addActionListener(event -> soundMenu.dispose());

        soundMenu.add(title);
        soundMenu.add(toggleSoundButton);
        soundMenu.add(closeButton);
        soundMenu.setVisible(true);
    }

    private String getSoundButtonText() {
        return SoundManager.isMusicEnabled() ? "🔈 قطع صدا" : "🔊 وصل صدا";
    }

    private void restartGame() {
        resetGameState();
        resetCollections();
        resetPlayer();

        gridManager.resetToLevel(START_LEVEL);
        currentLevel = gridManager.getCurrentLevel();

        SoundManager.playBackgroundMusic();
        gameTimer.start();
        requestFocusInWindow();
        repaint();
    }

    private void resetGameState() {
        score = 0;
        currentLevel = START_LEVEL;
        scoreSaved = false;
        victory = false;
        paused = false;
        frozen = false;
        showLifeGainedMessage = false;
        backgroundY = 0;
    }

    private void resetCollections() {
        bullets.clear();
        enemies.clear();
        eggs.clear();
        powerUps.clear();
        explosions.clear();
    }

    private void resetPlayer() {
        plane.loadStats(GameConfig.activePlaneName);
        plane.setFireLevel(1);
        plane.setLocation(375, 500);
    }

    private boolean isGameFinished() {
        return victory || plane.getLives() <= 0;
    }

    private void returnToMainMenu() {
        if (!isGameFinished()) {
            saveGameResult();
        }

        gameTimer.stop();
        SoundManager.stopBackgroundMusic();

        Window gameWindow = SwingUtilities.getWindowAncestor(this);
        if (gameWindow != null) {
            gameWindow.dispose();
        }

        SoundManager.playBackgroundMusic();
        if (mainMenu != null) {
            mainMenu.setVisible(true);
        }
    }

    @Override
    public void keyReleased(KeyEvent event) {
        int keyCode = event.getKeyCode();
        if (keyCode == KeyEvent.VK_LEFT || keyCode == KeyEvent.VK_A
                || keyCode == KeyEvent.VK_RIGHT || keyCode == KeyEvent.VK_D) {
            plane.setDx(0);
        }
        if (keyCode == KeyEvent.VK_UP || keyCode == KeyEvent.VK_W
                || keyCode == KeyEvent.VK_DOWN || keyCode == KeyEvent.VK_S) {
            plane.setDy(0);
        }
    }

    @Override
    public void keyTyped(KeyEvent event) {
        // This game only uses keyPressed and keyReleased.
    }
}
package model;

import javax.swing.*;
import java.awt.*;

public class Plane {

    private static final int PANEL_WIDTH = 800;
    private static final int PANEL_HEIGHT = 600;

    private static final int PLANE_WIDTH = 85;
    private static final int PLANE_HEIGHT = 85;

    private static final int DEFAULT_FIRE_LEVEL = 1;
    private static final int MAX_FIRE_LEVEL = 10;

    private static final int MAX_LIVES = 5;

    private static final int RAPID_FIRE_DURATION_SECONDS = 8;
    private static final int SHIELD_DURATION_SECONDS = 10;

    private static final int MILLISECONDS_PER_SECOND = 1_000;

    private static final int SHIELD_PADDING = 15;
    private static final int DEFAULT_PLANE_HEAD_SIZE = 10;

    private int x;
    private int y;

    private int dx;
    private int dy;

    private final int width = PLANE_WIDTH;
    private final int height = PLANE_HEIGHT;

    private int speed;
    private int lives;
    private int fireLevel = DEFAULT_FIRE_LEVEL;

    private long baseShotCooldown;
    private long lastShotTime;

    private long rapidFireEndTime;
    private long shieldEndTime;

    private Image planeImage;

    public Plane(int x, int y, String planeName) {
        this.x = x;
        this.y = y;

        loadPlaneImage(planeName);
        loadStats(planeName);
    }

    private void loadPlaneImage(String planeName) {
        planeImage = new ImageIcon("icon/" + planeName + ".png").getImage();
    }

    public void loadStats(String planeName) {
        switch (planeName) {
            case "Fast":
                configureStats(7, 3, 250);
                break;

            case "Heavy":
                configureStats(4, 5, 200);
                break;

            case "Sniper":
                configureStats(5, 3, 150);
                break;

            default:
                configureStats(5, 3, 300);
                break;
        }
    }

    private void configureStats(int speed, int lives, long shotCooldown) {
        this.speed = speed;
        this.lives = lives;
        this.baseShotCooldown = shotCooldown;
    }

    public void update() {
        x += dx;
        y += dy;

        keepInsidePanel();
    }

    private void keepInsidePanel() {
        x = Math.max(0, Math.min(x, PANEL_WIDTH - width));
        y = Math.max(0, Math.min(y, PANEL_HEIGHT - height));
    }

    public void draw(Graphics2D g2d) {
        drawShield(g2d);
        drawPlane(g2d);
    }

    private void drawShield(Graphics2D g2d) {
        if (!isShieldActive()) {
            return;
        }

        g2d.setColor(new Color(0, 191, 255, 100));
        g2d.fillOval(x - SHIELD_PADDING, y - SHIELD_PADDING, width + SHIELD_PADDING * 2, height + SHIELD_PADDING * 2);

        g2d.setColor(Color.CYAN);
        g2d.drawOval(x - SHIELD_PADDING, y - SHIELD_PADDING, width + SHIELD_PADDING * 2, height + SHIELD_PADDING * 2);
    }

    private void drawPlane(Graphics2D g2d) {
        if (planeImage != null) {
            g2d.drawImage(planeImage, x, y, width, height, null);

            return;
        }

        drawDefaultPlane(g2d);
    }

    private void drawDefaultPlane(Graphics2D g2d) {
        g2d.setColor(Color.GREEN);
        g2d.fillRect(x, y, width, height);

        int headX = x + width / 2 - DEFAULT_PLANE_HEAD_SIZE / 2;
        int headY = y - DEFAULT_PLANE_HEAD_SIZE / 2;

        g2d.setColor(Color.WHITE);
        g2d.fillOval(headX, headY, DEFAULT_PLANE_HEAD_SIZE, DEFAULT_PLANE_HEAD_SIZE);
    }

    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        long currentCooldown = getCurrentShotCooldown();

        if (currentTime - lastShotTime < currentCooldown) {
            return false;
        }

        lastShotTime = currentTime;
        return true;
    }

    private long getCurrentShotCooldown() {
        if (isRapidFireActive()) {
            return baseShotCooldown / 2;
        }

        return baseShotCooldown;
    }

    public void loseLife() {
        if (isShieldActive()) {
            return;
        }

        lives = Math.max(0, lives - 1);
    }

    public void addLife() {
        lives = Math.min(lives + 1, MAX_LIVES);
    }

    public void increaseFireLevel() {
        fireLevel = Math.min(fireLevel + 1, MAX_FIRE_LEVEL);
    }

    public void activateShield(int durationSeconds) {
        shieldEndTime = calculateEffectEndTime(durationSeconds);
    }

    public void activateRapidFire(int durationSeconds) {
        rapidFireEndTime = calculateEffectEndTime(durationSeconds);
    }

    private long calculateEffectEndTime(int durationSeconds) {
        int validDuration = Math.max(0, durationSeconds);
        return System.currentTimeMillis() + validDuration * MILLISECONDS_PER_SECOND;
    }

    public boolean isShieldActive() {
        return System.currentTimeMillis() < shieldEndTime;
    }

    public boolean isRapidFireActive() {
        return System.currentTimeMillis() < rapidFireEndTime;
    }

    public void applyPowerUp(PowerUpType type) {
        if (type == null) {
            return;
        }

        switch (type) {
            case ADD_FIRE:
                increaseFireLevel();
                break;

            case RAPID_FIRE:
                activateRapidFire(RAPID_FIRE_DURATION_SECONDS);
                break;

            case EXTRA_LIFE:
                addLife();
                break;

            case SHIELD:
                activateShield(SHIELD_DURATION_SECONDS);
                break;

            case FREEZE_BOMB:
                break;
        }
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;

        stopMoving();
        keepInsidePanel();
    }

    public void stopMoving() {
        dx = 0;
        dy = 0;
    }

    public void resetFireLevel() {
        fireLevel = DEFAULT_FIRE_LEVEL;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void shootMock() {
        int centerX = x + width / 2;
        int centerY = y;

        System.out.println("تیر شلیک شد! مختصات نوک سفینه: (" + centerX + ", " + centerY + ") - سطح تیر: " + fireLevel);
    }

    public void setDx(int dx) {
        this.dx = dx;
    }
    public void setDy(int dy) {
        this.dy = dy;
    }
    public void setLives(int lives) {
        this.lives = Math.max(0, Math.min(lives, MAX_LIVES));
    }
    public void setFireLevel(int fireLevel) {
        this.fireLevel = Math.max(DEFAULT_FIRE_LEVEL, Math.min(fireLevel, MAX_FIRE_LEVEL));
    }

    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public int getWidth() {
        return width;
    }
    public int getHeight() {
        return height;
    }
    public int getSpeed() {
        return speed;
    }
    public int getLives() {
        return lives;
    }
    public int getFireLevel() {
        return fireLevel;
    }
}
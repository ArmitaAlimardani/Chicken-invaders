package model;

import view.GamePanel;

import javax.swing.*;
import java.awt.*;


public class Plane {
    private int x, y;
    private int width = 85;
    private int height = 85;

    private int dx, dy;

    private int fireLevel = 1;

    private long lastShotTime = 0;
    private long rapidFireEndTime = 0;

    private long shieldEndTime = 0;

    private Image planeImage;

    private int speed;
    private int lives;
    private long baseShotCooldown;

    public void loadStats(String planeName) {
        switch (planeName) {
            case "Fast":
                this.speed = 7;
                this.lives = 3;
                this.baseShotCooldown = 250;
                break;
            case "Heavy":
                this.speed = 4;
                this.lives = 5;
                this.baseShotCooldown = 200;
                break;
            case "Sniper":
                this.speed = 5;
                this.lives = 3;
                this.baseShotCooldown = 150;
                break;
            default:
                this.speed = 5;
                this.lives = 3;
                this.baseShotCooldown = 300;
                break;
        }
    }

    public Plane(int x, int y, String planeName) {
        this.x = x;
        this.y = y;

        this.planeImage = new ImageIcon("icon/" + planeName + ".png").getImage();
        loadStats(planeName);

    }


    public void setLives(int lives) { this.lives = lives; }

    public void update() {
        x += dx;
        y += dy;

        if (x < 0) x = 0;
        if (x > 800 - width) x = 800 - width;
        if (y < 0) y = 0;
        if (y > 600 - height) y = 600 - height;
    }

    public void draw(Graphics2D g2d) {
        if (isShieldActive()) {
            g2d.setColor(new Color(0, 191, 255, 100));
            g2d.fillOval(x - 15, y - 15, width + 30, height + 30);
            g2d.setColor(Color.CYAN);
            g2d.drawOval(x - 15, y - 15, width + 30, height + 30);
        }

        if (planeImage != null) {
            g2d.drawImage(planeImage, x, y, width, height, null);
        } else {
            g2d.setColor(Color.GREEN);
            g2d.fillRect(x, y, width, height);
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x + width / 2 - 5, y - 5, 10, 10);
        }
    }

    public boolean canShoot() {
        long currentTime = System.currentTimeMillis();
        long currentCooldown = isRapidFireActive() ? (baseShotCooldown / 2) : baseShotCooldown;

        if (currentTime - lastShotTime >= currentCooldown) {
            lastShotTime = currentTime;
            return true;
        }
        return false;
    }

    public void shootMock() {
        int centerX = this.x + (this.width / 2);
        int centerY = this.y;
        System.out.println("تیر شلیک شد! مختصات نوک سفینه: (" + centerX + ", " + centerY + ") - سطح تیر: " + fireLevel);
    }

    public void loseLife() {
        if (!isShieldActive()) {
            lives--;
        }
    }

    public void activateShield(int durationSeconds) {
        shieldEndTime = System.currentTimeMillis() + (durationSeconds * 1000L);
    }

    public void activatePremiumRapidFire(int durationSeconds) {
        rapidFireEndTime = System.currentTimeMillis() + (durationSeconds * 1000L);
    }

    public void addLife() {
        if (lives < 5) lives++; // سقف ۵ جان طبق صورت پروژه
    }

    public void incrementFireLevel() {
        fireLevel++;
    }

    public boolean isShieldActive() {
        return System.currentTimeMillis() < shieldEndTime;
    }

    public boolean isRapidFireActive() {
        return System.currentTimeMillis() < rapidFireEndTime;
    }

    public void setDx(int dx) { this.dx = dx; }
    public void setDy(int dy) { this.dy = dy; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public int getSpeed() { return speed; }
    public int getLives() { return lives; }
    public int getFireLevel() { return fireLevel; }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public void applyPowerUp(PowerUpType type) {
        switch (type) {
            case ADD_FIRE:
                incrementFireLevel();
                break;
            case RAPID_FIRE:
                activatePremiumRapidFire(8);
                break;
            case EXTRA_LIFE:
                addLife();
                break;
            case SHIELD:
                activateShield(10);
                break;
            case FREEZE_BOMB:
                break;
        }
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
        this.dx = 0;
        this.dy = 0;
    }

    public void setFireLevel(int fireLevel) {
        this.fireLevel = fireLevel;
    }
}
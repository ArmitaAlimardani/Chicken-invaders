package model.enemy;

import model.Egg;

import javax.swing.ImageIcon;
import java.awt.*;
import java.util.ArrayList;

public class BossLevel4 extends Boss {

    private static final int PANEL_WIDTH = 800;
    private static final int BASE_Y = 50;
    private static final int VERTICAL_MOVEMENT_RANGE = 25;
    private static final double ANGLE_STEP = 0.05;

    private static final int EGG_SPEED = 4;
    private static final long SHOT_INTERVAL = 1500;
    private static final int[] SHOT_ANGLES = {0, 90, 180, 270};

    private double movementAngle;
    private long lastShotTime;

    public BossLevel4(int x, int y) {
        super(x, y, 2, 2, 50, 140, 140);
        bossImage = loadBossImage();
    }

    private Image loadBossImage() {
        ImageIcon icon = new ImageIcon("icon/boss1.png");

        if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            return null;
        }

        return icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    @Override
    public void update() {
        updateHorizontalPosition();
        updateVerticalPosition();
    }

    private void updateHorizontalPosition() {
        int newX = getX() + speedX;

        if (newX < 0 || newX > PANEL_WIDTH - width) {
            speedX *= -1;
            newX = getX() + speedX;
        }

        setPosition(newX, getY());
    }

    private void updateVerticalPosition() {
        movementAngle += ANGLE_STEP;
        int newY = BASE_Y + (int) (Math.sin(movementAngle) * VERTICAL_MOVEMENT_RANGE);

        setPosition(getX(), newY);
    }

    @Override
    public void updateAttack(ArrayList<Egg> eggs) {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastShotTime < SHOT_INTERVAL) {
            return;
        }

        shootInFourDirections(eggs);
        lastShotTime = currentTime;
    }

    private void shootInFourDirections(ArrayList<Egg> eggs) {
        int centerX = getX() + width / 2;
        int centerY = getY() + height;

        for (int angle : SHOT_ANGLES) {
            eggs.add(new Egg(centerX, centerY, EGG_SPEED, angle));
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        int x = getX();
        int y = getY();

        drawBoss(g2d, x, y);
        drawHealthBar(g2d);
    }

    private void drawBoss(Graphics2D g2d, int x, int y) {
        if (bossImage != null) {
            g2d.drawImage(bossImage, x, y, null);
            return;
        }

        drawFallbackBoss(g2d, x, y);
    }

    private void drawFallbackBoss(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(x, y, width, height);

        g2d.setColor(Color.RED);
        g2d.drawRect(x, y, width, height);
        g2d.drawString("Image Not Found!", x + 20, y + height / 2);
    }
}
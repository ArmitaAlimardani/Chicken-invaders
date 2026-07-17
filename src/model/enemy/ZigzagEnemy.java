package model.enemy;

import javax.swing.ImageIcon;
import java.awt.*;

public class ZigzagEnemy extends Enemy {

    private static final int PANEL_WIDTH = 800;
    private static final int INITIAL_DIRECTION = 1;
    private static final int TARGET_ZIGZAG_AMPLITUDE = 3;
    private static final int NORMAL_ZIGZAG_AMPLITUDE = 2;
    private static final double TARGET_ANGLE_STEP = 0.2;
    private static final double NORMAL_ANGLE_STEP = 0.1;
    private static final Font LIVES_FONT = new Font("Arial", Font.BOLD, 12);

    private int direction = INITIAL_DIRECTION;
    private double angle;
    private final Image enemyImage;

    public ZigzagEnemy(int x, int y, int currentLevel) {
        super(x, y, 2, 2, currentLevel <= 3 ? 2 : 3);
        enemyImage = loadEnemyImage();
    }

    private Image loadEnemyImage() {
        ImageIcon icon = new ImageIcon("icon/zigzag_chicken.png");

        if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            return null;
        }

        return icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    @Override
    public void update() {
        if (isMovingToTarget()) {
            updateTargetMovement();
            return;
        }

        updateZigzagMovement();
    }

    private void updateTargetMovement() {
        angle += TARGET_ANGLE_STEP;

        int newX = getX() + (int) (Math.sin(angle) * TARGET_ZIGZAG_AMPLITUDE);
        setPosition(newX, getY());

        moveTowardsTarget();
    }

    private void updateZigzagMovement() {
        angle += NORMAL_ANGLE_STEP;

        int newX = getX() + speedX * direction;
        int newY = getY() + (int) (Math.sin(angle) * NORMAL_ZIGZAG_AMPLITUDE);

        if (newX < 0 || newX > PANEL_WIDTH - width) {
            direction *= -1;
            newX = getX() + speedX * direction;
        }

        setPosition(newX, newY);
    }

    @Override
    public void draw(Graphics2D g2d) {
        int x = getX();
        int y = getY();

        drawEnemy(g2d, x, y);
        drawLives(g2d, x, y);
    }

    private void drawEnemy(Graphics2D g2d, int x, int y) {
        if (enemyImage != null) {
            g2d.drawImage(enemyImage, x, y, null);
            return;
        }

        g2d.setColor(Color.MAGENTA);
        g2d.fillOval(x, y, width, height);
    }

    private void drawLives(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(LIVES_FONT);
        g2d.drawString(String.valueOf(getLives()), x + width / 2 - 4, y - 5);
    }
}
package model.enemy;

import javax.swing.ImageIcon;
import java.awt.*;

public class NormalEnemy extends Enemy {

    private static final Font LIVES_FONT = new Font("Arial", Font.BOLD, 12);

    private final Image enemyImage;

    public NormalEnemy(int x, int y, int currentLevel) {
        super(x, y, 2, 2, currentLevel <= 3 ? 2 : 3);
        enemyImage = loadEnemyImage();
    }

    private Image loadEnemyImage() {
        ImageIcon icon = new ImageIcon("icon/2xnoramal_chicken.png");

        if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            return null;
        }

        return icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
    }

    @Override
    public void update() {
        if (isMovingToTarget()) {
            moveTowardsTarget();
        }
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

        g2d.setColor(Color.YELLOW);
        g2d.fillOval(x, y, width, height);
    }

    private void drawLives(Graphics2D g2d, int x, int y) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(LIVES_FONT);
        g2d.drawString(String.valueOf(getLives()), x + width / 2 - 4, y - 5);
    }
}
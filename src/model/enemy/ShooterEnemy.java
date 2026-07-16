package model.enemy;

import javax.swing.*;
import java.awt.*;

public class ShooterEnemy extends Enemy {
    private Image enemyImage;

    public ShooterEnemy(int x, int y, int currentLevel) {
        super(x, y, 2, 2, (currentLevel >= 7) ? 4 : 3);

        ImageIcon icon = new ImageIcon("icon\\shooter_chicken.png");
        Image rawImage = icon.getImage();
        if (rawImage != null && icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            this.enemyImage = rawImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
    }

    @Override
    public void update() {
        if (isMovingToTarget) {
            moveTowardsTarget();
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (enemyImage != null) {
            g2d.drawImage(enemyImage, x, y, null);
        } else {
            g2d.setColor(Color.RED);
            g2d.fillOval(x, y, width, height);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(String.valueOf(lives), x + width/2 - 4, y - 5);
    }
}
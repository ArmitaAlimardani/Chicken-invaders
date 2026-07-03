package model.enemy;

import javax.swing.*;
import java.awt.*;

public class FastEnemy extends Enemy {
    private int direction = 1;
    private Image enemyImage;

    public FastEnemy(int x, int y, int currentLevel) {
        super(x, y, 4, 3, (currentLevel <= 3) ? 1 : 2);

        ImageIcon icon = new ImageIcon("icon\\fast_chicken.png");
        Image rawImage = icon.getImage();
        if (rawImage != null && icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            this.enemyImage = rawImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
    }

    @Override
    public void update() {
        if (isMovingToTarget) {
            moveTowardsTarget();
            return;
        }
        x += speedX * direction;
        if (x < 0 || x > 800 - width) {
            direction *= -1;
            y += 10;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (enemyImage != null) {
            g2d.drawImage(enemyImage, x, y, null);
        } else {
            g2d.setColor(Color.ORANGE);
            g2d.fillOval(x, y, width, height);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(String.valueOf(lives), x + width/2 - 4, y - 5);
    }
}
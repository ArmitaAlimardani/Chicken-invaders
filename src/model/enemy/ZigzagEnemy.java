package model.enemy;

import javax.swing.*;
import java.awt.*;

public class ZigzagEnemy extends Enemy {
    private int direction = 1;
    private double angle = 0;
    private Image enemyImage;

    public ZigzagEnemy(int x, int y, int currentLevel) {
        super(x, y, 2, 2, (currentLevel <= 3) ? 2 : 3);

        ImageIcon icon = new ImageIcon("icon\\zigzag_chicken.png");
        Image rawImage = icon.getImage();
        if (rawImage != null && icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            this.enemyImage = rawImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
    }

    @Override
    public void update() {
        if (isMovingToTarget) {
            angle += 0.2;
            x += (int) (Math.sin(angle) * 3);
            moveTowardsTarget();
            return;
        }

        angle += 0.1;
        x += speedX * direction;
        y += (int) (Math.sin(angle) * 2);

        if (x < 0 || x > 800 - width) {
            direction *= -1;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (enemyImage != null) {
            g2d.drawImage(enemyImage, x, y, null);
        } else {
            g2d.setColor(Color.MAGENTA);
            g2d.fillOval(x, y, width, height);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        g2d.drawString(String.valueOf(lives), x + width/2 - 4, y - 5);
    }
}
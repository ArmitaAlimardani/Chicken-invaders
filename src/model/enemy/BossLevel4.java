package model.enemy;

import model.Egg;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BossLevel4 extends Boss {
    private double angleMovement = 0;
    private long lastShotTime = 0;
    private final long shotInterval = 1500;

    public BossLevel4(int x, int y) {
        // x, y, speedX, speedY, lives, width, height
        super(x, y, 2, 2, 50, 140, 140);

        ImageIcon icon = new ImageIcon("icon/boss1.png");
        Image rawImage = icon.getImage();

        if (rawImage != null) {
            this.bossImage = rawImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
    }

    @Override
    public void update() {
        x += speedX;
        if (x < 0 || x > 800 - width) {
            speedX *= -1;
        }

        angleMovement += 0.05;
        y = 50 + (int) (Math.sin(angleMovement) * 25);
    }

    @Override
    public void updateAttack(ArrayList<Egg> eggs) {
        long now = System.currentTimeMillis();
        if (now - lastShotTime > shotInterval) {
            int centerX = x + width / 2;
            int centerY = y + height;

            int[] angles = {0, 90, 180, 270};
            for (int angle : angles) {
                eggs.add(new Egg(centerX, centerY, 4, angle));
            }
            lastShotTime = now;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (bossImage != null && bossImage.getWidth(null) > 0) {
            g2d.drawImage(bossImage, x, y, null);
        } else {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(x, y, width, height);
            g2d.setColor(Color.RED);
            g2d.drawRect(x, y, width, height);
            g2d.drawString("Image Not Found!", x + 20, y + height / 2);
        }
        drawHealthBar(g2d);
    }
}
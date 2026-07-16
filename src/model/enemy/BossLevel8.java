package model.enemy;

import model.Egg;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BossLevel8 extends Boss {
    private double angleMovement = 0;
    private long lastShotTime = 0;
    private final long shotInterval = 1000;

    public BossLevel8(int x, int y) {
        super(x, y, 3, 2, 100, 180, 180);

        ImageIcon icon = new ImageIcon("icon/boss2.png");
        Image rawImage = icon.getImage();
        if (rawImage != null) {
            this.bossImage = rawImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
    }

    @Override
    public void update() {
        x += speedX;
        if (x < 10 || x > 800 - width - 10) {
            speedX *= -1;
        }

        angleMovement += 0.04;
        y = 60 + (int) (Math.sin(angleMovement) * 45);
    }

    @Override
    public void updateAttack(ArrayList<Egg> eggs) {
        long now = System.currentTimeMillis();
        if (now - lastShotTime > shotInterval) {
            int centerX = x + width / 2;
            int centerY = y + height;

            int[] angles = {0, 45, 90, 135, 180, 225, 270, 315};
            for (int angle : angles) {
                eggs.add(new Egg(centerX, centerY, 5, angle));
            }
            lastShotTime = now;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (bossImage != null && bossImage.getWidth(null) > 0) {
            g2d.drawImage(bossImage, x, y, null);
        } else {
            g2d.setColor(Color.MAGENTA);
            g2d.fillRect(x, y, width, height);
            g2d.setColor(Color.RED);
            g2d.drawRect(x, y, width, height);
            g2d.drawString("Image Not Found (boss2.png)!", x + 10, y + height / 2);
        }
        drawHealthBar(g2d);
    }
}
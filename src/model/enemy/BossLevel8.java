package model.enemy;

import model.Egg;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BossLevel8 extends Boss {
    private double angleMovement = 0;
    private long lastShotTime = 0;
    private final long shotInterval = 1000; // ۱ ثانیه

    public BossLevel8(int x, int y) {
        // ۱۰۰ جان، ابعاد بزرگتر ۱۸۰ در ۱۸۰
        super(x, y, 3, 2, 100, 180, 180);

        ImageIcon icon = new ImageIcon("icon\\boss_final.png");
        Image rawImage = icon.getImage();
        if (rawImage != null && icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            this.bossImage = rawImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
    }

    @Override
    public void update() {
        // حرکت افقی با سرعت مبنا
        x += speedX;
        if (x < 10 || x > 800 - width - 10) {
            speedX *= -1;
        }

        // حرکت نوسانی در محدوده ۱۰۰ پیکسل عمودی (بند ۴.۴)
        angleMovement += 0.04;
        y = 60 + (int) (Math.sin(angleMovement) * 45);
    }

    @Override
    public void updateAttack(ArrayList<Egg> eggs) {
        long now = System.currentTimeMillis();
        if (now - lastShotTime > shotInterval) {
            int centerX = x + width / 2 - 9;
            int centerY = y + height - 20;

            // شلیک ۸ جهته با سرعت ۵ پیکسل/فریم طبق داکیومنت
            int[] angles = {0, 45, 90, 135, 180, 225, 270, 315};
            for (int angle : angles) {
                eggs.add(new Egg(centerX, centerY, 5, angle));
            }
            lastShotTime = now;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (bossImage != null) {
            g2d.drawImage(bossImage, x, y, null);
        } else {
            g2d.setColor(Color.MAGENTA);
            g2d.fillRect(x, y, width, height);
        }
        drawHealthBar(g2d);
    }
}
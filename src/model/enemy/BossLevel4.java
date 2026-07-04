package model.enemy;

import model.Egg;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class BossLevel4 extends Boss {
    private double angleMovement = 0;
    private long lastShotTime = 0;
    private final long shotInterval = 1500; // ۱.۵ ثانیه

    public BossLevel4(int x, int y) {
        // x, y, speedX, speedY, lives, width, height
        super(x, y, 2, 2, 50, 140, 140);

        ImageIcon icon = new ImageIcon("icon\\boss_mid.png");
        Image rawImage = icon.getImage();
        if (rawImage != null && icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            this.bossImage = rawImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
    }

    @Override
    public void update() {
        // حرکت افقی چپ و راست
        x += speedX;
        if (x < 0 || x > 800 - width) {
            speedX *= -1;
        }

        // حرکت عمودی نوسانی آرام
        angleMovement += 0.05;
        y = 50 + (int) (Math.sin(angleMovement) * 25);
    }

    @Override
    public void updateAttack(ArrayList<Egg> eggs) {
        long now = System.currentTimeMillis();
        if (now - lastShotTime > shotInterval) {
            int centerX = x + width / 2 - 9;
            int centerY = y + height - 20;

            // شلیک ۴ جهته (۰، ۹۰، ۱۸۰، ۲۷۰ درجه) با سرعت ۴ پیکسل/فریم
            int[] angles = {0, 90, 180, 270};
            for (int angle : angles) {
                eggs.add(new Egg(centerX, centerY, 4, angle));
            }
            lastShotTime = now;
        }
    }

    @Override
    public void draw(Graphics2D g2d) {
        if (bossImage != null) {
            g2d.drawImage(bossImage, x, y, null);
        } else {
            g2d.setColor(Color.DARK_GRAY);
            g2d.fillRect(x, y, width, height);
        }
        drawHealthBar(g2d); // فراخوانی نوار سلامت از کلاس پدر
    }
}
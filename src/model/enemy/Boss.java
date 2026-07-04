package model.enemy;

import java.awt.*;

public abstract class Boss extends Enemy {
    protected int maxLives;
    protected Image bossImage;

    public Boss(int x, int y, int speedX, int speedY, int lives, int width, int height) {
        super(x, y, speedX, speedY, lives);
        this.width = width;
        this.height = height;
        this.maxLives = lives;
    }

    // متد انتزاعی برای حملات خاص هر غول که فرزندان باید پیاده کنند
    public abstract void updateAttack(java.util.ArrayList<model.Egg> eggs);

    // رسم مشترک نوار سلامت برای همه غول‌ها طبق بند ۴.۴
    protected void drawHealthBar(Graphics2D g2d) {
        int barWidth = width;
        int barHeight = 8;
        int barX = x;
        int barY = y - 15;

        // پس‌زمینه قرمز
        g2d.setColor(Color.RED);
        g2d.fillRect(barX, barY, barWidth, barHeight);

        // مقدار باقی‌مانده سبز
        g2d.setColor(Color.GREEN);
        int currentBarWidth = (int) (((double) lives / maxLives) * barWidth);
        g2d.fillRect(barX, barY, currentBarWidth, barHeight);

        // قاب دور نوار سلامت
        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }
}
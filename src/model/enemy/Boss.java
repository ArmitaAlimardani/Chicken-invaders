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

    public abstract void updateAttack(java.util.ArrayList<model.Egg> eggs);

    protected void drawHealthBar(Graphics2D g2d) {
        int barWidth = width;
        int barHeight = 8;
        int barX = x;
        int barY = y - 15;

        g2d.setColor(Color.RED);
        g2d.fillRect(barX, barY, barWidth, barHeight);

        g2d.setColor(Color.GREEN);
        int currentBarWidth = (int) (((double) lives / maxLives) * barWidth);
        g2d.fillRect(barX, barY, currentBarWidth, barHeight);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, barWidth, barHeight);
    }
}
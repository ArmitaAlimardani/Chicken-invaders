package model.enemy;

import model.Egg;

import java.awt.*;
import java.util.ArrayList;

public abstract class Boss extends Enemy {

    private static final int HEALTH_BAR_HEIGHT = 8;
    private static final int HEALTH_BAR_VERTICAL_GAP = 15;

    private final int maxLives;
    protected Image bossImage;

    public Boss(int x, int y, int speedX, int speedY, int lives, int width, int height) {
        super(x, y, speedX, speedY, lives);
        this.width = width;
        this.height = height;
        this.maxLives = lives;
    }

    public abstract void updateAttack(ArrayList<Egg> eggs);

    protected void drawHealthBar(Graphics2D g2d) {
        int barX = getX();
        int barY = getY() - HEALTH_BAR_VERTICAL_GAP;
        int currentBarWidth = calculateCurrentBarWidth();

        g2d.setColor(Color.RED);
        g2d.fillRect(barX, barY, width, HEALTH_BAR_HEIGHT);

        g2d.setColor(Color.GREEN);
        g2d.fillRect(barX, barY, currentBarWidth, HEALTH_BAR_HEIGHT);

        g2d.setColor(Color.WHITE);
        g2d.drawRect(barX, barY, width, HEALTH_BAR_HEIGHT);
    }

    private int calculateCurrentBarWidth() {
        if (maxLives <= 0) {
            return 0;
        }

        double healthRatio = (double) getLives() / maxLives;
        healthRatio = Math.max(0, Math.min(1, healthRatio));

        return (int) (healthRatio * width);
    }
}
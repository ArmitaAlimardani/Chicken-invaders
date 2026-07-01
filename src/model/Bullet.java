package model;

import java.awt.*;

public class Bullet {
    private int x, y;
    private int width = 6;
    private int height = 15;
    private int speed = 8;
    private boolean active = true;

    public Bullet(int x, int y) {
        this.x = x - (this.width / 2);
        this.y = y;
    }

    public void update() {
        y -= speed;

        if (y + height < 0) {
            active = false;
        }
    }

    public void draw(Graphics2D g2d) {
        // رسم یک گلوله لیزری و جذاب سرخ‌رنگ با افکت نئونی کوچک
        g2d.setColor(new Color(255, 69, 0, 150));
        g2d.fillRect(x - 1, y - 1, width + 2, height + 2);

        g2d.setColor(Color.RED); // هسته اصلی و پررنگ گلوله
        g2d.fillRect(x, y, width, height);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isActive() { return active; }
    public int getX() { return x; }
    public int getY() { return y; }
}
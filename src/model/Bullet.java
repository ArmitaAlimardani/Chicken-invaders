package model;

import javax.swing.*;
import java.awt.*;

public class Bullet {
    private int x, y;
    private int width = 45;
    private int height = 45;
    private int speed = 8;
    private boolean active = true;
    private Image bulletImage;

    public Bullet(int x, int y) {
        this.x = x - (this.width / 2);
        this.y = y;
        this.bulletImage = new ImageIcon("icon\\shot.png").getImage();
    }

    public void update() {
        y -= speed;
        if (y + height < 0) {
            active = false;
        }
    }

    public void draw(Graphics2D g2d) {
            g2d.drawImage(bulletImage, x, y, width, height, null);
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isActive() { return active; }
    public int getX() { return x; }
    public int getY() { return y; }
}
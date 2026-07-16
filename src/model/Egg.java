package model;

import java.awt.*;
import javax.swing.*;

public class Egg {
    private int x, y;
    private int width = 25;
    private int height = 30;
    private double speed;
    private double angle;
    private boolean active = true;
    private Image eggImage;

    public Egg(int x, int y, double speed, double angleDegree) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.angle = Math.toRadians(angleDegree);

        ImageIcon icon = new ImageIcon("icon\\egg.png");
        Image rawImage = icon.getImage();
        if (rawImage != null && icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            this.eggImage = rawImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
    }

    public void update() {
        x += (int) (speed * Math.cos(angle));
        y += (int) (speed * Math.sin(angle));

        if (y > 600 || y < -50 || x < -50 || x > 850) {
            active = false;
        }
    }

    public void draw(Graphics2D g2d) {
        if (eggImage != null) {
            g2d.drawImage(eggImage, x, y, null);
        } else {
            g2d.setColor(Color.WHITE);
            g2d.fillOval(x, y, width, height);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
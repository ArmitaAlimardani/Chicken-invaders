package model;

import javax.swing.*;
import java.awt.*;

public class PowerUp {
    private int x, y;
    private int width = 35;
    private int height = 35;
    private double speed = 2.0;
    private PowerUpType type;
    private boolean active = true;
    private Image powerUpImage;

    public PowerUp(int x, int y, PowerUpType type) {
        this.x = x;
        this.y = y;
        this.type = type;
        loadIcon();
    }

    private void loadIcon() {
        String fileName = "";
        switch (type) {
            case ADD_FIRE:     fileName = "add_shot.png"; break;
            case RAPID_FIRE:   fileName = "fast_shot.png"; break;
            case EXTRA_LIFE:   fileName = "heal.png"; break;
            case SHIELD:       fileName = "sheild.png"; break;
            case FREEZE_BOMB:  fileName = "freeze.png"; break;
        }

        ImageIcon icon = new ImageIcon("icon\\" + fileName);
        Image rawImage = icon.getImage();
        if (rawImage != null && icon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            this.powerUpImage = rawImage.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        }
    }

    public void update() {
        y += speed;
        if (y > 600) {
            active = false;
        }
    }

    public void draw(Graphics2D g2d) {
        if (powerUpImage != null) {
            g2d.drawImage(powerUpImage, x, y, null);
        } else {
            switch (type) {
                case ADD_FIRE: g2d.setColor(Color.ORANGE); break;
                case RAPID_FIRE: g2d.setColor(Color.RED); break;
                case EXTRA_LIFE: g2d.setColor(Color.PINK); break;
                case SHIELD: g2d.setColor(Color.CYAN); break;
                case FREEZE_BOMB: g2d.setColor(Color.BLUE); break;
            }
            g2d.fillOval(x, y, 25, 25);
            g2d.setColor(Color.BLACK);
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            g2d.drawString(type.name().substring(0, 1), x + 8, y + 17);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isActive() { return active; }
    public PowerUpType getType() { return type; }
}
package model;

import javax.swing.*;
import java.awt.*;

public class Explosion {
    private int x, y;
    private int size;
    private int maxSize = 60;
    private int alpha;
    private int holdTime = 0;
    private Image explosionImage;
    private boolean active;

    public Explosion(int x, int y) {
        this.x = x;
        this.y = y;
        this.size = 50;
        this.alpha = 255;
        this.active = true;
        this.explosionImage = new ImageIcon("icon\\Explosion.png").getImage();
    }

    public Explosion(int x, int y, Color color) {
        this(x, y);
    }

    public void update() {
        if (!active) return;

        if (holdTime < 10) {
            holdTime++;
        } else {
            // مرحله دوم: شروع به محو شدن
            if (alpha > 20) {
                alpha -= 15; // سرعت محو شدن را کمتر کردیم تا طولانی‌تر بماند
            } else {
                active = false;
            }
        }
    }

    public void draw(Graphics2D g2d) {
        if (!active) return;

        // اعمال شفافیت برای محو شدن تصویر
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));

        if (explosionImage != null) {
            g2d.drawImage(explosionImage, x - (size / 2), y - (size / 2), size, size, null);
        }

        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public boolean isActive() {
        return active;
    }
}
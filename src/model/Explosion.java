package model;

import java.awt.*;

public class Explosion {
    private int x, y;
    private int radius;
    private int maxRadius;
    private int speed;
    private int alpha; // میزان شفافیت (255 یعنی کاملا کدر، 0 یعنی محو شده)
    private Color color;
    private boolean active;

    public Explosion(int x, int y, Color color) {
        this.x = x;
        this.y = y;
        this.radius = 10;
        this.maxRadius = 50; // شعاع نهایی انفجار
        this.speed = 3;      // سرعت بزرگ شدن
        this.alpha = 255;    // شروع از بالاترین شفافیت
        this.color = color;
        this.active = true;
    }

    public void update() {
        if (!active) return;

        radius += speed;
        // به مرور زمان شفافیت کم می‌شود
        alpha -= 15;

        if (radius >= maxRadius || alpha <= 0) {
            alpha = 0;
            active = false; // انفجار به پایان رسید
        }
    }

    public void draw(Graphics2D g2d) {
        if (!active) return;

        // تنظیم میزان شفافیت برای نقاشی دایره
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));

        // رسم دایره اصلی انفجار
        g2d.setColor(color);
        g2d.fillOval(x - radius, y - radius, radius * 2, radius * 2);

        // رسم یک حلقه بیرونی برای جلوه بیشتر
        g2d.setColor(Color.ORANGE);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x - radius, y - radius, radius * 2, radius * 2);

        // ریست کردن وضعیت شفافیت به حالت عادی برای بقیه المان‌های بازی
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    public boolean isActive() {
        return active;
    }
}
package model.enemy;

import java.awt.*;

public abstract class Enemy {
    public int x, y;
    protected int width = 65;
    protected int height = 65;
    protected int speedX, speedY;
    protected int lives;
    protected boolean active = true;

    protected int targetX, targetY;
    public boolean isMovingToTarget = false;

    public Enemy(int x, int y, int speedX, int speedY, int lives) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.lives = lives;
    }

    public void setTargetPosition(int tx, int ty) {
        this.targetX = tx;
        this.targetY = ty;
        this.isMovingToTarget = true;
    }

    protected void moveTowardsTarget() {
        if (x < targetX) x += Math.min(speedX, targetX - x);
        else if (x > targetX) x -= Math.min(speedX, x - targetX);

        if (y < targetY) y += Math.min(speedY, targetY - y);
        else if (y > targetY) y -= Math.min(speedY, y - targetY);

        if (x == targetX && y == targetY) {
            isMovingToTarget = false;
        }
    }

    public abstract void update();
    public abstract void draw(Graphics2D g2d);

    public void takeDamage() {
        lives--;
        if (lives <= 0) {
            active = false;
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }
    public boolean isActive() { return active; }
    public int getX() { return x; }
    public int getY() { return y; }
    public int getLives(){ return lives; }
}
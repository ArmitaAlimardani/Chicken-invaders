package model.enemy;

import java.awt.Graphics2D;
import java.awt.Rectangle;

public abstract class Enemy {

    protected static final int DEFAULT_WIDTH = 65;
    protected static final int DEFAULT_HEIGHT = 65;

    private int x;
    private int y;
    private int targetX;
    private int targetY;
    private boolean movingToTarget;

    protected int width = DEFAULT_WIDTH;
    protected int height = DEFAULT_HEIGHT;
    protected int speedX;
    protected int speedY;
    protected int lives;
    protected boolean active = true;

    public Enemy(int x, int y, int speedX, int speedY, int lives) {
        this.x = x;
        this.y = y;
        this.targetX = x;
        this.targetY = y;
        this.speedX = speedX;
        this.speedY = speedY;
        this.lives = lives;
    }

    public void setTargetPosition(int targetX, int targetY) {
        this.targetX = targetX;
        this.targetY = targetY;
        movingToTarget = x != targetX || y != targetY;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    protected void moveTowardsTarget() {
        int horizontalSpeed = Math.abs(speedX);
        int verticalSpeed = Math.abs(speedY);

        if (x < targetX) {
            x += Math.min(horizontalSpeed, targetX - x);
        } else if (x > targetX) {
            x -= Math.min(horizontalSpeed, x - targetX);
        }

        if (y < targetY) {
            y += Math.min(verticalSpeed, targetY - y);
        } else if (y > targetY) {
            y -= Math.min(verticalSpeed, y - targetY);
        }

        if (x == targetX && y == targetY) {
            movingToTarget = false;
        }
    }

    public abstract void update();

    public abstract void draw(Graphics2D g2d);

    public void takeDamage() {
        if (!active) {
            return;
        }

        lives--;

        if (lives <= 0) {
            lives = 0;
            active = false;
        }
    }

    public Rectangle getBounds() { return new Rectangle(x, y, width, height); }

    public boolean isActive() { return active; }

    public boolean isMovingToTarget() { return movingToTarget; }

    public int getX() { return x; }

    public int getY() { return y; }

    public int getLives() { return lives; }
}
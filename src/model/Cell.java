package model;

import model.enemy.Enemy;

public class Cell {
    private int row, col;
    private int targetX, targetY; // موقعیت ثابت خانه در شبکه
    private int cellLives;        // شمارنده کل خانه (مثلاً ۲ یا ۳ یا ۴ بر اساس مرحله)
    private Enemy currentEnemy;   // مرغی که الان در این خانه حضور دارد یا دارد به سمتش پرواز می‌کند

    public Cell(int row, int col, int startX, int startY, int cellLives) {
        this.row = row;
        this.col = col;
        this.targetX = startX;
        this.targetY = startY;
        this.cellLives = cellLives;
    }

    // آپدیت موقعیت خانه زمانی که کل شبکه با هم جابجا می‌شود
    public void updatePosition(int offsetX, int offsetY) {
        this.targetX += offsetX;
        this.targetY += offsetY;

        // اگر مرغ جابجا شده و در حال پرواز نیست، حرکتش با شبکه هماهنگ شود
        if (currentEnemy != null && !currentEnemy.isMovingToTarget) {
            // ایجاد یک متد ساده در Enemy برای ست کردن مستقیم مکان هنگام حرکت شبکه
            currentEnemy.x = this.targetX;
            currentEnemy.y = this.targetY;
        }
    }

    // گترها و سترها
    public int getTargetX() { return targetX; }
    public int getTargetY() { return targetY; }
    public int getCellLives() { return cellLives; }
    public void decrementCellLives() { this.cellLives--; }
    public Enemy getCurrentEnemy() { return currentEnemy; }
    public void setCurrentEnemy(Enemy currentEnemy) { this.currentEnemy = currentEnemy; }
}
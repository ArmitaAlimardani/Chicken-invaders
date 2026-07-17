package model;

import model.enemy.Enemy;

public class Cell {

    private final int row;
    private final int col;

    private int targetX;
    private int targetY;
    private int cellLives;
    private Enemy currentEnemy;

    public Cell(int row, int col, int startX, int startY, int cellLives) {
        this.row = row;
        this.col = col;
        this.targetX = startX;
        this.targetY = startY;
        this.cellLives = cellLives;
    }

    public void updatePosition(int offsetX, int offsetY) {
        targetX += offsetX;
        targetY += offsetY;

        if (currentEnemy != null && !currentEnemy.isMovingToTarget()) {
            currentEnemy.setPosition(targetX, targetY);
        }
    }

    public int getTargetX() { return targetX; }

    public int getTargetY() { return targetY; }

    public int getCellLives() { return cellLives; }

    public void decrementCellLives() { cellLives--; }

    public Enemy getCurrentEnemy() { return currentEnemy; }

    public void setCurrentEnemy(Enemy currentEnemy) { this.currentEnemy = currentEnemy; }
}
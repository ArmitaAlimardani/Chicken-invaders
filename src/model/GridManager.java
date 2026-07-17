package model;

import model.enemy.Boss;
import model.enemy.BossLevel4;
import model.enemy.BossLevel8;
import model.enemy.Enemy;
import model.enemy.FastEnemy;
import model.enemy.NormalEnemy;
import model.enemy.ShooterEnemy;
import model.enemy.ZigzagEnemy;

import java.util.ArrayList;

public class GridManager {

    private static final int ROW_COUNT = 5;
    private static final int COLUMN_COUNT = 8;

    private static final int MID_BOSS_LEVEL = 4;
    private static final int FINAL_BOSS_LEVEL = 8;

    private static final int GRID_START_X = 80;
    private static final int GRID_START_Y = 50;
    private static final int HORIZONTAL_GAP = 70;
    private static final int VERTICAL_GAP = 65;

    private static final int PANEL_WIDTH = 800;
    private static final int ENEMY_WIDTH = 65;
    private static final int GRID_BOTTOM_LIMIT = 430;

    private final Cell[][] grid = new Cell[ROW_COUNT][COLUMN_COUNT];
    private final ArrayList<Enemy> activeEnemies;
    private final ArrayList<Egg> eggs;

    private int currentLevel;
    private double horizontalSpeed;
    private int verticalStep;
    private long eggDropInterval;
    private int initialCellLives;
    private int direction = 1;
    private long lastEggDropTime;
    private Boss currentBoss;

    public GridManager(int level, ArrayList<Enemy> activeEnemies, ArrayList<Egg> eggs) {
        currentLevel = level;
        this.activeEnemies = activeEnemies;
        this.eggs = eggs;

        configureLevelParameters();
        setupLevel();
    }

    private void configureLevelParameters() {
        switch (currentLevel) {
            case 1:
                configureLevel(1.0, 20, 3000, 2);
                break;
            case 2:
                configureLevel(1.5, 20, 2000, 2);
                break;
            case 3:
                configureLevel(2.0, 25, 1500, 3);
                break;
            case 4:
                configureLevel(1.5, 0, 1500, 1);
                break;
            case 5:
                configureLevel(2.5, 25, 1000, 3);
                break;
            case 6:
                configureLevel(3.0, 30, 800, 4);
                break;
            case 7:
                configureLevel(3.5, 30, 700, 4);
                break;
            case 8:
                configureLevel(3.0, 0, 1000, 1);
                break;
            default:
                configureLevel(1.0, 20, 3000, 2);
                break;
        }
    }

    private void configureLevel(double horizontalSpeed, int verticalStep, long eggDropInterval, int initialCellLives) {
        this.horizontalSpeed = horizontalSpeed;
        this.verticalStep = verticalStep;
        this.eggDropInterval = eggDropInterval;
        this.initialCellLives = initialCellLives;
    }

    private void setupLevel() {
        activeEnemies.clear();
        currentBoss = null;
        lastEggDropTime = System.currentTimeMillis();

        if (isBossLevel()) {
            setupBossLevel();
        } else {
            setupNormalLevel();
        }
    }

    private void setupBossLevel() {
        if (currentLevel == MID_BOSS_LEVEL) {
            currentBoss = new BossLevel4(330, 50);
        } else {
            currentBoss = new BossLevel8(310, 50);
        }

        activeEnemies.add(currentBoss);
    }

    private void setupNormalLevel() {
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                createGridCell(row, column);
            }
        }
    }

    private void createGridCell(int row, int column) {
        int cellX = GRID_START_X + column * HORIZONTAL_GAP;
        int cellY = GRID_START_Y + row * VERTICAL_GAP;

        Cell cell = new Cell(row, column, cellX, cellY, initialCellLives);
        Enemy enemy = createEnemyForLevel(cellX, cellY);

        cell.setCurrentEnemy(enemy);
        grid[row][column] = cell;
        activeEnemies.add(enemy);
    }

    private Enemy createEnemyForLevel(int x, int y) {
        double randomValue = Math.random();

        switch (currentLevel) {
            case 1:
                return new NormalEnemy(x, y, currentLevel);
            case 2:
                if (randomValue > 0.6) {
                    return new FastEnemy(x, y, currentLevel);
                }

                return new NormalEnemy(x, y, currentLevel);
            case 3:
                if (randomValue > 0.5) {
                    return new ZigzagEnemy(x, y, currentLevel);
                }

                return new NormalEnemy(x, y, currentLevel);
            case 5:
                if (randomValue > 0.5) {
                    return new ShooterEnemy(x, y, currentLevel);
                }

                return new FastEnemy(x, y, currentLevel);
            case 6:
                if (randomValue > 0.5) {
                    return new ZigzagEnemy(x, y, currentLevel);
                }

                return new ShooterEnemy(x, y, currentLevel);
            case 7:
                return createLevelSevenEnemy(x, y, randomValue);
            default:
                return new NormalEnemy(x, y, currentLevel);
        }
    }

    private Enemy createLevelSevenEnemy(int x, int y, double randomValue) {
        if (randomValue < 0.25) {
            return new NormalEnemy(x, y, currentLevel);
        }

        if (randomValue < 0.5) {
            return new FastEnemy(x, y, currentLevel);
        }

        if (randomValue < 0.75) {
            return new ZigzagEnemy(x, y, currentLevel);
        }

        return new ShooterEnemy(x, y, currentLevel);
    }

    public void update() {
        removeInactiveEnemies();

        if (activeEnemies.isEmpty()) {
            advanceLevel();
            return;
        }

        if (isBossLevel()) {
            updateBoss();
            return;
        }

        updateGridMovement();
        resetGridIfItReachedBottom();
        updateEggDropping();
    }

    private void removeInactiveEnemies() {
        activeEnemies.removeIf(enemy -> !enemy.isActive());
    }

    private void updateBoss() {
        if (currentBoss == null || !currentBoss.isActive()) {
            return;
        }

        currentBoss.update();
        currentBoss.updateAttack(eggs);
    }

    private void updateGridMovement() {
        int horizontalShift = (int) (horizontalSpeed * direction);

        if (willGridHitHorizontalEdge(horizontalShift)) {
            direction *= -1;
            moveGrid(0, verticalStep);
            return;
        }

        moveGrid(horizontalShift, 0);
    }

    private boolean willGridHitHorizontalEdge(int horizontalShift) {
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                Cell cell = grid[row][column];

                if (!isCellActive(cell)) {
                    continue;
                }

                int nextX = cell.getTargetX() + horizontalShift;

                if (nextX < 0 || nextX > PANEL_WIDTH - ENEMY_WIDTH) {
                    return true;
                }
            }
        }

        return false;
    }

    private void moveGrid(int deltaX, int deltaY) {
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                Cell cell = grid[row][column];

                if (cell != null) {
                    cell.updatePosition(deltaX, deltaY);
                }
            }
        }
    }

    private void resetGridIfItReachedBottom() {
        if (!hasGridReachedBottom()) {
            return;
        }

        resetGridPosition();
        System.out.println("Warning: Chickens reached the bottom. Grid reset to top.");
    }

    private boolean hasGridReachedBottom() {
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                Cell cell = grid[row][column];

                if (isCellActive(cell) && cell.getTargetY() > GRID_BOTTOM_LIMIT) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isCellActive(Cell cell) {
        return cell != null && cell.getCellLives() > 0;
    }

    private void resetGridPosition() {
        direction = 1;

        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                Cell cell = grid[row][column];

                if (cell == null) {
                    continue;
                }

                resetCellPosition(cell, row, column);
            }
        }
    }

    private void resetCellPosition(Cell cell, int row, int column) {
        int newX = GRID_START_X + column * HORIZONTAL_GAP;
        int newY = GRID_START_Y + row * VERTICAL_GAP;
        int deltaX = newX - cell.getTargetX();
        int deltaY = newY - cell.getTargetY();

        cell.updatePosition(deltaX, deltaY);
        resetCellEnemyPosition(cell, newX, newY);
    }

    private void resetCellEnemyPosition(Cell cell, int newX, int newY) {
        Enemy enemy = cell.getCurrentEnemy();

        if (enemy == null) {
            return;
        }

        enemy.setPosition(newX, newY);
    }

    private void updateEggDropping() {
        long currentTime = System.currentTimeMillis();

        if (currentTime - lastEggDropTime < eggDropInterval) {
            return;
        }

        shootEggFromRandomChicken();
        lastEggDropTime = currentTime;
    }

    private void shootEggFromRandomChicken() {
        ArrayList<Enemy> readyEnemies = getEnemiesReadyToShoot();

        if (readyEnemies.isEmpty()) {
            return;
        }

        Enemy shooter = selectRandomEnemy(readyEnemies);
        shootNormalEgg(shooter);

        if (shooter instanceof ShooterEnemy) {
            shootSpecialEgg(shooter);
        }
    }

    private ArrayList<Enemy> getEnemiesReadyToShoot() {
        ArrayList<Enemy> readyEnemies = new ArrayList<>();

        for (Enemy enemy : activeEnemies) {
            if (enemy.isActive() && !enemy.isMovingToTarget()) {
                readyEnemies.add(enemy);
            }
        }

        return readyEnemies;
    }

    private Enemy selectRandomEnemy(ArrayList<Enemy> enemies) {
        int randomIndex = (int) (Math.random() * enemies.size());
        return enemies.get(randomIndex);
    }

    private void shootNormalEgg(Enemy shooter) {
        eggs.add(new Egg(shooter.getX() + 20, shooter.getY() + 45, 4, 90));
    }

    private void shootSpecialEgg(Enemy shooter) {
        eggs.add(new Egg(shooter.getX(), shooter.getY() + 20, 5, 0));
    }

    public void handleEnemyDeath(Enemy deadEnemy) {
        if (deadEnemy == null) {
            return;
        }

        if (isBossLevel()) {
            handleBossDeath(deadEnemy);
            return;
        }

        Cell deadEnemyCell = findCellContaining(deadEnemy);

        if (deadEnemyCell == null) {
            return;
        }

        activeEnemies.remove(deadEnemy);
        handleCellEnemyDeath(deadEnemyCell);
    }

    private void handleBossDeath(Enemy deadEnemy) {
        if (deadEnemy != currentBoss) {
            return;
        }

        activeEnemies.remove(deadEnemy);
        currentBoss = null;
    }

    private Cell findCellContaining(Enemy enemy) {
        for (int row = 0; row < ROW_COUNT; row++) {
            for (int column = 0; column < COLUMN_COUNT; column++) {
                Cell cell = grid[row][column];

                if (cell != null && cell.getCurrentEnemy() == enemy) {
                    return cell;
                }
            }
        }

        return null;
    }

    private void handleCellEnemyDeath(Cell cell) {
        cell.decrementCellLives();

        if (cell.getCellLives() <= 0) {
            cell.setCurrentEnemy(null);
            return;
        }

        Enemy replacement = createReplacementEnemy(cell);
        cell.setCurrentEnemy(replacement);
        activeEnemies.add(replacement);
    }

    private Enemy createReplacementEnemy(Cell cell) {
        int spawnX = chooseRandomSpawnX();
        Enemy replacement = createEnemyForLevel(spawnX, 0);

        replacement.setTargetPosition(cell.getTargetX(), cell.getTargetY());

        return replacement;
    }

    private int chooseRandomSpawnX() {
        if (Math.random() > 0.5) {
            return 0;
        }

        return 750;
    }

    private void advanceLevel() {
        if (currentLevel >= FINAL_BOSS_LEVEL) {
            System.out.println("Victory! Game Completed.");
            return;
        }

        currentLevel++;
        configureLevelParameters();
        setupLevel();

        System.out.println("Advanced to Level " + currentLevel);
    }

    public void resetToLevel(int level) {
        currentLevel = level;
        direction = 1;

        configureLevelParameters();
        setupLevel();

        System.out.println("GridManager successfully reset to Level " + level);
    }

    private boolean isBossLevel() {
        return currentLevel == MID_BOSS_LEVEL || currentLevel == FINAL_BOSS_LEVEL;
    }

    public int getCurrentLevel() { return currentLevel; }
}
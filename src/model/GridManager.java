package model;

import model.enemy.Enemy;
import model.enemy.NormalEnemy;
import model.enemy.FastEnemy;
import model.enemy.ZigzagEnemy;
import model.enemy.ShooterEnemy;
import model.enemy.Boss;
import model.enemy.BossLevel4;
import model.enemy.BossLevel8;

import java.awt.*;
import java.util.ArrayList;

public class GridManager {
    private Cell[][] grid = new Cell[5][8];
    private ArrayList<Enemy> activeEnemies;
    private ArrayList<Egg> eggs;
    private int currentLevel;

    // پارامترهای مرحله طبق جدول صورت پروژه
    private double speedX;
    private int stepY;
    private long eggDropInterval; // به میلی‌ثانیه
    private int initialCellLives;

    private int direction = 1; // ۱ راست، ۱- چپ
    private long lastEggDropTime = 0;

    // فیلد چندریختی برای نگهداری غول‌های مرحله ۴ و ۸
    private Boss currentBoss = null;

    public GridManager(int level, ArrayList<Enemy> activeEnemies, ArrayList<Egg> eggs) {
        this.currentLevel = level;
        this.activeEnemies = activeEnemies;
        this.eggs = eggs;
        initLevelParameters();
        setupGrid();
    }

    // تنظیم دقیق پارامترها از روی جدول پروژه (مراحل ۱ تا ۸)
    private void initLevelParameters() {
        switch (currentLevel) {
            case 1:
                speedX = 1.0; stepY = 20; eggDropInterval = 3000; initialCellLives = 2;
                break;
            case 2:
                speedX = 1.5; stepY = 20; eggDropInterval = 2000; initialCellLives = 2;
                break;
            case 3:
                speedX = 2.0; stepY = 25; eggDropInterval = 1500; initialCellLives = 3;
                break;
            case 4: // غول میانی (پارامتر شلیک افکت داخلی دارد اما بیس ذخیره می‌شود)
                speedX = 1.5; stepY = 0;  eggDropInterval = 1500; initialCellLives = 1;
                break;
            case 5:
                speedX = 2.5; stepY = 25; eggDropInterval = 1000; initialCellLives = 3;
                break;
            case 6:
                speedX = 3.0; stepY = 30; eggDropInterval = 800;  initialCellLives = 4;
                break;
            case 7:
                speedX = 3.5; stepY = 30; eggDropInterval = 700;  initialCellLives = 4;
                break;
            case 8: // غول نهایی
                speedX = 3.0; stepY = 0;  eggDropInterval = 1000; initialCellLives = 1;
                break;
            default:
                speedX = 1.0; stepY = 20; eggDropInterval = 3000; initialCellLives = 2;
        }
    }

    // چیدن مرغ‌ها یا غول‌ها متناسب با مرحله بازی
    private void setupGrid() {
        // ⬇️ خیلی مهم: این خط باعث میشه لیست اصلی توی GamePanel هم کاملاً پاک بشه
        activeEnemies.clear();
        currentBoss = null;

        // مدیریت مراحل غول (بند ۴.۴)
        if (currentLevel == 4) {
            currentBoss = new BossLevel4(330, 50);
            activeEnemies.add(currentBoss);
            return;
        }
        if (currentLevel == 8) {
            currentBoss = new BossLevel8(310, 50);
            activeEnemies.add(currentBoss);
            return;
        }

        // چیدن ماتریس ۵ در ۸ برای مراحل معمولی (۱، ۲، ۳، ۵، ۶، ۷)
        int startX = 80;
        int startY = 50;
        int hGap = 70;
        int vGap = 65;

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 8; c++) {
                int cellX = startX + (c * hGap);
                int cellY = startY + (r * vGap);

                grid[r][c] = new Cell(r, c, cellX, cellY, initialCellLives);

                Enemy enemy = createEnemyForLevel(cellX, cellY);
                grid[r][c].setCurrentEnemy(enemy);

                // ⬇️ مرغ‌های جدید لول بعدی مستقیماً به لیست بازی فرستاده می‌شوند
                activeEnemies.add(enemy);
            }
        }
    }

    // تولید اختصاصی تنوع مرغ‌ها بر اساس جدول "نوع دشمنان غالب" صورت پروژه
    private Enemy createEnemyForLevel(int x, int y) {
        double rand = Math.random();

        switch (currentLevel) {
            case 1:
                return new NormalEnemy(x, y, currentLevel);
            case 2: // ترکیب نرمال و سریع
                if (rand > 0.6) return new FastEnemy(x, y, currentLevel);
                return new NormalEnemy(x, y, currentLevel);
            case 3: // ترکیب نرمال و زیگزاگ
                if (rand > 0.5) return new ZigzagEnemy(x, y, currentLevel);
                return new NormalEnemy(x, y, currentLevel);
            case 5: // ترکیب شوتینگ و سریع
                if (rand > 0.5) return new ShooterEnemy(x, y, currentLevel);
                return new FastEnemy(x, y, currentLevel);
            case 6: // ترکیب زیگزاگ و شوتینگ
                if (rand > 0.5) return new ZigzagEnemy(x, y, currentLevel);
                return new ShooterEnemy(x, y, currentLevel);
            case 7: // همه انواع به صورت کاملاً رندوم
                if (rand < 0.25) return new NormalEnemy(x, y, currentLevel);
                else if (rand < 0.5) return new FastEnemy(x, y, currentLevel);
                else if (rand < 0.75) return new ZigzagEnemy(x, y, currentLevel);
                else return new ShooterEnemy(x, y, currentLevel);
            default:
                return new NormalEnemy(x, y, currentLevel);
        }
    }

    // به‌روزرسانی هوشمند فریم‌ها بر اساس نوع لول (شبکه‌ای یا غول)
    public void update() {
        // اگر تمام دشمنان نابود شده باشند، برو مرحله بعد
        if (activeEnemies.isEmpty()) {
            advanceLevel();
            return;
        }

        // مدیریت لول‌های غول
        if (currentLevel == 4 || currentLevel == 8) {
            if (currentBoss != null && currentBoss.isActive()) {
                currentBoss.update();
                currentBoss.updateAttack(eggs);
            }
            return;
        }

        // حرکت تیمی شبکه مرغ‌های معمولی
        boolean hitEdge = false;
        int currentShift = (int)(speedX * direction);

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 8; c++) {
                Cell cell = grid[r][c];
                if (cell != null && cell.getCellLives() > 0) {
                    int nextX = cell.getTargetX() + currentShift;
                    if (nextX < 0 || nextX > 800 - 65) {
                        hitEdge = true;
                    }
                }
            }
        }

        if (hitEdge) {
            direction *= -1;
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 8; c++) {
                    if (grid[r][c] != null) grid[r][c].updatePosition(0, stepY);
                }
            }
        } else {
            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 8; c++) {
                    if (grid[r][c] != null) grid[r][c].updatePosition(currentShift, 0);
                }
            }
        }

        // 🛑 [بخش جدید: راه حل دوم] بررسی رسیدن کل گله به پایین صفحه
        boolean networkReachedBottom = false;
        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 8; c++) {
                Cell cell = grid[r][c];
                // اگر سلول زنده است و موقعیت تارگت Y آن از حد مجاز (مثلاً 430 پیکسل) عبور کرده
                if (cell != null && cell.getCellLives() > 0 && cell.getTargetY() > 430) {
                    networkReachedBottom = true;
                    break;
                }
            }
        }

        // اگر گله مرغ‌ها به کف صفحه نزدیک شد، کل ماتریس شبکه را به بالا ریست کن
        if (networkReachedBottom) {
            int startX = 80;
            int startY = 50; // بازگشت به Y سقف
            int hGap = 70;
            int vGap = 65;
            direction = 1; // ریست کردن جهت حرکت افقی به سمت راست

            for (int r = 0; r < 5; r++) {
                for (int c = 0; c < 8; c++) {
                    Cell cell = grid[r][c];
                    if (cell != null) {
                        int targetNewX = startX + (c * hGap);
                        int targetNewY = startY + (r * vGap);

                        // محاسبه میزان جابه‌جایی لازم برای ریست فیزیکی سلول به مختصات اولیه بالا
                        int diffX = targetNewX - cell.getTargetX();
                        int diffY = targetNewY - cell.getTargetY();

                        // انتقال هماهنگ سلول شبکه
                        cell.updatePosition(diffX, diffY);

                        // اگر مرغ زنده درون این سلول است، مختصات فیزیکی و تارگت مرغ را هم فورا بالا ببر
                        Enemy enemy = cell.getCurrentEnemy();
                        if (enemy != null) {
                            enemy.x = targetNewX;
                            enemy.y = targetNewY;
                            if (enemy.isMovingToTarget) {
                                enemy.setTargetPosition(targetNewX, targetNewY);
                            }
                        }
                    }
                }
            }
            System.out.println("⚠️ Warning: Chickens invaded the bottom! Grid reset to top.");
            // جریمه کم شدن جان هواپیما به دلیل کپسوله‌سازی کامل در متد updateGame کلاس GamePanel مدیریت می‌شود.
        }

        // شلیک تخم‌مرغ توسط مرغ‌ها در زمان مشخص شده
        long now = System.currentTimeMillis();
        if (now - lastEggDropTime > eggDropInterval) {
            shootEggFromRandomChicken();
            lastEggDropTime = now;
        }
    }

    private void shootEggFromRandomChicken() {
        if (activeEnemies.isEmpty()) return;

        // فیلتر کردن مرغ‌هایی که در حال پرواز به خانه شبکه نیستند برای شلیک منطقی
        ArrayList<Enemy> readyToShoot = new ArrayList<>();
        for (Enemy e : activeEnemies) {
            if (!e.isMovingToTarget) {
                readyToShoot.add(e);
            }
        }

        if (readyToShoot.isEmpty()) return;

        int index = (int)(Math.random() * readyToShoot.size());
        Enemy shooter = readyToShoot.get(index);

        // شلیک تخم‌مرغ عمودی (سرعت ۴، زاویه ۹۰) طبق بند ۴.۵
        eggs.add(new Egg(shooter.getX() + 20, shooter.getY() + 45, 4, 90));

        // اگر دشمن شلیک‌کننده (ShooterEnemy) بود، علاوه بر تخم عمودی شلیک افقی هم می‌کند
        if (shooter instanceof ShooterEnemy) {
            // شلیک افقی به چپ یا راست بر اساس مکان هواپیما (یا ساده به راست با سرعت ۵)
            eggs.add(new Egg(shooter.getX(), shooter.getY() + 20, 5, 0));
        }
    }

    // مدیریت مرگ مرغ‌ها و شبیه‌سازی مکانیزم پرواز جایگزین
    public void handleEnemyDeath(Enemy deadEnemy) {
        if (currentLevel == 4 || currentLevel == 8) {
            if (deadEnemy == currentBoss) {
                activeEnemies.remove(deadEnemy);
            }
            return;
        }

        for (int r = 0; r < 5; r++) {
            for (int c = 0; c < 8; c++) {
                Cell cell = grid[r][c];
                if (cell != null && cell.getCurrentEnemy() == deadEnemy) {
                    cell.decrementCellLives();

                    if (cell.getCellLives() > 0) {
                        int spawnX = (Math.random() > 0.5) ? 0 : 750;
                        Enemy replacement = createEnemyForLevel(spawnX, 0);

                        replacement.setTargetPosition(cell.getTargetX(), cell.getTargetY());
                        cell.setCurrentEnemy(replacement);
                        activeEnemies.add(replacement);
                    } else {
                        cell.setCurrentEnemy(null);
                    }
                    return;
                }
            }
        }
    }

    // هدایت اتوماتیک بازی به لول بالاتر
    private void advanceLevel() {
        if (currentLevel < 8) {
            currentLevel++;
            initLevelParameters();
            setupGrid();
            System.out.println("Advanced to Level " + currentLevel);
        } else {
            System.out.println("Victory! Game Completed.");
            // این فاز در GamePanel هندل پیروزی نهایی را فعال می‌کند.
        }
    }

    //  متد اختصاصی برای تنظیم مجدد لول و چیدمان شبکه مرغ‌ها در زمان ری‌استارت بازی
    public void resetToLevel(int level) {
        this.currentLevel = level;
        this.direction = 1;            // ریست کردن جهت حرکت شبکه به سمت راست
        this.lastEggDropTime = 0;       // ریست کردن زمان‌سنج شلیک تخم‌مرغ‌ها

        // ۱. مقداردهی مجدد پارامترهای سرعت و جان مرغ‌ها بر اساس لول جدید (لول ۱)
        initLevelParameters();

        // ۲. خالی کردن لیست غول‌ها و بازسازی ماتریس ۵ در ۸ مرغ‌ها از سقف
        setupGrid();

        System.out.println("GridManager successfully reset to Level " + level);
    }

    public int getCurrentLevel() { return currentLevel; }
}
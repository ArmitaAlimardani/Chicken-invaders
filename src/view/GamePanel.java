package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import model.Plane;
import model.enemy.Enemy;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer gameTimer;
    private Plane plane;
    private boolean isPaused = false;
    private int score = 0;
    private int currentLevel = 1;

    private java.util.ArrayList<model.Bullet> bullets = new java.util.ArrayList<>();
    private java.util.ArrayList<Enemy> enemies = new java.util.ArrayList<>();
    private java.util.ArrayList<model.Egg> eggs = new java.util.ArrayList<>();
    private model.GridManager gridManager;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        plane = new Plane(362, 480);

        // مقداردهی اولیه مدیریت شبکه و مراحل
        gridManager = new model.GridManager(currentLevel, enemies, eggs);

        gameTimer = new Timer(16, this);
        gameTimer.start();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isPaused) {
            updateGame();
        }
        repaint();
    }

    private void updateGame() {
        // اگر جان هواپیما صفر شده باشد، بازی تمام است (Game Over)
        if (plane.getLives() <= 0) {
            gameTimer.stop();
            return;
        }

        plane.update();

        // ۱. آپدیت هماهنگ و تیمی شبکهٔ مرغ‌ها و غول‌ها
        gridManager.update();

        // همگام‌سازی لول پنل گرافیکی با موتور بازی
        this.currentLevel = gridManager.getCurrentLevel();

        // ۲. آپدیت گلوله‌های هواپیما
        for (int i = 0; i < bullets.size(); i++) {
            model.Bullet b = bullets.get(i);
            b.update();
            if (!b.isActive()) {
                bullets.remove(i);
                i--;
            }
        }

        // ۳. آپدیت وضعیت تک‌تک مرغ‌ها (مانند حرکت و پرواز جایگزین‌ها)
        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            enemy.update();
            if (!enemy.isActive()) {
                enemies.remove(i);
                i--;
            }
        }

        // ۴. آپدیت تخم‌مرغ‌ها و گلوله‌های دشمن
        for (int i = 0; i < eggs.size(); i++) {
            model.Egg egg = eggs.get(i);
            egg.update();
            if (!egg.isActive()) {
                eggs.remove(i);
                i--;
            }
        }

        // ۵. سیستم برخورد تیرهای هواپیما به مرغ‌ها و غول‌ها
        for (int i = 0; i < bullets.size(); i++) {
            model.Bullet b = bullets.get(i);
            for (int j = 0; j < enemies.size(); j++) {
                Enemy enemy = enemies.get(j);

                if (b.getBounds().intersects(enemy.getBounds())) {
                    bullets.remove(i);
                    i--;
                    enemy.takeDamage();

                    if (!enemy.isActive()) {
                        score += 10;
                        gridManager.handleEnemyDeath(enemy);

                        // حذف فیزیکی دشمن از لیست پنل برای فعال شدن چرخه تغییر لول
                        enemies.remove(j);
                        j--;
                    }
                    break;
                }
            }
        }

        // ۶. سیستم برخورد تخم‌مرغ‌ها/تیرهای دشمن به هواپیما (بند ۴.۵)
        for (int i = 0; i < eggs.size(); i++) {
            model.Egg egg = eggs.get(i);
            if (egg.getBounds().intersects(plane.getBounds())) {
                eggs.remove(i);
                i--;

                // کم شدن یک جان از هواپیما (سیستم سپر بعداً اضافه می‌شود)
                plane.setLives(plane.getLives() - 1);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // رسم هواپیما
        plane.draw(g2d);

        // رسم گلوله‌ها
        for (model.Bullet b : bullets) {
            b.draw(g2d);
        }

        // رسم مرغ‌ها و غول‌ها
        for (Enemy enemy : enemies) {
            enemy.draw(g2d);
        }

        // رسم تخم‌مرغ‌ها
        for (model.Egg egg : eggs) {
            egg.draw(g2d);
        }

        // رسم اطلاعات بازی (HUD)
        drawHUD(g2d);

        // صفحه توقف بازی (PAUSED)
        if (isPaused) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, 800, 600);
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.drawString("PAUSED", 330, 300);
        }

        // صفحه باخت نهایی (Game Over) - بند ۴.۳
        if (plane.getLives() <= 0) {
            g2d.setColor(new Color(150, 0, 0, 180)); // قرمز ملایم
            g2d.fillRect(0, 0, 800, 600);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.drawString("GAME OVER", 260, 300);
        }

        // صفحه پیروزی نهایی بازی (Victory) - بند ۴.۴
        if (currentLevel == 8 && enemies.isEmpty()) {
            gameTimer.stop();
            g2d.setColor(new Color(0, 150, 0, 180)); // سبز ملایم
            g2d.fillRect(0, 0, 800, 600);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.drawString("VICTORY!", 290, 300);
        }
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));
        g2d.drawString("USER: Player1", 20, 30);
        g2d.drawString("SCORE: " + score, 20, 55);
        g2d.drawString("LIVES: " + plane.getLives(), 20, 80);
        g2d.drawString("FIRE POWER: x" + plane.getFireLevel(), 20, 105);
        g2d.drawString("LEVEL: " + currentLevel, 700, 30);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (plane.getLives() <= 0 || (currentLevel == 8 && enemies.isEmpty())) return;

        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) plane.setDx(-plane.getSpeed());
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) plane.setDx(plane.getSpeed());
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) plane.setDy(-plane.getSpeed());
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) plane.setDx(0);
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) plane.setDy(plane.getSpeed());

        if (key == KeyEvent.VK_SPACE) {
            if (plane.canShoot()) {
                int bulletX = plane.getX() + (plane.getWidth() / 2);
                int bulletY = plane.getY();
                bullets.add(new model.Bullet(bulletX, bulletY));
                plane.shootMock();
            }
        }

        if (key == KeyEvent.VK_P) {
            isPaused = !isPaused;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            plane.setDx(0);
        }
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W || key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            plane.setDy(0);
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
}
package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import model.Plane;
import model.PowerUp;
import model.PowerUpType;
import model.enemy.Enemy;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer gameTimer;
    private Plane plane;
    private boolean isPaused = false;
    private int score = 0;
    private int currentLevel = 1;

    private Image backgroundImage;

    private java.util.ArrayList<model.Bullet> bullets = new java.util.ArrayList<>();
    private java.util.ArrayList<Enemy> enemies = new java.util.ArrayList<>();
    private java.util.ArrayList<model.Egg> eggs = new java.util.ArrayList<>();

    // لیست نگهداری پاورآپ‌های فعال در صفحه (بند ۴.۶)
    private java.util.ArrayList<PowerUp> powerUps = new java.util.ArrayList<>();

    private model.GridManager gridManager;

    // متغیرهای مدیریت پاورآپ بمب یخ‌زن
    private boolean isFrozen = false;
    private long freezeEndTime = 0;

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // بارگذاری تصویر پس‌زمینه جدید
        ImageIcon bgIcon = new ImageIcon("icon\\background.jpg");
        if (bgIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            this.backgroundImage = bgIcon.getImage().getScaledInstance(800, 600, Image.SCALE_SMOOTH);
        }

        plane = new Plane(362, 480);
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
        if (plane.getLives() <= 0) {
            gameTimer.stop();
            return;
        }

        plane.update();

        // چک کردن اتمام زمان بمب یخ‌زن
        long now = System.currentTimeMillis();
        if (isFrozen && now > freezeEndTime) {
            isFrozen = false;
        }

        // ۱. آپدیت شبکهٔ مرغ‌ها و غول‌ها (فقط در صورت عدم یخ‌زدگی)
        if (!isFrozen) {
            gridManager.update();
        }

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

        // ۳. آپدیت وضعیت تک‌تک مرغ‌ها
        if (!isFrozen) {
            for (int i = 0; i < enemies.size(); i++) {
                Enemy enemy = enemies.get(i);
                enemy.update();

                // اگر مرغ زنده انفرادی یا سیستمی به پایین صفحه (مثلاً 500) نفوذ کرد
                if (enemy.y > 500) {
                    // جریمه: اگر سپر فعال نباشد، یک جان کم می‌شود
                    if (!plane.isShieldActive()) {
                        plane.setLives(plane.getLives() - 1);
                    }
                    // مختصات این مرغ خاطی را موقتاً بالا می‌بریم تا متد ریست تیمی GridManager کل گله را یکپارچه کند
                    enemy.y = 100;
                }

                if (!enemy.isActive()) {
                    enemies.remove(i);
                    i--;
                }
            }
        }

        // ۴. آپدیت تخم‌مرغ‌ها و گلوله‌های دشمن (متوقف در زمان یخ‌زدگی)
        if (!isFrozen) {
            for (int i = 0; i < eggs.size(); i++) {
                model.Egg egg = eggs.get(i);
                egg.update();
                if (!egg.isActive()) {
                    eggs.remove(i);
                    i--;
                }
            }
        }

        // ۵. آپدیت پاورآپ‌های در حال سقوط
        for (int i = 0; i < powerUps.size(); i++) {
            PowerUp p = powerUps.get(i);
            p.update();
            if (!p.isActive()) {
                powerUps.remove(i);
                i--;
            }
        }

        // ۶. سیستم برخورد تیرهای هواپیما به مرغ‌ها + شانس سقوط ۲۰٪ پاورآپ
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

                        // شانس ۲۰ درصدی سقوط پاورآپ در لول‌های معمولی (نه غول‌ها)
                        if (currentLevel != 4 && currentLevel != 8 && Math.random() < 0.20) {
                            PowerUpType[] types = PowerUpType.values();
                            PowerUpType randomType = types[(int) (Math.random() * types.length)];
                            powerUps.add(new PowerUp(enemy.getX(), enemy.getY(), randomType));
                        }

                        enemies.remove(j);
                        j--;
                    }
                    break;
                }
            }
        }

        // ۷. سیستم برخورد تخم‌مرغ‌ها به هواپیما (با بررسی وضعیت شیلد)
        for (int i = 0; i < eggs.size(); i++) {
            model.Egg egg = eggs.get(i);
            if (egg.getBounds().intersects(plane.getBounds())) {
                eggs.remove(i);
                i--;

                // اگر سپر فعال نباشد، هواپیما جان از دست می‌دهد
                if (!plane.isShieldActive()) {
                    plane.setLives(plane.getLives() - 1);
                }
            }
        }

        // ۸. سیستم برخورد هواپیما با پاورآپ‌ها و جذب آن‌ها
        for (int i = 0; i < powerUps.size(); i++) {
            PowerUp p = powerUps.get(i);
            if (p.getBounds().intersects(plane.getBounds())) {

                // اعمال پاورآپ روی مدل Plane
                plane.applyPowerUp(p.getType());

                // هندل کردن اثر محیطی بمب یخ‌زن در پنل
                if (p.getType() == PowerUpType.FREEZE_BOMB) {
                    isFrozen = true;
                    freezeEndTime = System.currentTimeMillis() + 3000; // ۳ ثانیه یخ‌زدگی
                }

                powerUps.remove(i);
                i--;
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 🌌 رسم تصویر پس‌زمینه کهکشانی
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, null);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, 800, 600);
        }

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

        // رسم پاورآپ‌های در حال سقوط
        for (PowerUp p : powerUps) {
            p.draw(g2d);
        }

        // رسم اطلاعات بازی (HUD)
        drawHUD(g2d);

        // افکت بصری یخ‌زدگی محیطی
        if (isFrozen) {
            g2d.setColor(new Color(0, 191, 255, 35)); // لایه آبی شفاف یخی
            g2d.fillRect(0, 0, 800, 600);
            g2d.setColor(Color.CYAN);
            g2d.setFont(new Font("Arial", Font.BOLD, 18));
            g2d.drawString("❄️ ENEMIES FROZEN ❄️", 310, 35);
        }

        // صفحه توقف بازی (PAUSED)
        if (isPaused) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, 800, 600);
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.drawString("PAUSED", 330, 300);
        }

        // صفحه باخت نهایی (Game Over)
        if (plane.getLives() <= 0) {
            g2d.setColor(new Color(150, 0, 0, 180));
            g2d.fillRect(0, 0, 800, 600);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.drawString("GAME OVER", 260, 300);
        }

        // صفحه پیروزی نهایی بازی (Victory)
        if (currentLevel == 8 && enemies.isEmpty()) {
            gameTimer.stop();
            g2d.setColor(new Color(0, 150, 0, 180));
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
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) plane.setDy(plane.getSpeed());

        if (key == KeyEvent.VK_SPACE) {
            if (plane.canShoot()) {
                int bulletX = plane.getX() + (plane.getWidth() / 2);
                int bulletY = plane.getY();
                int fireLevel = plane.getFireLevel();

                // مدیریت شلیک چندگانه موازی/بادبزنی بر اساس لول شلیک (بند ۴.۶)
                if (fireLevel == 1) {
                    bullets.add(new model.Bullet(bulletX, bulletY));
                } else if (fireLevel == 2) {
                    // دو تیر موازی
                    bullets.add(new model.Bullet(bulletX - 15, bulletY));
                    bullets.add(new model.Bullet(bulletX + 15, bulletY));
                } else {
                    // لول ۳ و بالاتر: سه تیر همزمان عریض
                    bullets.add(new model.Bullet(bulletX, bulletY));
                    bullets.add(new model.Bullet(bulletX - 25, bulletY));
                    bullets.add(new model.Bullet(bulletX + 25, bulletY));
                }
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
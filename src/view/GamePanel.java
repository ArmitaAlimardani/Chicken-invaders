package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import model.Plane;
import model.PowerUp;
import model.PowerUpType;
import model.enemy.Boss;
import model.enemy.Enemy;

import model.database.DatabaseManager;
import model.database.UserSession;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer gameTimer;
    private Plane plane;
    private boolean isPaused = false;
    private int score = 0;
    private int currentLevel = 1;
    private boolean scoreSaved = false;

    private boolean isVictory = false;

    private MainMenu mainMenu;

    private Image backgroundImage;

    private java.util.ArrayList<model.Bullet> bullets = new java.util.ArrayList<>();
    private java.util.ArrayList<Enemy> enemies = new java.util.ArrayList<>();
    private java.util.ArrayList<model.Egg> eggs = new java.util.ArrayList<>();

    // لیست نگهداری پاورآپ‌های فعال در صفحه
    private java.util.ArrayList<PowerUp> powerUps = new java.util.ArrayList<>();

    private model.GridManager gridManager;

    // لیست نگهداری افکت‌های انفجار فعال در صفحه (بند ۴.۷)
    private java.util.ArrayList<model.Explosion> explosions = new java.util.ArrayList<>();

    // متغیرهای مدیریت پاورآپ بمب یخ‌زن
    private boolean isFrozen = false;
    private long freezeEndTime = 0;

    //  تغییر ورودی سازنده برای اتصال منوی اصلی
    public GamePanel(MainMenu mainMenu) {
        this.mainMenu = mainMenu;

        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        ImageIcon bgIcon = new ImageIcon("icon\\background.jpg");
        if (bgIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            this.backgroundImage = bgIcon.getImage().getScaledInstance(800, 600, Image.SCALE_SMOOTH);
        }

        plane = new Plane(362, 480);
        gridManager = new model.GridManager(currentLevel, enemies, eggs);

        gameTimer = new Timer(16, this);
        gameTimer.start();

        //  سیستم هوشمند KeyBinding برای بازگشت قطعی به منوی اصلی با کلید Enter
        this.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "returnToMenu");

        // ۱. اکشن مربوط به حالت پیروزی (کلید Enter)
        this.getActionMap().put("returnToMenu", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (isVictory) {
                    Window topFrame = SwingUtilities.getWindowAncestor(GamePanel.this);
                    if (topFrame != null) {
                        topFrame.dispose();
                    }

                    // 🚀 پخش مجدد موزیک متن هنگام بازگشت به منوی اصلی
                    controller.SoundManager.playBackgroundMusic();

                    if (GamePanel.this.mainMenu != null) {
                        GamePanel.this.mainMenu.setVisible(true);
                    }
                }
            }
        });


        //  سیستم هوشمند KeyBinding برای بازگشت به منوی اصلی پس از Game Over (کلید ESC)
        this.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "returnToMenuOnGameOver");

        // اختیاری: فعال کردن کلید ENTER برای حالت باخت علاوه بر ESC
        this.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "returnToMenuOnGameOver");

        // ۲. اکشن مربوط به حالت باخت (کلید ESC یا Enter)
        this.getActionMap().put("returnToMenuOnGameOver", new javax.swing.AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (plane.getLives() <= 0) {
                    java.awt.Window topFrame = javax.swing.SwingUtilities.getWindowAncestor(GamePanel.this);
                    if (topFrame != null) {
                        topFrame.dispose();
                    }

                    // 🚀 پخش مجدد موزیک متن هنگام بازگشت به منوی اصلی
                    controller.SoundManager.playBackgroundMusic();

                    if (GamePanel.this.mainMenu != null) {
                        GamePanel.this.mainMenu.setVisible(true);
                    }
                }
            }
        });
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

            // 🚀 [اصلاح صوتی ۱]: توقف موزیک متن و پخش صدای باخت بازی
            controller.SoundManager.stopBackgroundMusic();
            controller.SoundManager.playGameOverSound();

            // ذخیره رکورد در صورت باخت
            if (!scoreSaved && UserSession.isLoggedIn()) {
                int finalLvl = gridManager.getCurrentLevel();
                DatabaseManager.saveGameRecord(UserSession.getUsername(), score, finalLvl, "ON");
                scoreSaved = true;
                System.out.println(" Game Over record saved into database!");
            }
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

            for (Enemy enemy : enemies) {
                if (enemy instanceof Boss) {
                    ((Boss) enemy).updateAttack(eggs);
                }
            }
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

                if (enemy.y > 500) {
                    if (!plane.isShieldActive()) {
                        plane.setLives(plane.getLives() - 1);

                        // 🚀 افکت صوتی برخورد: کم شدن جان به دلیل نفوذ مرغ
                        controller.SoundManager.playCollisionSound();

                        explosions.add(new model.Explosion(plane.getX() + (plane.getWidth() / 2), plane.getY() + (plane.getHeight() / 2), Color.ORANGE));
                    }
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

        // ۶. سیستم برخورد تیرهای هواپیما به مرغ‌ها / غول‌ها
        if (currentLevel == 4 || currentLevel == 8) {
            if (!enemies.isEmpty()) {
                Enemy boss = enemies.get(0);

                for (int i = 0; i < bullets.size(); i++) {
                    model.Bullet b = bullets.get(i);

                    if (b.getBounds().intersects(boss.getBounds())) {
                        bullets.remove(i);
                        i--;

                        if (boss.isActive() && boss.getLives() > 0) {
                            boss.takeDamage();

                            if (!boss.isActive() || boss.getLives() <= 0) {
                                score += 100;

                                controller.SoundManager.playCollisionSound();

                                explosions.add(new model.Explosion(
                                        boss.getX() + (boss.getBounds().width / 2),
                                        boss.getY() + (boss.getBounds().height / 2),
                                        Color.ORANGE
                                ));

                                enemies.remove(boss);

                                if (currentLevel == 8) {
                                    gameTimer.stop();
                                    isVictory = true;

                                    // 🚀 [اصلاح صوتی ۲]: توقف موزیک متن و پخش صدای پیروزی در مرحله نهایی غول
                                    controller.SoundManager.stopBackgroundMusic();
                                    controller.SoundManager.playGameOverSound();

                                    if (!scoreSaved && UserSession.isLoggedIn()) {
                                        DatabaseManager.saveGameRecord(UserSession.getUsername(), score, 8, "ON");
                                        scoreSaved = true;
                                        System.out.println("🏆 Victory record saved into database!");
                                    }
                                    return;
                                }

                                gridManager.handleEnemyDeath(boss);
                                this.currentLevel = gridManager.getCurrentLevel();
                                return;
                            }
                        }
                    }
                }
            }
        }
        else {
            for (int i = 0; i < bullets.size(); i++) {
                model.Bullet b = bullets.get(i);

                for (int j = 0; j < enemies.size(); j++) {
                    Enemy enemy = enemies.get(j);

                    if (b.getBounds().intersects(enemy.getBounds())) {
                        bullets.remove(i);
                        i--;

                        if (enemy.isActive() && enemy.getLives() > 0) {
                            enemy.takeDamage();

                            if (!enemy.isActive() || enemy.getLives() <= 0) {
                                score += 10;
                                gridManager.handleEnemyDeath(enemy);

                                controller.SoundManager.playCollisionSound();

                                explosions.add(new model.Explosion(
                                        enemy.getX() + (enemy.getBounds().width / 2),
                                        enemy.getY() + (enemy.getBounds().height / 2),
                                        Color.RED
                                ));

                                if (Math.random() < 0.20) {
                                    PowerUpType[] types = PowerUpType.values();
                                    PowerUpType randomType = types[(int) (Math.random() * types.length)];
                                    powerUps.add(new PowerUp(enemy.getX(), enemy.getY(), randomType));
                                }

                                enemies.remove(j);
                                j--;
                            }
                        }
                        break;
                    }
                }
            }
        }

        // ۷. سیستم برخورد تخم‌مرغ‌ها به هواپیما
        for (int i = 0; i < eggs.size(); i++) {
            model.Egg egg = eggs.get(i);
            if (egg.getBounds().intersects(plane.getBounds())) {
                eggs.remove(i);
                i--;

                if (!plane.isShieldActive()) {
                    plane.setLives(plane.getLives() - 1);

                    controller.SoundManager.playCollisionSound();

                    explosions.add(new model.Explosion(plane.getX() + (plane.getWidth() / 2), plane.getY() + (plane.getHeight() / 2), Color.YELLOW));
                }
            }
        }

        // ۸. سیستم برخورد هواپیما با پاورآپ‌ها
        for (int i = 0; i < powerUps.size(); i++) {
            PowerUp p = powerUps.get(i);
            if (p.getBounds().intersects(plane.getBounds())) {
                plane.applyPowerUp(p.getType());

                if (p.getType() == PowerUpType.FREEZE_BOMB) {
                    isFrozen = true;
                    freezeEndTime = System.currentTimeMillis() + 3000;
                }

                powerUps.remove(i);
                i--;
            }
        }

        // ۹. آپدیت افکت‌های انفجار
        for (int i = 0; i < explosions.size(); i++) {
            model.Explosion exp = explosions.get(i);
            exp.update();
            if (!exp.isActive()) {
                explosions.remove(i);
                i--;
            }
        }

        // بررسی پیروزی نهایی در انتهای لول ۸
        if (currentLevel == 8 && enemies.isEmpty()) {
            gameTimer.stop();
            isVictory = true;
            this.requestFocusInWindow();

            // 🚀 [اصلاح صوتی ۳]: توقف موزیک متن و پخش صدای پیروزی نهایی
            controller.SoundManager.stopBackgroundMusic();
            controller.SoundManager.playGameOverSound();

            if (!scoreSaved && UserSession.isLoggedIn()) {
                DatabaseManager.saveGameRecord(UserSession.getUsername(), score, currentLevel, "Default Settings");
                scoreSaved = true;
                System.out.println("🏆 Victory record saved into database!");
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // رسم تصویر پس‌زمینه کهکشانی
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, 0, null);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, 800, 600);
        }

        // 💥 رسم افکت‌های بصری انفجار (قبل از هواپیما و المان‌ها رندر می‌شود تا روی پس‌زمینه بنشیند)
        for (model.Explosion exp : explosions) {
            exp.draw(g2d);
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
            g2d.setColor(new Color(0, 191, 255, 35));
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
        if (isVictory) {
            g2d.setColor(new Color(0, 150, 0, 180));
            g2d.fillRect(0, 0, 800, 600);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.drawString("VICTORY!", 290, 300);

            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.drawString("Press ENTER to return to Main Menu", 255, 350);
        }

        // صفحه باخت نهایی (Game Over)
        if (plane.getLives() <= 0) {
            g2d.setColor(new Color(150, 0, 0, 180));
            g2d.fillRect(0, 0, 800, 600);
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 48));
            g2d.drawString("GAME OVER", 260, 300);

            // اضافه کردن متن راهنما برای بازیکن هنگام باخت
            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.drawString("Press ESC or ENTER to return to Main Menu", 230, 350);
        }
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        //  استفاده از نام کاربری که لاگین کرده است
        String currentUser = UserSession.isLoggedIn() ? UserSession.getUsername() : "Guest";
        g2d.drawString("USER: " + currentUser, 20, 30);

        g2d.drawString("SCORE: " + score, 20, 55);
        g2d.drawString("LIVES: " + plane.getLives(), 20, 80);
        g2d.drawString("FIRE POWER: x" + plane.getFireLevel(), 20, 105);
        g2d.drawString("LEVEL: " + currentLevel, 700, 30);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        if (isVictory && key == KeyEvent.VK_ENTER) {
            Window topFrame = SwingUtilities.getWindowAncestor(this);
            if (topFrame != null) {
                topFrame.dispose();
            }

            //  پخش مجدد موزیک متن برای نمونه‌ای که تازه ساخته می‌شود
            controller.SoundManager.playBackgroundMusic();

            SwingUtilities.invokeLater(() -> {
                new view.MainMenu().setVisible(true);
            });
            return;
        }

        if (plane.getLives() <= 0 || (currentLevel == 8 && enemies.isEmpty())) return;

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) plane.setDx(-plane.getSpeed());
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) plane.setDx(plane.getSpeed());
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) plane.setDy(-plane.getSpeed());
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) plane.setDy(plane.getSpeed());

        if (key == KeyEvent.VK_SPACE) {

            controller.SoundManager.playShotSound();


            if (plane.canShoot()) {
                int bulletX = plane.getX() + (plane.getWidth() / 2);
                int bulletY = plane.getY();
                int fireLevel = plane.getFireLevel();

                if (fireLevel == 1) {
                    bullets.add(new model.Bullet(bulletX, bulletY));
                } else if (fireLevel == 2) {
                    bullets.add(new model.Bullet(bulletX - 15, bulletY));
                    bullets.add(new model.Bullet(bulletX + 15, bulletY));
                } else {
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
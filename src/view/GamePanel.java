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
    private int backgroundY = 0; // ردیابی موقعیت عمودی پس‌زمینه برای ایجاد افکت حرکت

    private boolean showLifeGainedMessage = false;
    private long lifeMessageEndTime = 0;

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

    // تغییر ورودی سازنده برای اتصال منوی اصلی
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

        // سیستم هوشمند KeyBinding برای بازگشت قطعی به منوی اصلی با کلید Enter
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

                    // پخش مجدد موزیک متن هنگام بازگشت به منوی اصلی
                    controller.SoundManager.playBackgroundMusic();

                    if (GamePanel.this.mainMenu != null) {
                        GamePanel.this.mainMenu.setVisible(true);
                    }
                }
            }
        });

        // سیستم هوشمند KeyBinding برای بازگشت به منوی اصلی پس از Game Over (کلید ESC)
        this.getInputMap(javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW)
                .put(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "returnToMenuOnGameOver");

        // فعال کردن کلید ENTER برای حالت باخت علاوه بر ESC
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

                    // پخش مجدد موزیک متن هنگام بازگشت به منوی اصلی
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

            // توقف موزیک متن و پخش صدای باخت بازی
            controller.SoundManager.stopBackgroundMusic();
            controller.SoundManager.playGameOverSound();

            // ذخیره رکورد در صورت باخت
            if (!scoreSaved && UserSession.isLoggedIn()) {
                int finalLvl = gridManager.getCurrentLevel();
                DatabaseManager.saveGameRecord(UserSession.getUsername(), score, finalLvl, "ON");
                scoreSaved = true;
                System.out.println(" Game Over record saved into database!");
            }

            // نمایش پیغام «بازی تمام شد»
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "بازی تمام شد!\nامتیاز نهایی شما: " + score, "Game Over", JOptionPane.ERROR_MESSAGE);
            });
            return;
        }

        // چک کردن برخورد هواپیما با مرغ‌ها
        if (!plane.isShieldActive()) { //  فقط اگر سپر فعال نباشد
            for (int i = 0; i < enemies.size(); i++) {
                Enemy enemy = enemies.get(i);

                if (plane.getBounds().intersects(enemy.getBounds())) {
                    // ۱. کاهش جان هواپیما
                    plane.setLives(plane.getLives() - 1);

                    // ۲. پخش صدای برخورد
                    controller.SoundManager.playCollisionSound();

                    // ۳. ایجاد افکت انفجار در محل هواپیما
                    explosions.add(new model.Explosion(
                            plane.getX() + (plane.getWidth() / 2),
                            plane.getY() + (plane.getHeight() / 2),
                            Color.RED));

                    // ۴. حذف مرغی که با هواپیما برخورد کرده (یا حذف هواپیما/کاهش جان)
                    // طبق منطق بازی، حذف مرغ برخورد کرده عادلانه است
                    enemies.remove(i);
                    i--;
                }
            }
        }

        //  حرکت دادن موقعیت پس‌زمینه (فقط در صورتی که بازی متوقف نباشد)
        if (!isPaused) {
            backgroundY += 2; // سرعت اسکرول شدن ستاره‌ها به سمت پایین
            if (backgroundY >= 600) {
                backgroundY = 0; // ریست چرخه به صورت نرم و بی‌انتها
            }
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

        // بررسی اینکه آیا مرحله معمولی تمام شده و باید پاداش پاک‌سازی بگیرد
        int oldLevel = this.currentLevel;
        this.currentLevel = gridManager.getCurrentLevel();

        // پاداش ۲۰۰ امتیازی پاک کردن کامل یک مرحله (مراحل ۱،۲،۳،۵،۶،۷)
        if (this.currentLevel > oldLevel && oldLevel != 4 && oldLevel != 8) {
            score += 200;
            System.out.println("🎉 +200 Clearance Bonus for Level " + oldLevel);
        }

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

                        // افکت صوتی برخورد: کم شدن جان به دلیل نفوذ مرغ
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
                                controller.SoundManager.playCollisionSound();

                                explosions.add(new model.Explosion(
                                        boss.getX() + (boss.getBounds().width / 2),
                                        boss.getY() + (boss.getBounds().height / 2),
                                        Color.ORANGE
                                ));

                                enemies.remove(boss);

                                //  شکست غول اول (مرحله ۴): ۵۰۰ امتیاز اضافی
                                if (currentLevel == 4) {
                                    score += 500;
                                    System.out.println("Boss 1 Defeated! +500 Score!");
                                }
                                //  شکست غول نهایی (مرحله ۸): ۱۰۰۰ امتیاز اضافی و پیغام «پیروزی»
                                else if (currentLevel == 8) {
                                    score += 1000;
                                    gameTimer.stop();
                                    isVictory = true;

                                    // توقف موزیک متن و پخش صدای پیروزی در مرحله نهایی غول
                                    controller.SoundManager.stopBackgroundMusic();
                                    controller.SoundManager.playGameOverSound();

                                    if (!scoreSaved && UserSession.isLoggedIn()) {
                                        DatabaseManager.saveGameRecord(UserSession.getUsername(), score, 8, "ON");
                                        scoreSaved = true;
                                        System.out.println("Victory record saved into database!");
                                    }

                                    // نمایش پیغام «پیروزی» بر اساس بند ۵
                                    SwingUtilities.invokeLater(() -> {
                                        JOptionPane.showMessageDialog(this, "شما برنده شدید!\nامتیاز نهایی: " + score, "پیروزی", JOptionPane.INFORMATION_MESSAGE);
                                    });
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

                                //  امتیازدهی دقیق بر اساس نوع مرغ (بند ۵ صورت سوال)
                                String className = enemy.getClass().getSimpleName();
                                if (className.contains("Shooter")) {
                                    score += 25;
                                } else if (className.contains("Zigzag")) {
                                    score += 20;
                                } else if (className.contains("Fast")) {
                                    score += 15;
                                } else {
                                    score += 10; // Normal Chicken
                                }

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
                else if (p.getType() == PowerUpType.EXTRA_LIFE) {
                    showLifeGainedMessage = true;
                    lifeMessageEndTime = System.currentTimeMillis() + 1500; // پیام تا ۱.۵ ثانیه زنده بماند
                }

                powerUps.remove(i);
                i--;
            }
        }
        if (showLifeGainedMessage && System.currentTimeMillis() > lifeMessageEndTime) {
            showLifeGainedMessage = false;
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

        // بررسی پیروزی نهایی در انتهای لول ۸ (اگر غول قبلاً پاک شده بود)
        if (currentLevel == 8 && enemies.isEmpty()) {
            gameTimer.stop();
            isVictory = true;
            this.requestFocusInWindow();

            controller.SoundManager.stopBackgroundMusic();
            controller.SoundManager.playGameOverSound();

            if (!scoreSaved && UserSession.isLoggedIn()) {
                DatabaseManager.saveGameRecord(UserSession.getUsername(), score, currentLevel, "Default Settings");
                scoreSaved = true;
                System.out.println("Victory record saved into database!");
            }

            // نمایش پیغام «پیروزی» بر اساس بند ۵
            SwingUtilities.invokeLater(() -> {
                JOptionPane.showMessageDialog(this, "شما برنده شدید!\nامتیاز نهایی: " + score, "پیروزی", JOptionPane.INFORMATION_MESSAGE);
            });
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        //  رسم تصویر پس‌زمینه کهکشانی به صورت متحرک و بی‌انتها (Scrolling Background)
        if (backgroundImage != null) {
            // تصویر اول در موقعیت جاری
            g2d.drawImage(backgroundImage, 0, backgroundY, null);
            // تصویر دوم درست بالای تصویر اول قرار می‌گیرد تا فضای خالی سقف را پر کند
            g2d.drawImage(backgroundImage, 0, backgroundY - 600, null);
        } else {
            g2d.setColor(Color.BLACK);
            g2d.fillRect(0, 0, 800, 600);
        }

        // رسم افکت‌های بصری انفجار
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

        // ️ افکت شیشه‌ای یخ‌زدگی محیطی (چاپ متن موازی از اینجا کاملاً حذف شد)
        if (isFrozen) {
            g2d.setColor(new Color(0, 191, 255, 35));
            g2d.fillRect(0, 0, 800, 600);
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
            g2d.drawString("GAME OVER", 260, 280); // مقداری Y را بالاتر بردیم تا جا برای متن جدید باز شود

            g2d.setFont(new Font("Arial", Font.PLAIN, 18));
            g2d.drawString("Press SPACE to Replay / Restart Game", 245, 330);

            g2d.setFont(new Font("Arial", Font.PLAIN, 16));
            g2d.setColor(new Color(230, 230, 230));
            g2d.drawString("Press ESC or ENTER to return to Main Menu", 235, 370);
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
    }

    private void drawHUD(Graphics2D g2d) {
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Arial", Font.BOLD, 14));

        // ۱. نمایش نام کاربری واردشده
        String currentUser = UserSession.isLoggedIn() ? UserSession.getUsername() : "Guest";
        g2d.drawString("USER: " + currentUser, 20, 30);

        // ۲. نمایش امتیاز فعلی
        g2d.drawString("SCORE: " + score, 180, 30);

        // ۳. نمایش تعداد جان باقی‌مانده
        g2d.drawString("LIVES: " + plane.getLives(), 320, 30);

        // ۴. نمایش تعداد تیرهای همزمان فعلی (بند ۵)
        int currentGuns = plane.getFireLevel(); // لول تفنگ که ۱ یا ۲ یا ۳ تیر شلیک می‌کند
        g2d.drawString("GUNS SYNC: [ " + currentGuns + " BULLETS ]", 450, 30);

        // ۵. نمایش مرحله جاری (Level)
        g2d.drawString("LEVEL: " + currentLevel, 710, 30);

        //  ۶. مدیریت چیدمان پویای وضعیت پاورآپ‌های فعال (بدون تداخل و کاملاً زیر هم)
        int HUD_Y = 65;

        if (plane.isShieldActive()) {
            g2d.setColor(Color.CYAN);
            g2d.drawString("SHIELD ACTIVE", 20, HUD_Y);
            HUD_Y += 22;
        }

        if (isFrozen) {
            g2d.setColor(new Color(30, 144, 255));
            g2d.drawString("FREEZE BOMB ACTIVE", 20, HUD_Y);
            HUD_Y += 22;
        }

        if (currentGuns > 1) {
            g2d.setColor(Color.ORANGE);
            g2d.drawString("WEAPON BOOST ACTIVE (Level " + currentGuns + ")", 20, HUD_Y);
            HUD_Y += 22;
        }

        if (showLifeGainedMessage) {
            g2d.setColor(new Color(50, 205, 50));
            g2d.drawString("EXTRA LIFE GAINED! (+1 LIFE)", 20, HUD_Y);
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {
        int key = e.getKeyCode();

        // ۱. مدیریت دکمه ENTER در حالت پیروزی برای بازگشت به منوی اصلی
        if (isVictory && key == KeyEvent.VK_ENTER) {
            Window topFrame = SwingUtilities.getWindowAncestor(this);
            if (topFrame != null) {
                topFrame.dispose();
            }

            controller.SoundManager.playBackgroundMusic();

            SwingUtilities.invokeLater(() -> {
                new view.MainMenu().setVisible(true);
            });
            return;
        }

        // 🚀 ۲. مدیریت کلید SPACE (بالاترین اولویت برای ری‌استارت در حالت باخت یا شلیک/رزومه در حین بازی)
        if (key == KeyEvent.VK_SPACE) {
            if (plane.getLives() <= 0) {
                restartGame(); // اگر باخته بود، بازی از نو شروع شود
                return;
            }

            if (isPaused) {
                isPaused = false; // اگر متوقف بود، ادامه پیدا کند
            } else {
                // روال عادی: شلیک گلوله در حین بازی
                if (plane.canShoot()) {
                    controller.SoundManager.playShotSound();

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
            return;
        }

        // 🛑 قفل برای بقیه دکمه‌ها (اگر بازیکن باخته باشد یا بازی در وضعیت پیروزی نهایی باشد، جهت‌ها کار نکنند)
        if (plane.getLives() <= 0 || (currentLevel == 8 && enemies.isEmpty())) return;
        if (isPaused) return; // قفل دکمه‌های حرکتی در حالت Pause

        // ۳. کنترل‌های حرکتی استاندارد (جهت‌ها و کلیدهای WASD)
        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) plane.setDx(-plane.getSpeed());
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) plane.setDx(plane.getSpeed());
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) plane.setDy(-plane.getSpeed());
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) plane.setDy(plane.getSpeed());

        // ۴. توقف / ادامه بازی با کلید P
        if (key == KeyEvent.VK_P) {
            isPaused = !isPaused;
        }

        // ۵. بازگشت به منوی اصلی با کلید ESC در حین بازی
        if (key == KeyEvent.VK_ESCAPE) {
            gameTimer.stop();
            controller.SoundManager.stopBackgroundMusic();

            Window topFrame = SwingUtilities.getWindowAncestor(this);
            if (topFrame != null) {
                topFrame.dispose();
            }

            controller.SoundManager.playBackgroundMusic();
            if (this.mainMenu != null) {
                this.mainMenu.setVisible(true);
            }
        }

        // ۶. باز کردن منوی تنظیمات صدا در حین بازی با کلید M
        if (key == KeyEvent.VK_M) {
            isPaused = true;
            Window topFrame = SwingUtilities.getWindowAncestor(this);
            JDialog soundMenu = new JDialog(topFrame, "تنظیمات صدا", Dialog.ModalityType.APPLICATION_MODAL);
            soundMenu.setSize(300, 200);
            soundMenu.setLayout(new GridLayout(3, 1, 10, 10));
            soundMenu.setLocationRelativeTo(this);

            JButton toggleSoundBtn = new JButton(controller.SoundManager.isMusicEnabled() ? "🔈 قطع صدا" : "🔊 وصل صدا");
            toggleSoundBtn.addActionListener(evt -> {
                controller.SoundManager.toggleAllSounds();
                toggleSoundBtn.setText(controller.SoundManager.isMusicEnabled() ? "🔈 قطع صدا" : "🔊 وصل صدا");
            });

            JButton closeBtn = new JButton("بازگشت به بازی");
            closeBtn.addActionListener(evt -> soundMenu.dispose());

            JLabel infoLabel = new JLabel("تنظیمات صوتی حین بازی", SwingConstants.CENTER);
            infoLabel.setFont(new Font("Arial", Font.BOLD, 14));

            soundMenu.add(infoLabel);
            soundMenu.add(toggleSoundBtn);
            soundMenu.add(closeBtn);

            soundMenu.setVisible(true);
        }
    }

    private void restartGame() {
        // ۱. ریست کردن مشخصات فضاپیما
        plane.setLives(3);             // بازگرداندن ۳ جان اصلی (یا هر تعداد که پیش‌فرض بازی است)
        plane.setFireLevel(1);          // ریست کردن لول تیر
        plane.setLocation(375, 500);    // وسط‌چین کردن مجدد فضاپیما در پایین صفحه

        // ۲. صفر کردن امتیاز و لول
        score = 0;
        currentLevel = 1;
        scoreSaved = false;
        isVictory = false;
        isPaused = false;
        isFrozen = false;

        // ۳. خالی کردن لیست‌های پویا
        bullets.clear();
        enemies.clear();
        eggs.clear();
        powerUps.clear();
        explosions.clear();

        // ۴. ریست کردن گرید مرغ‌ها به مرحله اول
        gridManager.resetToLevel(1);
        this.currentLevel = gridManager.getCurrentLevel();

        // ۵. راه اندازی مجدد موزیک و تایمر بازی
        controller.SoundManager.playBackgroundMusic();
        gameTimer.start();

        // درخواست فوکوس مجدد روی پنل برای دریافت کلیدها
        this.requestFocusInWindow();
        repaint();

        System.out.println("🔄 Game successfully restarted via SPACE key!");
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
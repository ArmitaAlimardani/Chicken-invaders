package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import model.Plane;
import model.enemy.Enemy;
import model.enemy.NormalEnemy;
import model.enemy.FastEnemy;
import model.enemy.ZigzagEnemy;
import model.enemy.ShooterEnemy;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer gameTimer;
    private Plane plane;
    private boolean isPaused = false;
    private int score = 0;
    private int currentLevel = 1;

    private java.util.ArrayList<model.Bullet> bullets = new java.util.ArrayList<>();
    private java.util.ArrayList<Enemy> enemies = new java.util.ArrayList<>();

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        plane = new Plane(362, 480);

        enemies.add(new NormalEnemy(150, 80, currentLevel));
        enemies.add(new FastEnemy(300, 80, currentLevel));
        enemies.add(new ZigzagEnemy(450, 80, currentLevel));
        enemies.add(new ShooterEnemy(600, 80, currentLevel));

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
        plane.update();

        for (int i = 0; i < bullets.size(); i++) {
            model.Bullet b = bullets.get(i);
            b.update();

            if (!b.isActive()) {
                bullets.remove(i);
                i--;
            }
        }

        for (int i = 0; i < enemies.size(); i++) {
            Enemy enemy = enemies.get(i);
            enemy.update();

            if (!enemy.isActive()) {
                enemies.remove(i);
                i--;
            }
        }

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

                        int targetGridX = enemy.getX();
                        int targetGridY = enemy.getY();

                        int spawnX = (Math.random() > 0.5) ? 0 : 750;
                        int spawnY = 0;

                        Enemy replacement = null;
                        int rand = (int) (Math.random() * 4);

                        if (rand == 0) replacement = new NormalEnemy(spawnX, spawnY, currentLevel);
                        else if (rand == 1) replacement = new FastEnemy(spawnX, spawnY, currentLevel);
                        else if (rand == 2) replacement = new ZigzagEnemy(spawnX, spawnY, currentLevel);
                        else replacement = new ShooterEnemy(spawnX, spawnY, currentLevel);

                        replacement.setTargetPosition(targetGridX, targetGridY);
                        enemies.add(replacement);
                    }

                    break;
                }
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

        // رسم مرغ‌ها
        for (Enemy enemy : enemies) {
            enemy.draw(g2d);
        }

        // رسم اطلاعات بازی (HUD)
        drawHUD(g2d);

        // صفحه توقف بازی
        if (isPaused) {
            g2d.setColor(new Color(0, 0, 0, 150));
            g2d.fillRect(0, 0, 800, 600);
            g2d.setColor(Color.YELLOW);
            g2d.setFont(new Font("Arial", Font.BOLD, 36));
            g2d.drawString("PAUSED", 330, 300);
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
        int key = e.getKeyCode();

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) plane.setDx(-plane.getSpeed());
        if (key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) plane.setDx(plane.getSpeed());
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W) plane.setDy(-plane.getSpeed());
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) plane.setDx(0); // رفع باگ ایست پیش‌فرض جهت افقی
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
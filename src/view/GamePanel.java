package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import model.Plane;

public class GamePanel extends JPanel implements ActionListener, KeyListener {
    private Timer gameTimer;
    private Plane plane;
    private boolean isPaused = false;
    private int score = 0;
    private int currentLevel = 1;
    private java.util.ArrayList<model.Bullet> bullets = new java.util.ArrayList<>();

    public GamePanel() {
        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        addKeyListener(this);

        // ساخت هواپیما در پایین و وسط صفحه
        plane = new Plane(310, 480);

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
                i --;
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
        for (model.Bullet b : bullets) {
            b.draw(g2d);
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
        if (key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) plane.setDy(plane.getSpeed());

        if (key == KeyEvent.VK_SPACE) {
            plane.shootMock();
        }

        if (key == KeyEvent.VK_P) {
            isPaused = !isPaused;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        int key = e.getKeyCode();

        if ((key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A) && plane.getX() < 0) {
            plane.setDx(0);
        }
        if ((key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) && plane.getX() > 0) {
            plane.setDx(0);
        }

        if (key == KeyEvent.VK_LEFT || key == KeyEvent.VK_A || key == KeyEvent.VK_RIGHT || key == KeyEvent.VK_D) {
            plane.setDx(0);
        }
        if (key == KeyEvent.VK_UP || key == KeyEvent.VK_W || key == KeyEvent.VK_DOWN || key == KeyEvent.VK_S) {
            plane.setDy(0);
        }

        if (key == KeyEvent.VK_SPACE) {
            if (plane.canShoot()) {
                // محاسبات برای شلیک دقیقاً از وسط و نوک سفینه
                int bulletX = plane.getX() + (plane.getWidth() / 2);
                int bulletY = plane.getY();

                bullets.add(new model.Bullet(bulletX, bulletY));

                // این لاگ آزمایشی قبلی را هم نگه می‌داریم
                plane.shootMock();
            }
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
}
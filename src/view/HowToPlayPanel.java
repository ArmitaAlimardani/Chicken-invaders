package view;

import javax.swing.*;
import java.awt.*;

public class HowToPlayPanel extends JPanel {
    private Image backgroundImage;
    private JDialog dialog;
    private int backgroundY = 0;
    private Timer animationTimer;

    public HowToPlayPanel(JDialog dialog) {
        this.dialog = dialog;

        // تنظیم ابعاد دقیق و ساختار عمودی مقتدر
        setPreferredSize(new Dimension(800, 600));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // ۱. بارگذاری تصویر پس‌زمینه کهکشانی
        ImageIcon bgIcon = new ImageIcon("icon/background.jpg");
        if (bgIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            this.backgroundImage = bgIcon.getImage();
        }

        // 🌟 کاهش فاصله از ۵۵ به ۳۰ برای شیفت دادن کل محتوا به سمت بالا
        add(Box.createVerticalStrut(30));

        // ۲. عنوان اصلی صفحه
        JLabel lblTitle = new JLabel("HOW TO PLAY");
        lblTitle.setFont(new Font("Impact", Font.PLAIN, 40));
        lblTitle.setForeground(Color.YELLOW);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lblTitle);

        add(Box.createVerticalStrut(25));

        // ۳. متن راهنما با ساختار مهار شده و فونت کاملاً خوانا
        JLabel lblGuide = new JLabel();
        lblGuide.setAlignmentX(Component.CENTER_ALIGNMENT);

        String guideHTML = "<html><div dir='ltr' style='font-family:\"Segoe UI\", Arial, sans-serif; color:#E0E0E0; text-align:center; width:620px; text-shadow: 1px 1px 3px #000000;'>"
                + "<p style='font-size:16px; margin-bottom:8px;'><b style='color:#00BFFF; letter-spacing: 1px;'>🕹️ CONTROLS</b></p>"
                + "<p style='font-size:13px; margin:3px 0;'>Move Left: <span style='color:#FFF; font-weight:bold;'>LEFT Arrow / A</span></p>"
                + "<p style='font-size:13px; margin:3px 0;'>Move Right: <span style='color:#FFF; font-weight:bold;'>RIGHT Arrow / D</span></p>"
                + "<p style='font-size:13px; margin:3px 0;'>Move Up: <span style='color:#FFF; font-weight:bold;'>UP Arrow / W</span></p>"
                + "<p style='font-size:13px; margin:3px 0;'>Move Down: <span style='color:#FFF; font-weight:bold;'>DOWN Arrow / S</span></p>"
                + "<p style='font-size:13px; margin:3px 0;'>Shoot: <span style='color:#FFF; font-weight:bold;'>SPACEBAR</span></p>"
                + "<p style='font-size:13px; margin:3px 0;'>Pause Game: <span style='color:#FFF; font-weight:bold;'>P Key</span></p>"
                + "<br>"
                + "<p style='font-size:16px; margin-bottom:8px;'><b style='color:#FF4500; letter-spacing: 1px;'>🚀 GAME RULES</b></p>"
                + "<p style='font-size:13px; margin:5px 0; font-family:Tahoma, sans-serif; font-weight:500;'>مرغ‌ها را قبل از رسیدن به انتهای صفحه نابود کنید.</p>"
                + "<p style='font-size:13px; margin:5px 0; font-family:Tahoma, sans-serif; font-weight:500;'>با جمع‌آوری پاورآپ‌ها، قدرت شلیک خود را ارتقا دهید.</p>"
                + "<p style='font-size:13px; margin:5px 0; font-family:Tahoma, sans-serif; font-weight:500;'>بمب یخ‌زن دشمنان را به مدت ۳ ثانیه منجمد می‌کند.</p>"
                + "<p style='font-size:13px; margin:5px 0; font-family:Tahoma, sans-serif; font-weight:500;'>در مراحل ۴ و ۸ آماده رویارویی با غول بزرگ باشید!</p>"
                + "</div></html>";

        lblGuide.setText(guideHTML);
        add(lblGuide);

        // فاصله بین متن و دکمه بازگشت
        add(Box.createVerticalStrut(20));

        // ۴. دکمهٔ بازگشت شکیل و خوش‌استایل
        JButton btnBack = new JButton("BACK");
        btnBack.setFont(new Font("Arial", Font.BOLD, 15));
        btnBack.setBackground(new Color(45, 45, 45));
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusable(false);
        btnBack.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnBack.setMaximumSize(new Dimension(140, 40));
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);

        // افکت Hover
        btnBack.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) { btnBack.setBackground(Color.DARK_GRAY); }
            public void mouseExited(java.awt.event.MouseEvent evt) { btnBack.setBackground(new Color(45, 45, 45)); }
        });

        btnBack.addActionListener(e -> {
            if (animationTimer != null) {
                animationTimer.stop();
            }
            if (this.dialog != null) {
                this.dialog.dispose();
            }
        });
        add(btnBack);

        // ۵. راه‌اندازی تایمر حرکت پس‌زمینه
        animationTimer = new Timer(16, e -> {
            backgroundY += 1;
            if (backgroundY >= 600) {
                backgroundY = 0;
            }
            repaint();
        });
        animationTimer.start();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // رسم پس‌زمینه متحرک
        if (backgroundImage != null) {
            g2d.drawImage(backgroundImage, 0, backgroundY, getWidth(), getHeight(), null);
            g2d.drawImage(backgroundImage, 0, backgroundY - getHeight(), getWidth(), getHeight(), null);
        } else {
            g2d.setColor(new Color(10, 10, 20));
            g2d.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
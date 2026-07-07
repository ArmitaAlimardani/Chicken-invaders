package view;

import javax.swing.*;
import java.awt.*;

public class HowToPlayPanel extends JPanel {
    private Image backgroundImage;
    private JDialog dialog; // برای مدیریت بستن پنجره

    public HowToPlayPanel(JDialog dialog) {
        this.dialog = dialog;

        // تنظیم ابعاد دقیق و ساختار عمودی مقتدر
        setPreferredSize(new Dimension(800, 600));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setOpaque(false);

        // ۱. بارگذاری قطعی تصویر پس‌زمینه کهکشانی
        ImageIcon bgIcon = new ImageIcon("icon/background.jpg");
        if (bgIcon.getImageLoadStatus() == MediaTracker.COMPLETE) {
            this.backgroundImage = bgIcon.getImage();
        }

        // ایجاد فاصله‌گذاری‌های استاندارد از بالا
        add(Box.createVerticalStrut(40));

        // ۲. عنوان اصلی صفحه (بزرگ و درخشان در مرکز)
        JLabel lblTitle = new JLabel("HOW TO PLAY");
        lblTitle.setFont(new Font("Arial", Font.BOLD, 42));
        lblTitle.setForeground(Color.YELLOW);
        lblTitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(lblTitle);

        add(Box.createVerticalStrut(30));

        // ۳. متن راهنما با ساختار مهار شده و فونت کاملاً خوانا
        JLabel lblGuide = new JLabel();
        lblGuide.setAlignmentX(Component.CENTER_ALIGNMENT);

        String guideHTML = "<html><div dir='ltr' style='font-family:Arial; color:white; text-align:center; width:600px;'>"
                + "<p style='font-size:20px; margin-bottom:10px;'><b style='color:#00BFFF;'>🕹️ CONTROLS</b></p>"
                + "<p style='font-size:15px; margin:4px 0;'>Move Left: <b>LEFT Arrow / A</b></p>"
                + "<p style='font-size:15px; margin:4px 0;'>Move Right: <b>RIGHT Arrow / D</b></p>"
                + "<p style='font-size:15px; margin:4px 0;'>Move Up: <b>UP Arrow / W</b></p>"
                + "<p style='font-size:15px; margin:4px 0;'>Move Down: <b>DOWN Arrow / S</b></p>"
                + "<p style='font-size:15px; margin:4px 0;'>Shoot: <b>SPACEBAR</b></p>"
                + "<p style='font-size:15px; margin:4px 0;'>Pause Game: <b>P Key</b></p>"
                + "<br>"
                + "<p style='font-size:20px; margin-bottom:10px;'><b style='color:#FF4500;'>🚀 GAME RULES</b></p>"
                + "<p style='font-size:15px; margin:5px 0; font-family:sans-serif;'>مرغ‌ها را قبل از رسیدن به انتهای صفحه نابود کنید.</p>"
                + "<p style='font-size:15px; margin:5px 0; font-family:sans-serif;'>با جمع‌آوری پاورآپ‌ها، قدرت شلیک خود را ارتقا دهید.</p>"
                + "<p style='font-size:15px; margin:5px 0; font-family:sans-serif;'>بمب یخ‌زن دشمنان را به مدت ۳ ثانیه منجمد می‌کند.</p>"
                + "<p style='font-size:15px; margin:5px 0; font-family:sans-serif;'>در مراحل ۴ و ۸ آماده رویارویی با غول بزرگ باشید!</p>"
                + "</div></html>";

        lblGuide.setText(guideHTML);
        add(lblGuide);

        add(Box.createVerticalStrut(40));

        // ۴. دکمهٔ بازگشت شکیل
        JButton btnBack = new JButton("BACK");
        btnBack.setFont(new Font("Arial", Font.BOLD, 18));
        btnBack.setBackground(Color.DARK_GRAY);
        btnBack.setForeground(Color.WHITE);
        btnBack.setFocusable(false);
        btnBack.setMaximumSize(new Dimension(160, 45));
        btnBack.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(btnBack);

        // اکشن دکمه بازگشت: بستن پنجرهٔ پاپ‌آپ و برگشت به منو
        btnBack.addActionListener(e -> {
            if (this.dialog != null) {
                this.dialog.dispose();
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        // رندر پس‌زمینه در کل ابعاد پنجره جدید
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), null);
        } else {
            g.setColor(new Color(10, 10, 20));
            g.fillRect(0, 0, getWidth(), getHeight());
        }
    }
}
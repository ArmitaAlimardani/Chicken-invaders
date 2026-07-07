package controller;

import javax.sound.sampled.*;
import java.io.File;

public class SoundManager {
    private static boolean isMusicEnabled = true;
    private static boolean isShotEnabled = true;
    private static boolean isCollisionEnabled = true;
    private static boolean isGameOverEnabled = true;

    private static Clip bgMusicClip;
    private static Clip gameOverMusicClip;

    // به‌روزرسانی پرچم‌ها بر اساس فرمت دیتابیس ("1,1,1,1")
    public static void updateSettings(String settingsStr) {
        String[] parts = settingsStr.split(",");
        if (parts.length == 4) {
            isMusicEnabled = parts[0].equals("1");
            isShotEnabled = parts[1].equals("1");
            isCollisionEnabled = parts[2].equals("1");
            isGameOverEnabled = parts[3].equals("1");
        }

        //  اگر کاربر صدا را OFF کرد، هر دو کلیپ را کاملاً خاموش کن
        if (!isMusicEnabled) {
            if (bgMusicClip != null && bgMusicClip.isRunning()) {
                bgMusicClip.stop();
            }
            if (gameOverMusicClip != null && gameOverMusicClip.isRunning()) {
                gameOverMusicClip.stop();
            }
        } else {
            // اگر صدا ON بود و هیچ‌کدام پخش نمی‌شدند، موزیک اصلی را لود کن
            if ((bgMusicClip == null || !bgMusicClip.isRunning()) &&
                    (gameOverMusicClip == null || !gameOverMusicClip.isRunning())) {
                playBackgroundMusic();
            }
        }
    }

    // ۱. موسیقی زمینه
    public static void playBackgroundMusic() {
        if (!isMusicEnabled) return;

        //  قبل از پخش موزیک اصلی، مطمئن شو موزیک پایان بازی قطع شده است
        if (gameOverMusicClip != null && gameOverMusicClip.isRunning()) {
            gameOverMusicClip.stop();
        }

        try {
            if (bgMusicClip == null) {
                File musicFile = new File("sounds/Chicken Invaders 2 Remastered OST - Main Theme.wav");
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(musicFile);
                bgMusicClip = AudioSystem.getClip();
                bgMusicClip.open(audioStream);
            }
            if (!bgMusicClip.isRunning()) {
                bgMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
                bgMusicClip.start();
            }
        } catch (Exception e) {
            System.out.println("فایل موسیقی زمینه یافت نشد یا فرمت آن پشتیبانی نمی‌شود.");
        }
    }

    // متد توقف دستی موزیک متن
    public static void stopBackgroundMusic() {
        try {
            if (bgMusicClip != null && bgMusicClip.isRunning()) {
                bgMusicClip.stop();
            }
        } catch (Exception e) {
            System.out.println(" خطا در متوقف کردن موسیقی زمینه: " + e.getMessage());
        }
    }

    // ۲. افکت شلیک
    public static void playShotSound() {
        if (!isShotEnabled) return;
        playSoundEffect("sounds/mixkit-short-laser-gun-shot-1670.wav");
    }

    // ۳. افکت برخورد/انفجار
    public static void playCollisionSound() {
        if (!isCollisionEnabled) return;
        playSoundEffect("sounds/mixkit-epic-impact-afar-explosion-2782.wav");
    }

    // ۴. صدای پایان بازی (اصلاح شده به صورت کنترل‌پذیر)
    public static void playGameOverSound() {
        // حواسش به هر دو پرچم دیتابیس (موزیک کلی و وضعیت گیم‌اور) هست
        if (!isGameOverEnabled || !isMusicEnabled) return;

        try {
            if (gameOverMusicClip == null) {
                File soundFile = new File("sounds/Chicken Invaders 2 Remastered OST - Ending Theme.wav");
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(soundFile.getAbsoluteFile());
                gameOverMusicClip = AudioSystem.getClip();
                gameOverMusicClip.open(audioStream);
            }

            if (!gameOverMusicClip.isRunning()) {
                gameOverMusicClip.setFramePosition(0); // ریست فریم به ابتدا
                gameOverMusicClip.start();
            }
        } catch (Exception e) {
            System.out.println("خطا در پخش صدای پایان بازی: " + e.getMessage());
        }
    }

    private static void playSoundEffect(String path) {
        new Thread(() -> {
            try {
                File file = new File(path);
                AudioInputStream stream = AudioSystem.getAudioInputStream(file);
                Clip clip = AudioSystem.getClip();
                clip.open(stream);
                clip.start();
            } catch (Exception e) {
                System.out.println(" خطا در پخش افکت صوتی مسیر: " + path);
            }
        }).start();
    }
}
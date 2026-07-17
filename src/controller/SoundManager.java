package controller;

import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public final class SoundManager {
    private static final String BACKGROUND_MUSIC = "sounds/Chicken Invaders 2 Remastered OST - Main Theme.wav";
    private static final String GAME_OVER_MUSIC = "sounds/Chicken Invaders 2 Remastered OST - Ending Theme.wav";
    private static final String SHOT_SOUND = "sounds/mixkit-short-laser-gun-shot-1670.wav";
    private static final String COLLISION_SOUND = "sounds/mixkit-epic-impact-afar-explosion-2782.wav";

    private static final String ALL_SOUNDS_ON = "1,1,1,1";
    private static final String ALL_SOUNDS_OFF = "0,0,0,0";

    private static boolean isMusicEnabled = true;
    private static boolean isShotEnabled = true;
    private static boolean isCollisionEnabled = true;
    private static boolean isGameOverEnabled = true;

    private static Clip backgroundMusicClip;
    private static Clip gameOverMusicClip;

    private SoundManager() {
    }

    public static void updateSettings(String settings) {
        if (settings == null) {
            return;
        }

        String[] parts = settings.split(",");

        if (parts.length != 4) {
            return;
        }

        isMusicEnabled = "1".equals(parts[0].trim());
        isShotEnabled = "1".equals(parts[1].trim());
        isCollisionEnabled = "1".equals(parts[2].trim());
        isGameOverEnabled = "1".equals(parts[3].trim());

        if (!isMusicEnabled) {
            stopBackgroundMusic();
        }

        if (!isGameOverEnabled) {
            stopGameOverSound();
        }

        if (isMusicEnabled
                && !isClipRunning(backgroundMusicClip)
                && !isClipRunning(gameOverMusicClip)) {
            playBackgroundMusic();
        }
    }

    public static void playBackgroundMusic() {
        if (!isMusicEnabled) {
            return;
        }

        stopGameOverSound();

        try {
            if (backgroundMusicClip == null || !backgroundMusicClip.isOpen()) {
                backgroundMusicClip = loadClip(BACKGROUND_MUSIC);
            }

            if (!backgroundMusicClip.isRunning()) {
                backgroundMusicClip.setFramePosition(0);
                backgroundMusicClip.loop(Clip.LOOP_CONTINUOUSLY);
            }
        } catch (UnsupportedAudioFileException | IOException |
                 LineUnavailableException e) {
            System.err.println("خطا در پخش موسیقی زمینه: " + e.getMessage());
        }
    }

    public static void stopBackgroundMusic() {
        if (backgroundMusicClip != null && backgroundMusicClip.isRunning()) {
            backgroundMusicClip.stop();
        }
    }

    public static void playShotSound() {
        if (isShotEnabled) {
            playSoundEffect(SHOT_SOUND);
        }
    }

    public static void playCollisionSound() {
        if (isCollisionEnabled) {
            playSoundEffect(COLLISION_SOUND);
        }
    }

    public static void playGameOverSound() {
        if (!isGameOverEnabled) {
            return;
        }

        stopBackgroundMusic();

        try {
            if (gameOverMusicClip == null || !gameOverMusicClip.isOpen()) {
                gameOverMusicClip = loadClip(GAME_OVER_MUSIC);
            }

            gameOverMusicClip.stop();
            gameOverMusicClip.setFramePosition(0);
            gameOverMusicClip.start();
        } catch (UnsupportedAudioFileException | IOException |
                 LineUnavailableException e) {
            System.err.println("خطا در پخش صدای پایان بازی: " + e.getMessage());
        }
    }

    private static void stopGameOverSound() {
        if (gameOverMusicClip != null && gameOverMusicClip.isRunning()) {
            gameOverMusicClip.stop();
        }
    }

    private static Clip loadClip(String path) throws UnsupportedAudioFileException, IOException, LineUnavailableException {
        File file = new File(path);

        try (AudioInputStream stream = AudioSystem.getAudioInputStream(file)) {
            Clip clip = AudioSystem.getClip();
            clip.open(stream);
            return clip;
        }
    }

    private static void playSoundEffect(String path) {
        Thread soundThread = new Thread(() -> {
            try {
                Clip clip = loadClip(path);

                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });

                clip.start();
            } catch (UnsupportedAudioFileException | IOException |
                     LineUnavailableException e) {
                System.err.println(
                        "خطا در پخش افکت صوتی " + path + ": " + e.getMessage()
                );
            }
        });

        soundThread.start();
    }

    private static boolean isClipRunning(Clip clip) {
        return clip != null && clip.isRunning();
    }

    private static boolean areAllSoundsEnabled() {
        return isMusicEnabled && isShotEnabled && isCollisionEnabled && isGameOverEnabled;
    }

    public static boolean isMusicEnabled() {
        return isMusicEnabled;
    }

    public static void toggleAllSounds() {
        if (areAllSoundsEnabled())
            updateSettings(ALL_SOUNDS_OFF);
        else
            updateSettings(ALL_SOUNDS_ON);
    }
}
// AudioManager.java
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import java.io.File;

/**
 * Manages all audio operations for the game
 */
public class AudioManager {
    private MediaPlayer currentMusic;
    private MediaPlayer titleMusic;

    /**
     * Plays a sound effect once
     */
    public void playSound(String soundPath) {
        try {
            Media sound = new Media(new File(soundPath).toURI().toString());
            MediaPlayer player = new MediaPlayer(sound);
            player.setVolume(GameConstants.VOLUME);
            player.play();
        } catch (Exception e) {
            System.err.println("Could not play sound: " + soundPath + " - " + e.getMessage());
        }
    }

    /**
     * Plays background music with optional looping
     */
    public void playMusic(String musicPath, boolean loop) {
        try {
            stopMusic();
            Media music = new Media(new File(musicPath).toURI().toString());
            currentMusic = new MediaPlayer(music);
            currentMusic.setVolume(GameConstants.VOLUME);
            if (loop) {
                currentMusic.setCycleCount(MediaPlayer.INDEFINITE);
            }
            currentMusic.play();

            if (musicPath.contains("Title")) {
                titleMusic = currentMusic;
            }
        } catch (Exception e) {
            System.err.println("Could not play music: " + musicPath + " - " + e.getMessage());
        }
    }

    /**
     * Stops current music
     */
    public void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }
}
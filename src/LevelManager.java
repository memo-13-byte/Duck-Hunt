// LevelManager.java
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javafx.scene.layout.Pane;

/**
 * Manages level creation and progression
 */
public class LevelManager {
    private int currentLevel = 1;
    private int ammoLeft = 0;
    private List<Duck> ducks = new ArrayList<>();
    private Random random = new Random();
    private AudioManager audioManager;

    public LevelManager(AudioManager audioManager) {
        this.audioManager = audioManager;
    }

    /**
     * Creates ducks for the current level
     */
    public void createDucks(int duckCount, Pane gamePane) {
        ducks.clear();
        String[] colors = {"black", "blue", "red"};

        for (int i = 0; i < duckCount; i++) {
            String color = colors[random.nextInt(colors.length)];
            double startX = random.nextDouble() * (GameConstants.WINDOW_WIDTH - 60 * GameConstants.SCALE);
            double startY = random.nextDouble() * (GameConstants.WINDOW_HEIGHT - 60 * GameConstants.SCALE);
            double velocityX = (random.nextDouble() - 0.5) * 4 * GameConstants.SCALE;
            double velocityY = (random.nextDouble() - 0.5) * 4 * GameConstants.SCALE;

            Duck duck = new Duck(color, startX, startY, velocityX, velocityY, audioManager);
            ducks.add(duck);
            gamePane.getChildren().add(duck.getImageView());
        }
    }

    /**
     * Updates all ducks
     */
    public void updateDucks() {
        for (Duck duck : ducks) {
            duck.update();
        }
    }

    /**
     * Checks if all ducks are dead
     */
    public boolean allDucksDefeated() {
        return ducks.stream().allMatch(duck -> !duck.isAlive());
    }

    /**
     * Handles shooting at coordinates
     */
    public boolean handleShoot(double x, double y) {
        for (Duck duck : ducks) {
            if (duck.isAlive() && duck.contains(x, y)) {
                duck.shoot();
                return true;
            }
        }
        return false;
    }

    // Getters and Setters
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int level) { this.currentLevel = level; }
    public int getAmmoLeft() { return ammoLeft; }
    public void setAmmoLeft(int ammo) { this.ammoLeft = ammo; }
    public void decreaseAmmo() { this.ammoLeft--; }
    public List<Duck> getDucks() { return ducks; }
}
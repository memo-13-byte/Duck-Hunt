// Duck.java
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import java.io.File;

/**
 * Represents a duck entity in the game
 */
public class Duck {
    private ImageView imageView;
    private double x, y;
    private double velocityX, velocityY;
    private String color;
    private int animationFrame = 1;
    private boolean alive = true;
    private boolean falling = false;
    private Timeline animationTimeline;
    private Timeline fallTimeline;
    private AudioManager audioManager;

    /**
     * Constructor for Duck
     */
    public Duck(String color, double startX, double startY, double velX, double velY, AudioManager audioManager) {
        this.color = color;
        this.x = startX;
        this.y = startY;
        this.velocityX = velX;
        this.velocityY = velY;
        this.audioManager = audioManager;

        initializeImageView();
        startFlyingAnimation();
    }

    private void initializeImageView() {
        imageView = new ImageView();
        imageView.setFitWidth(60 * GameConstants.SCALE);
        imageView.setFitHeight(60 * GameConstants.SCALE);
        updateImage();
    }

    private void startFlyingAnimation() {
        animationTimeline = new Timeline(new KeyFrame(Duration.millis(200), e -> {
            if (alive && !falling) {
                animationFrame = (animationFrame % 3) + 1;
                updateImage();
            }
        }));
        animationTimeline.setCycleCount(Timeline.INDEFINITE);
        animationTimeline.play();
    }

    private void updateImage() {
        try {
            String imagePath = "assets/duck_" + color + "/" + animationFrame + ".png";
            Image image = new Image(new File(imagePath).toURI().toString(),
                    60 * GameConstants.SCALE, 60 * GameConstants.SCALE, false, false);
            imageView.setImage(image);

            // Flip image if moving left
            imageView.setScaleX(velocityX < 0 ? -1 : 1);
            imageView.setX(x);
            imageView.setY(y);
        } catch (Exception e) {
            System.err.println("Could not load duck image: " + color + "/" + animationFrame + " - " + e.getMessage());
            imageView.setImage(null);
        }
    }

    /**
     * Updates duck position and handles boundary reflection
     */
    public void update() {
        if (alive && !falling) {
            x += velocityX;
            y += velocityY;

            // Boundary reflection
            if (x <= 0 || x >= GameConstants.WINDOW_WIDTH - imageView.getFitWidth()) {
                velocityX = -velocityX;
                x = Math.max(0, Math.min(GameConstants.WINDOW_WIDTH - imageView.getFitWidth(), x));
            }
            if (y <= 0 || y >= GameConstants.WINDOW_HEIGHT - imageView.getFitHeight()) {
                velocityY = -velocityY;
                y = Math.max(0, Math.min(GameConstants.WINDOW_HEIGHT - imageView.getFitHeight(), y));
            }

            updateImage();
        }
    }

    /**
     * Handles duck being shot
     */
    public void shoot() {
        if (alive && !falling) {
            alive = false;
            falling = true;
            animationFrame = 7;
            updateImage();

            audioManager.playSound("assets/effects/DuckFalls.mp3");
            startFallingAnimation();
        }
    }

    private void startFallingAnimation() {
        fallTimeline = new Timeline(
                new KeyFrame(Duration.millis(500), e -> {
                    animationFrame = 8;
                    updateImage();
                }),
                new KeyFrame(Duration.millis(1000), e -> {
                    y = GameConstants.WINDOW_HEIGHT - imageView.getFitHeight();
                    updateImage();
                })
        );
        fallTimeline.play();
    }

    /**
     * Checks if point is inside duck bounds
     */
    public boolean contains(double pointX, double pointY) {
        return pointX >= x && pointX <= x + imageView.getFitWidth() &&
                pointY >= y && pointY <= y + imageView.getFitHeight();
    }

    // Getters
    public ImageView getImageView() { return imageView; }
    public boolean isAlive() { return alive; }
}
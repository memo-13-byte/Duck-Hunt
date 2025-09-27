// UIManager.java
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import java.io.File;

/**
 * Manages all UI elements and screens
 */
public class UIManager {
    private Timeline flashingTimeline;
    private Label levelLabel;
    private Label ammoLabel;

    /**
     * Creates title screen
     */
    public StackPane createTitleScreen() {
        StackPane titlePane = new StackPane();

        // Background
        try {
            ImageView background = new ImageView(new Image(new File("assets/welcome/1.png").toURI().toString()));
            background.setFitWidth(GameConstants.WINDOW_WIDTH);
            background.setFitHeight(GameConstants.WINDOW_HEIGHT);
            titlePane.getChildren().add(background);
        } catch (Exception e) {
            System.err.println("Could not load title background: " + e.getMessage());
        }

        // Title text
        Label titleLabel = new Label("DUCK HUNT");
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 48 * GameConstants.SCALE / 3));
        titleLabel.setTextFill(Color.CYAN);
        StackPane.setAlignment(titleLabel, Pos.TOP_CENTER);
        titleLabel.setTranslateY(80 * GameConstants.SCALE / 3);

        // Instructions
        Label instructionsLabel = new Label("PRESS ENTER TO START\nPRESS ESC TO EXIT");
        instructionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16 * GameConstants.SCALE / 3));
        instructionsLabel.setTextFill(Color.ORANGE);
        instructionsLabel.setAlignment(Pos.CENTER);
        StackPane.setAlignment(instructionsLabel, Pos.CENTER);
        instructionsLabel.setTranslateY(50 * GameConstants.SCALE / 3);

        // Flashing animation
        flashingTimeline = new Timeline(
                new KeyFrame(Duration.millis(500), e -> instructionsLabel.setVisible(!instructionsLabel.isVisible()))
        );
        flashingTimeline.setCycleCount(Timeline.INDEFINITE);
        flashingTimeline.play();

        titlePane.getChildren().addAll(titleLabel, instructionsLabel);
        return titlePane;
    }

    /**
     * Creates background selection screen
     */
    public StackPane createBackgroundSelection(int selectedBackground, int selectedCrosshair) {
        StackPane selectionPane = new StackPane();

        try {
            ImageView background = new ImageView(new Image(new File("assets/background/" + selectedBackground + ".png").toURI().toString()));
            background.setFitWidth(GameConstants.WINDOW_WIDTH);
            background.setFitHeight(GameConstants.WINDOW_HEIGHT);
            selectionPane.getChildren().add(background);
        } catch (Exception e) {
            System.err.println("Could not load background: " + e.getMessage());
        }

        // Show crosshair preview
        try {
            ImageView crosshair = new ImageView(new Image(new File("assets/crosshair/" + selectedCrosshair + ".png").toURI().toString()));
            crosshair.setFitWidth(30 * GameConstants.SCALE);
            crosshair.setFitHeight(30 * GameConstants.SCALE);
            StackPane.setAlignment(crosshair, Pos.CENTER);
            selectionPane.getChildren().add(crosshair);
        } catch (Exception e) {
            System.err.println("Could not load crosshair: " + e.getMessage());
        }

        return selectionPane;
    }

    /**
     * Creates game pane with background and foreground
     */
    public Pane createGamePane(int selectedBackground, int currentLevel) {
        Pane gamePane = new Pane();

        // Add background
        try {
            ImageView background = new ImageView(new Image(new File("assets/background/" + selectedBackground + ".png").toURI().toString()));
            background.setFitWidth(GameConstants.WINDOW_WIDTH);
            background.setFitHeight(GameConstants.WINDOW_HEIGHT);
            gamePane.getChildren().add(background);
        } catch (Exception e) {
            System.err.println("Could not load game background: " + e.getMessage());
        }

        return gamePane;
    }

    /**
     * Adds UI labels to game pane
     */
    public void addGameLabels(Pane gamePane, int currentLevel, int ammoLeft) {
        levelLabel = new Label("Level " + currentLevel + "/" + GameConstants.TOTAL_LEVELS);
        levelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24 * GameConstants.SCALE / 3));
        levelLabel.setTextFill(Color.ORANGE);
        levelLabel.setLayoutX(GameConstants.WINDOW_WIDTH / 2 - 50 * GameConstants.SCALE / 3);
        levelLabel.setLayoutY(20 * GameConstants.SCALE / 3);

        ammoLabel = new Label("Ammo Left: " + ammoLeft);
        ammoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20 * GameConstants.SCALE / 3));
        ammoLabel.setTextFill(Color.ORANGE);
        ammoLabel.setLayoutX(GameConstants.WINDOW_WIDTH - 200 * GameConstants.SCALE / 3);
        ammoLabel.setLayoutY(20 * GameConstants.SCALE / 3);

        gamePane.getChildren().addAll(levelLabel, ammoLabel);
    }

    /**
     * Adds foreground to game pane
     */
    public void addForeground(Pane gamePane, int selectedBackground) {
        try {
            ImageView foreground = new ImageView(new Image(new File("assets/foreground/" + selectedBackground + ".png").toURI().toString()));
            foreground.setFitWidth(GameConstants.WINDOW_WIDTH);
            foreground.setFitHeight(GameConstants.WINDOW_HEIGHT);
            gamePane.getChildren().add(foreground);
        } catch (Exception e) {
            System.err.println("Could not load foreground: " + e.getMessage());
        }
    }

    /**
     * Updates ammo label
     */
    public void updateAmmoLabel(int ammoLeft) {
        if (ammoLabel != null) {
            ammoLabel.setText("Ammo Left: " + ammoLeft);
        }
    }

    /**
     * Stops flashing animation
     */
    public void stopFlashing() {
        if (flashingTimeline != null) {
            flashingTimeline.stop();
        }
    }

    /**
     * Shows completion message
     */
    public void showCompletionMessage(Pane pane, String message, Color color) {
        Label completionLabel = new Label(message);
        completionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32 * GameConstants.SCALE / 3));
        completionLabel.setTextFill(color);
        completionLabel.setAlignment(Pos.CENTER);
        completionLabel.setLayoutX(GameConstants.WINDOW_WIDTH / 2 - 200 * GameConstants.SCALE / 3);
        completionLabel.setLayoutY(GameConstants.WINDOW_HEIGHT / 2 - 50 * GameConstants.SCALE / 3);
        pane.getChildren().add(completionLabel);
    }
}
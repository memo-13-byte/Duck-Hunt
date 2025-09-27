import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.ImageCursor;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Duck Hunt Game Implementation using JavaFX
 * A simplified version of the classic Duck Hunt game with multiple levels,
 * different backgrounds, crosshairs, and sound effects.
 *
 * Assignment: BBM 104 Project Assignment 4
 * @author Student
 * @version 1.0
 * Spring 2023
 */
public class DuckHunt extends Application {

    // Global configuration parameters as required by assignment
    public static final double SCALE = 3.0;
    public static final double VOLUME = 0.025;

    // Game constants
    private static final int WINDOW_WIDTH = (int)(256 * SCALE);
    private static final int WINDOW_HEIGHT = (int)(240 * SCALE);
    private static final int TOTAL_LEVELS = 6;

    // Game state variables
    private Stage primaryStage;
    private Scene scene;
    private Pane currentPane;
    private GameState gameState = GameState.TITLE;
    private Timeline gameLoop;

    // Configuration variables - reset on ESC from background selection
    private int selectedBackground = 1;
    private int selectedCrosshair = 1;
    private int currentLevel = 1;
    private int ammoLeft = 0;
    private List<Duck> ducks = new ArrayList<>();
    private Random random = new Random();

    // Media players for sound effects
    private MediaPlayer titleMusic;
    private MediaPlayer currentMusic;

    // UI elements
    private Label levelLabel;
    private Label ammoLabel;
    private Timeline flashingTimeline;
    private boolean introSoundNeeded = true; // Track if intro sound should play

    /**
     * Game state enumeration for managing different screens
     */
    enum GameState {
        TITLE, BACKGROUND_SELECTION, PLAYING, LEVEL_COMPLETED, GAME_OVER, GAME_COMPLETED
    }

    /**
     * Duck class representing individual duck entities with proper animation
     * and movement mechanics as specified in requirements
     */
    class Duck {
        private ImageView imageView;
        private double x, y;
        private double velocityX, velocityY;
        private String color;
        private int animationFrame = 1;
        private boolean alive = true;
        private boolean falling = false;
        private Timeline animationTimeline;
        private Timeline fallTimeline;
        private boolean movingLeft = false;
        private boolean movingUp = true; // Varsayılan olarak yukarı doğru

        /**
         * Constructor for Duck with proper initialization
         * @param color Duck color (black, blue, red)
         * @param startX Starting X position
         * @param startY Starting Y position
         * @param velX X velocity
         * @param velY Y velocity
         */
        public Duck(String color, double startX, double startY, double velX, double velY) {
            this.color = color;
            this.x = startX;
            this.y = startY;
            this.velocityX = velX;
            this.velocityY = velY;
            this.movingLeft = velX < 0;
            this.movingUp = velY < 0; // Negatif velocityY yukarı doğru hareketi gösterir

            imageView = new ImageView();
            imageView.setFitWidth(60 * SCALE);
            imageView.setFitHeight(60 * SCALE);
            updateImage();

            // Animation timeline for flying with 3 frames cycling
            animationTimeline = new Timeline(new KeyFrame(Duration.millis(200), e -> {
                if (alive && !falling) {
                    animationFrame = (animationFrame % 3) + 1;
                    updateImage();
                }
            }));
            animationTimeline.setCycleCount(Timeline.INDEFINITE);
            animationTimeline.play();
        }

        /**
         * Updates the duck's image based on current state and direction
         * Handles proper flipping for left/right movement
         */
        /**
         * Updates the duck's image based on current state and direction
         * Handles proper flipping for left/right movement
         */
        private void updateImage() {
            try {
                String imagePath;
                if (falling) {
                    // Falling animation uses frames 7 and 8
                    imagePath = "assets/duck_" + color + "/" + (animationFrame) + ".png";
                } else {
                    // Check if duck is moving horizontally (no vertical movement)
                    if (Math.abs(velocityY) < 0.1) {
                        // Horizontal movement uses frames 4, 5, 6
                        imagePath = "assets/duck_" + color + "/" + (animationFrame + 3) + ".png";
                    } else {
                        // Diagonal movement uses frames 1, 2, 3
                        imagePath = "assets/duck_" + color + "/" + animationFrame + ".png";
                    }
                }

                Image image = new Image(new File(imagePath).toURI().toString(),
                        60 * SCALE, 60 * SCALE, false, false);
                imageView.setImage(image);

                // Flip image horizontally if moving left
                if (movingLeft) {
                    imageView.setScaleX(-1);
                } else {
                    imageView.setScaleX(1);
                }

                // Flip image vertically if moving down
                // Sadece diagonal hareket ediyorsa dikey çevirme uygula
                if (Math.abs(velocityY) >= 0.1) {
                    if (!movingUp) {
                        imageView.setScaleY(-1); // Aşağı doğru hareket ediyorsa dikey çevir
                    } else {
                        imageView.setScaleY(1);  // Yukarı doğru hareket ediyorsa normal
                    }
                } else {
                    imageView.setScaleY(1); // Yatay hareket ediyorsa dikey çevirmeyi sıfırla
                }

                imageView.setX(x);
                imageView.setY(y);
            } catch (Exception e) {
                System.err.println("Could not load duck image: " + color + "/" + animationFrame + " - " + e.getMessage());
                imageView.setImage(null);
            }
        }

        /**
         * Updates duck position and handles boundary reflection
         * Reflects off window edges as specified in requirements
         * With improved rotation logic for diagonal ducks
         */
        public void update() {
            if (alive && !falling) {
                x += velocityX;
                y += velocityY;

                // Boundary reflection from window edges with improved rotation logic
                if (x <= 0 || x >= WINDOW_WIDTH - imageView.getFitWidth()) {
                    velocityX = -velocityX;
                    movingLeft = velocityX < 0;
                    x = Math.max(0, Math.min(WINDOW_WIDTH - imageView.getFitWidth(), x));
                }

                if (y <= 0 || y >= WINDOW_HEIGHT - imageView.getFitHeight()) {
                    velocityY = -velocityY;
                    movingUp = velocityY < 0; // Negatif Y değeri yukarı doğru hareketi gösterir
                    y = Math.max(0, Math.min(WINDOW_HEIGHT - imageView.getFitHeight(), y));
                }

                updateImage();
            }
        }

        /**
         * Handles duck being shot with proper falling animation
         * Plays falling sound and animates from frame 7 to 8
         */
        public void shoot() {
            if (alive && !falling) {
                alive = false;
                falling = true;
                animationFrame = 7;
                updateImage();

                // Play duck falls sound as required
                playSound("assets/effects/DuckFalls.mp3");

                // Start falling animation: frame 7 -> frame 8 -> hit ground
                fallTimeline = new Timeline(
                        new KeyFrame(Duration.millis(500), e -> {
                            animationFrame = 8;
                            updateImage();
                        }),
                        new KeyFrame(Duration.millis(1000), e -> {
                            // Duck hits ground
                            y = WINDOW_HEIGHT - imageView.getFitHeight();
                            updateImage();
                        })
                );
                fallTimeline.play();
            }
        }

        /**
         * Checks if point is inside duck bounds for hit detection
         * @param pointX Mouse X coordinate
         * @param pointY Mouse Y coordinate
         * @return true if point is inside duck bounds
         */
        public boolean contains(double pointX, double pointY) {
            return pointX >= x && pointX <= x + imageView.getFitWidth() &&
                    pointY >= y && pointY <= y + imageView.getFitHeight();
        }

        public ImageView getImageView() { return imageView; }
        public boolean isAlive() { return alive; }
    }

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        primaryStage.setTitle("HUBBM Duck Hunt"); // Exact title as required

        // Set favicon as specified in requirements
        try {
            primaryStage.getIcons().add(new Image(new File("assets/favicon/1.png").toURI().toString()));
        } catch (Exception e) {
            System.err.println("Could not load favicon: " + e.getMessage());
        }

        primaryStage.setWidth(WINDOW_WIDTH);
        primaryStage.setHeight(WINDOW_HEIGHT);
        primaryStage.setResizable(false);

        showTitleScreen();
        primaryStage.show();
    }

    /**
     * Displays the title screen with proper flashing text and title music
     * Plays Title.mp3 in loop as required
     */
    private void showTitleScreen() {
        gameState = GameState.TITLE;
        introSoundNeeded = true; // Reset intro sound flag

        StackPane titlePane = new StackPane();

        // Background image
        try {
            ImageView background = new ImageView(new Image(new File("assets/welcome/1.png").toURI().toString()));
            background.setFitWidth(WINDOW_WIDTH);
            background.setFitHeight(WINDOW_HEIGHT);
            titlePane.getChildren().add(background);
        } catch (Exception e) {
            System.err.println("Could not load title background: " + e.getMessage());
        }



        // Instructions as specified: "PRESS ENTER TO PLAY" and "PRESS ESC TO EXIT"
        Label instructionsLabel = new Label("PRESS ENTER TO PLAY\nPRESS ESC TO EXIT");
        instructionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16 * SCALE / 3));
        instructionsLabel.setTextFill(Color.ORANGE);
        instructionsLabel.setAlignment(Pos.CENTER);
        StackPane.setAlignment(instructionsLabel, Pos.CENTER);
        instructionsLabel.setTranslateY(50 * SCALE / 3);

        // Flashing animation for instructions
        flashingTimeline = new Timeline(
                new KeyFrame(Duration.millis(500), e -> instructionsLabel.setVisible(!instructionsLabel.isVisible()))
        );
        flashingTimeline.setCycleCount(Timeline.INDEFINITE);
        flashingTimeline.play();

        titlePane.getChildren().addAll(instructionsLabel);

        scene = new Scene(titlePane, WINDOW_WIDTH, WINDOW_HEIGHT);
        setupKeyHandlers();
        primaryStage.setScene(scene);

        // Return cursor to default when returning to title screen
        scene.setCursor(javafx.scene.Cursor.DEFAULT);

        // Play title music in loop as required
        playMusic("assets/effects/Title.mp3", true);
    }

    /**
     * Displays the background selection screen with navigation options
     * Keeps title music playing and resets options if coming from title screen
     */
    private void showBackgroundSelection() {
        gameState = GameState.BACKGROUND_SELECTION;

        if (flashingTimeline != null) {
            flashingTimeline.stop();
        }

        // Reset selections when coming from title screen as required
        selectedBackground = 1;
        selectedCrosshair = 1;

        StackPane selectionPane = new StackPane();
        updateBackgroundPreview(selectionPane);

        scene = new Scene(selectionPane, WINDOW_WIDTH, WINDOW_HEIGHT);
        setupKeyHandlers();
        primaryStage.setScene(scene);

        currentPane = selectionPane;
    }

    /**
     * Updates the background preview with current selections
     * Shows selected background and crosshair with instructions
     */
    private void updateBackgroundPreview(StackPane pane) {
        pane.getChildren().clear();

        // Show selected background
        try {
            ImageView background = new ImageView(new Image(new File("assets/background/" + selectedBackground + ".png").toURI().toString()));
            background.setFitWidth(WINDOW_WIDTH);
            background.setFitHeight(WINDOW_HEIGHT);
            pane.getChildren().add(background);
        } catch (Exception e) {
            System.err.println("Could not load background: " + e.getMessage());
        }

        // Show crosshair preview
        try {
            ImageView crosshair = new ImageView(new Image(new File("assets/crosshair/" + selectedCrosshair + ".png").toURI().toString()));
            crosshair.setFitWidth(30 * SCALE);
            crosshair.setFitHeight(30 * SCALE);
            StackPane.setAlignment(crosshair, Pos.CENTER);
            pane.getChildren().add(crosshair);
        } catch (Exception e) {
            System.err.println("Could not load crosshair: " + e.getMessage());
        }

        // Instructions as specified in requirements
        Label instructionsLabel = new Label("USE ARROW KEYS TO NAVIGATE\nPRESS ENTER TO START\nPRESS ESC TO EXIT");
        instructionsLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16 * SCALE / 3));
        instructionsLabel.setTextFill(Color.WHITE);
        instructionsLabel.setAlignment(Pos.CENTER);
        StackPane.setAlignment(instructionsLabel, Pos.BOTTOM_CENTER);
        instructionsLabel.setTranslateY(-50 * SCALE / 3);
        pane.getChildren().add(instructionsLabel);
    }

    /**
     * Starts the game with intro sound if needed
     * Intro sound only plays when starting from background selection
     */
    private void startGame() {
        stopMusic();

        if (introSoundNeeded) {
            // Play intro sound and wait for it to finish
            playSound("assets/effects/Intro.mp3");
            Timeline introDelay = new Timeline(new KeyFrame(Duration.millis(2000), e -> startLevel()));
            introDelay.play();
        } else {
            // Start immediately without intro sound
            startLevel();
        }
    }

    /**
     * Starts a new level with proper setup
     * Creates ducks, UI elements, and game environment
     * Always creates a fresh scene for each level
     */
    private void startLevel() {
        System.out.println("=== startLevel() called for level " + currentLevel + " ===");
        gameState = GameState.PLAYING;

        // Calculate level parameters: 3x ammo per duck as required
        int duckCount = Math.min(currentLevel, 3);
        ammoLeft = duckCount * 3;

        System.out.println("Level " + currentLevel + " starting with " + duckCount + " ducks and " + ammoLeft + " ammo");

        // Clear any existing ducks from previous level
        ducks.clear();
        System.out.println("Ducks list cleared, size now: " + ducks.size());

        // Create fresh game pane for each level
        Pane gamePane = new Pane();
        System.out.println("Fresh game pane created");

        // Add background using the SAME selected background for all levels
        try {
            ImageView background = new ImageView(new Image(new File("assets/background/" + selectedBackground + ".png").toURI().toString()));
            background.setFitWidth(WINDOW_WIDTH);
            background.setFitHeight(WINDOW_HEIGHT);
            gamePane.getChildren().add(background);
            System.out.println("Background " + selectedBackground + " added");
        } catch (Exception e) {
            System.err.println("Could not load game background: " + e.getMessage());
        }

        // Create ducks (between background and foreground as required)
        System.out.println("About to create " + duckCount + " ducks");
        createDucks(duckCount, gamePane);
        System.out.println("Duck creation completed, ducks list size: " + ducks.size());

        // Add foreground using the SAME selected foreground for all levels
        try {
            ImageView foreground = new ImageView(new Image(new File("assets/foreground/" + selectedBackground + ".png").toURI().toString()));
            foreground.setFitWidth(WINDOW_WIDTH);
            foreground.setFitHeight(WINDOW_HEIGHT);
            gamePane.getChildren().add(foreground);
            System.out.println("Foreground " + selectedBackground + " added");
        } catch (Exception e) {
            System.err.println("Could not load foreground: " + e.getMessage());
        }

        // Add UI labels as specified: "Level X/Y" centered top, "Ammo Left: Z" right corner
        levelLabel = new Label("Level " + currentLevel + "/" + TOTAL_LEVELS);
        levelLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24 * SCALE / 3));
        levelLabel.setTextFill(Color.ORANGE);
        levelLabel.setLayoutX(WINDOW_WIDTH / 2 - 50 * SCALE / 3);
        levelLabel.setLayoutY(20 * SCALE / 3);

        ammoLabel = new Label("Ammo Left: " + ammoLeft);
        ammoLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20 * SCALE / 3));
        ammoLabel.setTextFill(Color.ORANGE);
        ammoLabel.setLayoutX(WINDOW_WIDTH - 200 * SCALE / 3);
        ammoLabel.setLayoutY(20 * SCALE / 3);

        gamePane.getChildren().addAll(levelLabel, ammoLabel);
        System.out.println("UI labels added");

        // Create a completely NEW scene for each level
        scene = new Scene(gamePane, WINDOW_WIDTH, WINDOW_HEIGHT);
        setupGameHandlers();
        setupKeyHandlers(); // CRITICAL: Setup key handlers for the new scene
        setupCustomCursor();
        primaryStage.setScene(scene);
        System.out.println("New scene created and set");

        currentPane = gamePane;

        // Start duck animation loop
        startGameLoop();
        System.out.println("=== startLevel() completed ===");
    }

    /**
     * Creates ducks for the current level with proper movement patterns
     * Ensures 6 different movement directions as required
     */
    private void createDucks(int duckCount, Pane gamePane) {
        ducks.clear();
        String[] colors = {"black", "blue", "red"};

        for (int i = 0; i < duckCount; i++) {
            String color = colors[random.nextInt(colors.length)];
            double startX = random.nextDouble() * (WINDOW_WIDTH - 60 * SCALE);
            double startY = random.nextDouble() * (WINDOW_HEIGHT - 60 * SCALE);

            // Ensure 6 different directions: left, right, diagonal corners
            double velocityX, velocityY;
            switch (i % 6) {
                case 0: // Left
                    velocityX = -3 * SCALE;
                    velocityY = 0;
                    break;
                case 1: // Right
                    velocityX = 3 * SCALE;
                    velocityY = 0;
                    break;
                case 2: // Top-left to bottom-right
                    velocityX = 2 * SCALE;
                    velocityY = 2 * SCALE;
                    break;
                case 3: // Top-right to bottom-left
                    velocityX = -2 * SCALE;
                    velocityY = 2 * SCALE;
                    break;
                case 4: // Bottom-left to top-right
                    velocityX = 2 * SCALE;
                    velocityY = -2 * SCALE;
                    break;
                case 5: // Bottom-right to top-left
                    velocityX = -2 * SCALE;
                    velocityY = -2 * SCALE;
                    break;
                default:
                    velocityX = (random.nextDouble() - 0.5) * 4 * SCALE;
                    velocityY = (random.nextDouble() - 0.5) * 4 * SCALE;
            }

            Duck duck = new Duck(color, startX, startY, velocityX, velocityY);
            ducks.add(duck);
            gamePane.getChildren().add(duck.getImageView());
        }
    }

    /**
     * Starts the main game loop for duck updates and game state checking
     */
    private void startGameLoop() {
        if (gameLoop != null) {
            gameLoop.stop();
        }

        gameLoop = new Timeline(new KeyFrame(Duration.millis(16), e -> updateGame()));
        gameLoop.setCycleCount(Timeline.INDEFINITE);
        gameLoop.play();
    }

    /**
     * Updates game state each frame
     * Checks for level completion and game over conditions
     */
    private void updateGame() {
        if (gameState == GameState.PLAYING) {
            for (Duck duck : ducks) {
                duck.update();
            }

            // Check if all ducks are dead
            boolean allDead = ducks.stream().allMatch(duck -> !duck.isAlive());
            if (allDead) {
                if (gameLoop != null) gameLoop.stop();
                levelCompleted();
            } else if (ammoLeft <= 0) {
                if (gameLoop != null) gameLoop.stop();
                gameOver();
            }
        }
    }

    /**
     * Handles level completion with proper sound and progression
     */
    private void levelCompleted() {
        gameState = GameState.LEVEL_COMPLETED;

        if (currentLevel >= TOTAL_LEVELS) {
            gameCompleted();
            return;
        }

        playSound("assets/effects/LevelCompleted.mp3");

        // Show completion message as specified
        Label completionLabel = new Label("YOU WIN!\nPress ENTER to play next level");
        completionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32 * SCALE / 3));
        completionLabel.setTextFill(Color.YELLOW);
        completionLabel.setAlignment(Pos.CENTER);
        completionLabel.setLayoutX(WINDOW_WIDTH / 2 - 200 * SCALE / 3);
        completionLabel.setLayoutY(WINDOW_HEIGHT / 2 - 50 * SCALE / 3);

        // Add flashing effect to second line as required
        Timeline flashingText = new Timeline(
                new KeyFrame(Duration.millis(500), e -> {
                    String[] lines = completionLabel.getText().split("\n");
                    if (lines.length > 1) {
                        if (lines[1].isEmpty()) {
                            completionLabel.setText(lines[0] + "\nPress ENTER to play next level");
                        } else {
                            completionLabel.setText(lines[0] + "\n");
                        }
                    }
                })
        );
        flashingText.setCycleCount(Timeline.INDEFINITE);
        flashingText.play();

        currentPane.getChildren().add(completionLabel);
    }

    /**
     * Handles game completion (finishing last level)
     */
    private void gameCompleted() {
        gameState = GameState.GAME_COMPLETED;

        playSound("assets/effects/GameCompleted.mp3");

        // Show completion message as specified
        Label completionLabel = new Label("You have completed the game!\nPress ENTER to play again\nPress ESC to exit");
        completionLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24 * SCALE / 3));
        completionLabel.setTextFill(Color.YELLOW);
        completionLabel.setAlignment(Pos.CENTER);
        completionLabel.setLayoutX(WINDOW_WIDTH / 2 - 200 * SCALE / 3);
        completionLabel.setLayoutY(WINDOW_HEIGHT / 2 - 75 * SCALE / 3);

        // Add flashing effect to second and third lines as required
        Timeline flashingText = new Timeline(
                new KeyFrame(Duration.millis(500), e -> {
                    String text = completionLabel.getText();
                    if (text.contains("Press ENTER")) {
                        completionLabel.setText("You have completed the game!\n\n");
                    } else {
                        completionLabel.setText("You have completed the game!\nPress ENTER to play again\nPress ESC to exit");
                    }
                })
        );
        flashingText.setCycleCount(Timeline.INDEFINITE);
        flashingText.play();

        currentPane.getChildren().add(completionLabel);
    }

    /**
     * Handles game over scenario
     */
    private void gameOver() {
        gameState = GameState.GAME_OVER;

        playSound("assets/effects/GameOver.mp3");

        // Show game over message as specified
        Label gameOverLabel = new Label("GAME OVER!\nPress ENTER to play again\nPress ESC to exit");
        gameOverLabel.setFont(Font.font("Arial", FontWeight.BOLD, 32 * SCALE / 3));
        gameOverLabel.setTextFill(Color.RED);
        gameOverLabel.setAlignment(Pos.CENTER);
        gameOverLabel.setLayoutX(WINDOW_WIDTH / 2 - 200 * SCALE / 3);
        gameOverLabel.setLayoutY(WINDOW_HEIGHT / 2 - 75 * SCALE / 3);

        // Add flashing effect to second and third lines as required
        Timeline flashingText = new Timeline(
                new KeyFrame(Duration.millis(500), e -> {
                    String text = gameOverLabel.getText();
                    if (text.contains("Press ENTER")) {
                        gameOverLabel.setText("GAME OVER!\n\n");
                    } else {
                        gameOverLabel.setText("GAME OVER!\nPress ENTER to play again\nPress ESC to exit");
                    }
                })
        );
        flashingText.setCycleCount(Timeline.INDEFINITE);
        flashingText.play();

        currentPane.getChildren().add(gameOverLabel);
    }

    /**
     * Sets up custom cursor for crosshair during gameplay
     * Returns to default cursor when outside window or in title screen
     */
    private void setupCustomCursor() {
        try {
            Image cursorImage = new Image(new File("assets/crosshair/" + selectedCrosshair + ".png").toURI().toString(), 32, 32, true, true);
            ImageCursor imageCursor = new ImageCursor(cursorImage, cursorImage.getWidth()/2, cursorImage.getHeight()/2);
            scene.setCursor(imageCursor);
        } catch (Exception e) {
            System.err.println("Could not load cursor: " + e.getMessage());
            scene.setCursor(javafx.scene.Cursor.CROSSHAIR);
        }
    }

    /**
     * Sets up keyboard event handlers for all game states
     */
    private void setupKeyHandlers() {
        scene.setOnKeyPressed(e -> {
            switch (gameState) {
                case TITLE:
                    if (e.getCode() == KeyCode.ENTER) {
                        showBackgroundSelection();
                    } else if (e.getCode() == KeyCode.ESCAPE) {
                        primaryStage.close();
                    }
                    break;

                case BACKGROUND_SELECTION:
                    if (e.getCode() == KeyCode.LEFT) {
                        selectedBackground = selectedBackground > 1 ? selectedBackground - 1 : 6;
                        updateBackgroundPreview((StackPane) currentPane);
                    } else if (e.getCode() == KeyCode.RIGHT) {
                        selectedBackground = selectedBackground < 6 ? selectedBackground + 1 : 1;
                        updateBackgroundPreview((StackPane) currentPane);
                    } else if (e.getCode() == KeyCode.UP) {
                        selectedCrosshair = selectedCrosshair > 1 ? selectedCrosshair - 1 : 7;
                        updateBackgroundPreview((StackPane) currentPane);
                    } else if (e.getCode() == KeyCode.DOWN) {
                        selectedCrosshair = selectedCrosshair < 7 ? selectedCrosshair + 1 : 1;
                        updateBackgroundPreview((StackPane) currentPane);
                    } else if (e.getCode() == KeyCode.ENTER) {
                        startGame();
                    } else if (e.getCode() == KeyCode.ESCAPE) {
                        showTitleScreen();
                    }
                    break;

                case LEVEL_COMPLETED:
                    System.out.println("LEVEL_COMPLETED: Key pressed = " + e.getCode()); // Debug
                    if (e.getCode() == KeyCode.ENTER) {
                        System.out.println("ENTER pressed, moving to next level"); // Debug
                        currentLevel++;
                        introSoundNeeded = false; // No intro sound for level progression
                        startLevel();
                    }
                    break;

                case GAME_OVER:
                case GAME_COMPLETED:
                    if (e.getCode() == KeyCode.ENTER) {
                        currentLevel = 1;
                        introSoundNeeded = false; // No intro sound for restart
                        startLevel();
                    } else if (e.getCode() == KeyCode.ESCAPE) {
                        showTitleScreen();
                    }
                    break;
            }
        });
    }

    /**
     * Sets up game-specific event handlers for shooting
     */
    private void setupGameHandlers() {
        scene.setOnMouseClicked(e -> {
            if (gameState == GameState.PLAYING && ammoLeft > 0) {
                handleShoot(e);
            }
        });
    }

    /**
     * Handles shooting mechanics with proper sound effects
     * Can hit multiple ducks with one shot as specified
     * @param e Mouse event containing click coordinates
     */
    private void handleShoot(MouseEvent e) {
        // Play gunshot sound regardless of hit as required
        playSound("assets/effects/Gunshot.mp3");
        ammoLeft--;
        ammoLabel.setText("Ammo Left: " + ammoLeft);

        // Check if any duck was hit (can hit multiple ducks as specified)
        for (Duck duck : ducks) {
            if (duck.isAlive() && duck.contains(e.getX(), e.getY())) {
                duck.shoot();
            }
        }
    }

    /**
     * Plays a sound effect with proper volume control
     * @param soundPath Path to the sound file
     */
    private void playSound(String soundPath) {
        try {
            Media sound = new Media(new File(soundPath).toURI().toString());
            MediaPlayer player = new MediaPlayer(sound);
            player.setVolume(VOLUME);
            player.play();
        } catch (Exception e) {
            System.err.println("Could not play sound: " + soundPath + " - " + e.getMessage());
        }
    }

    /**
     * Plays background music with loop option and proper volume control
     * @param musicPath Path to the music file
     * @param loop Whether to loop the music
     */
    private void playMusic(String musicPath, boolean loop) {
        try {
            stopMusic();
            Media music = new Media(new File(musicPath).toURI().toString());
            currentMusic = new MediaPlayer(music);
            currentMusic.setVolume(VOLUME);
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
     * Stops current music playback
     */
    private void stopMusic() {
        if (currentMusic != null) {
            currentMusic.stop();
            currentMusic = null;
        }
    }

    /**
     * Main method to launch the Duck Hunt application
     * @param args Command line arguments (not used)
     */
    public static void main(String[] args) {
        launch(args);
    }
}
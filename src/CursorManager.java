// CursorManager.java
import javafx.scene.ImageCursor;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import java.io.File;

/**
 * Manages cursor operations
 */
public class CursorManager {

    /**
     * Sets up custom crosshair cursor
     */
    public void setupCustomCursor(Scene scene, int selectedCrosshair) {
        try {
            Image cursorImage = new Image(new File("assets/crosshair/" + selectedCrosshair + ".png").toURI().toString());
            scene.setCursor(new ImageCursor(cursorImage));
        } catch (Exception e) {
            System.err.println("Could not load cursor: " + e.getMessage());
        }
    }
}
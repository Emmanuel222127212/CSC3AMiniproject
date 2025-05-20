package maze_gui;

import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.function.Consumer;

public class InformationPanelWrapper {
    private InformationPanel panel;
    private ImageView imageView;

    public InformationPanelWrapper(InformationPanel panel, ImageView imageView) {
        this.panel = panel;
        this.imageView = imageView;
    }

    public void SelectImage(VBox vBox, Consumer<String> pathCallback) {
        panel.SelectImage(vBox);
        // Use FileChooser to select image and set it in ImageView
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"));
        File file = fileChooser.showOpenDialog(null);
        if (file != null) {
            pathCallback.accept(file.getAbsolutePath());
            Image fxImage = new Image(file.toURI().toString());
            imageView.setImage(fxImage);
        }
    }

    public void setAttemptButton(Scene scene) {
        panel.setAttemptButton(scene);
    }

    public void change() {
        panel.change();
    }
}
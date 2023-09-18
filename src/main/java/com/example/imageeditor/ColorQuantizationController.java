package com.example.imageeditor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ColorQuantizationController {

    @FXML
    public ChoiceBox<String> choiceBox;
    private MainController ctrl;

    public void quantize(ActionEvent event) {
        var quantizer = this.choiceBox.getValue().equalsIgnoreCase("kmeans")
                ? new KMeans(this.ctrl.getImageAsBufferedImage())
                : this.choiceBox.getValue().equalsIgnoreCase("octree")
                ? new OctreeColorQuantizer(this.ctrl.getImageAsBufferedImage())
                : new UniformQuantizer(this.ctrl.getImageAsBufferedImage());

        // convert BufferedImage to InputStream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        this.ctrl.setImageAsBuffered(quantizer.getImage());
        this.ctrl.imageView.setImage(this.ctrl.getImage());

        var box = new VBox(
                new Label("Color Quantization")
        );
        this.ctrl.infoBox.getChildren().add(box);
        this.ctrl.displayColorPalette(quantizer.getPalette(), box);

        ((Stage)((Node)event.getTarget()).getScene().getWindow()).close();
    }

    public void setMainController(MainController ctrl) {
        this.ctrl = ctrl;
    }
}

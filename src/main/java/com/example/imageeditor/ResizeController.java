package com.example.imageeditor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

public class ResizeController {
    @FXML
    public TextField newWidth;
    @FXML
    public TextField newHeight;
    private MainController ctrl;

    public void resize(ActionEvent event) {
        try {
            int width = Integer.parseInt(newWidth.getText());
            int height = Integer.parseInt(newWidth.getText());

            var im = this.ctrl.getImageAsBufferedImage();
            java.awt.Image tmp = im.getScaledInstance(width, height, java.awt.Image.SCALE_SMOOTH);
            BufferedImage newimg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

            Graphics2D g2d = newimg.createGraphics();
            g2d.drawImage(tmp, 0, 0, null);
            g2d.dispose();

            System.out.println("Resizing image..");

            this.ctrl.setImageAsBuffered(newimg);
            this.ctrl.imageView.setImage(this.ctrl.getImage());
            /* update resolution */

            ((Stage) ((Node) event.getTarget()).getScene().getWindow()).close();
        } catch (Exception e) {
            System.err.println("ResizeController::resize: Something wrong happened.");
        }
    }

    public void setMainController(MainController ctrl) {
        this.ctrl = ctrl;
    }
}

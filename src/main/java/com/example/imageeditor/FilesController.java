package com.example.imageeditor;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;

import java.awt.event.ActionEvent;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class FilesController {
    @FXML
    public ImageView imagePreview;
    @FXML
    public ListView<Label> imagesListView;

    public void setImages(List<File> images) {
        var btns = images.stream()
                .map(File::getAbsolutePath)
                .map(Label::new)
                .toList();

        imagesListView.setStyle("-fx-background-color: #444;");
        imagesListView.getItems().addAll(btns);
        imagesListView.setOnMouseClicked(this::selectImage);
    }

    public void selectImage(MouseEvent event) {
        try {
            if (event.getClickCount() == 1) {
                Label selectedItem = imagesListView.getSelectionModel().getSelectedItem();
                File file = new File(selectedItem.getText());
                this.imagePreview.setImage(new Image(new DataInputStream(new FileInputStream((file)))));
            }
        } catch (IOException e) {
            System.out.println("Image file not found!");
            e.printStackTrace();
        }
    }

}

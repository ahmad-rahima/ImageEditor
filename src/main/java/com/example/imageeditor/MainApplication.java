package com.example.imageeditor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());

        stage.heightProperty().addListener((value, oldHeight, newHeight) -> {
            ImageView imageView = (ImageView) scene.lookup("#imageView");
            imageView.setFitHeight(newHeight.doubleValue() * .75);
        });

        stage.widthProperty().addListener((value, oldWidth, newWidth) -> {
            ImageView imageView = (ImageView) scene.lookup("#imageView");
            imageView.setFitWidth(newWidth.doubleValue() * .75);
        });

        stage.setTitle("Image Editor");
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }
}
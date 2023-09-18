package com.example.imageeditor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableView;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.io.*;
import java.net.URL;
import java.nio.Buffer;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;


public class MainController {
    @FXML
    public ImageView imageView;
    @FXML
    public Rectangle imagePlaceHolder;
    @FXML
    public HBox filtersBar;
    @FXML
    public VBox infoBox;

    @FXML
    public StackPane mainPane;
    @FXML
    public AnchorPane mainAnchorPane;

    static final private String homeDir = System.getProperty("user.home");
    private boolean filtersBarVisibility = false;
    private File imFile;
    private BufferedImage im;
    private final Stack<BufferedImage> images = new Stack<>();

    private Rectangle selectionRect;
    private double startX, startY, endX, endY;


    private void updateSelectionRect() {
        double x = Math.min(startX, endX);
        double y = Math.min(startY, endY);
        double width = Math.abs(endX - startX);
        double height = Math.abs(endY - startY);
        selectionRect.setX(x);
        selectionRect.setY(y);
        selectionRect.setWidth(width);
        selectionRect.setHeight(height);
    }


    public void initialize() {
        selectionRect = new Rectangle();
        selectionRect.setStroke(javafx.scene.paint.Color.YELLOW);
        selectionRect.setFill(javafx.scene.paint.Color.TRANSPARENT);

        mainAnchorPane.setOnMousePressed(e -> {
            if (!this.cropMode) return;

            this.selectionRect.setVisible(true);
            startX = e.getX();
            startY = e.getY();
            endX = startX;
            endY = startY;
            updateSelectionRect();
        });

        mainAnchorPane.setOnMouseDragged(e -> {
            if (!this.cropMode) return;

            endX = e.getX();
            endY = e.getY();
            updateSelectionRect();
        });

        mainAnchorPane.setOnMouseReleased(e -> {
            if (!this.cropMode) return;

            endX = e.getX();
            endY = e.getY();
            updateSelectionRect();
            this.selectionRect.setVisible(false);
            cropImage();
        });

        mainAnchorPane.getChildren().add(selectionRect);
    }

    public void resizeImage() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("resize-view.fxml"));
            Scene scene = new Scene(loader.load());
            ResizeController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("MainController: resizeImage: Error");
            e.printStackTrace();
        }
    }

    Point2D[] getRectLocalToImageView(double x0, double y0, double x1, double y1) {
        double startX = Math.min(x0, x1);
        double startY = Math.min(y0, y1);

        Point2D mousePoint = new Point2D(startX, startY);
        Point2D nodePoint = imageView.sceneToLocal(mousePoint);
        x0 = nodePoint.getX();
        y0 = nodePoint.getY();
        if (x0 < 0) x0 = 0;
        if (y0 < 0) y0 = 0;

        double width = Math.abs(x1 - x0);
        double height = Math.abs(y1 - y0);

        mousePoint = new Point2D(x0 + width, y0 + height);
        nodePoint = imageView.sceneToLocal(mousePoint);
        x1 = nodePoint.getX();
        y1 = nodePoint.getY();
        if (x1 >  imageView.getBoundsInLocal().getMaxX()) x1 = imageView.getBoundsInLocal().getMaxX();
        if (y1 >  imageView.getBoundsInLocal().getMaxY()) y1 = imageView.getBoundsInLocal().getMaxY();


        return new Point2D[] {new Point2D(x0, y0), new Point2D(x1, y1)};
    }

    private Point2D[] getBoundriesScaled(Point2D p0, Point2D p1) {
        double width = this.imageView.getBoundsInLocal().getWidth();
        double height = this.imageView.getBoundsInLocal().getHeight();

        return new Point2D[] {
                new Point2D(p0.getX() / width, p0.getY() / height),
                new Point2D(p1.getX() / width, p1.getY() / height)
        };
    }

    private BufferedImage getImageCroppedToScaledRect(Point2D[] boundries) {
        BufferedImage oldimg = getImageAsBufferedImage();
        int width = oldimg.getWidth();
        int height = oldimg.getHeight();

        int x = (int)(boundries[0].getX() * width);
        int y = (int)(boundries[0].getY() * height);
        int newWidth = (int)(boundries[1].getX() * width);
        int newHeight = (int)(boundries[1].getY() * height) - y;

        return oldimg.getSubimage(x, y, newWidth, newHeight);
    }

    private void cropImage() {
        Image image = imageView.getImage();
        Point2D[] boundries = getRectLocalToImageView(startX, startY, endX, endY);
        Point2D[] boundriesScaled = getBoundriesScaled(boundries[0], boundries[1]);
        BufferedImage newImg = getImageCroppedToScaledRect(boundriesScaled);
        setImageAsBuffered(newImg);
        this.imageView.setImage(this.getImage());
    }

    public File getImageAsFile() {
        return this.imFile;
    }

    public Image getImage() {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(this.im, "png", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());
            return new Image(is);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public BufferedImage getImageAsBufferedImage() {
        return this.im;
    }

    public void setImageAsFile(File imFile) throws IOException {
        this.imFile = imFile;
        this.im = ImageIO.read(this.imFile);
        this.images.push(this.im);
    }

    public void undo() {
        try {
            this.images.pop();
            this.setImageAsBuffered(this.images.peek());
            this.imageView.setImage(this.getImage());
        } catch (Exception e) {
            System.out.println("MainController::undo: No images left in the stack!");
        }
    }

    public void setImageAsBuffered(BufferedImage im) {
        /* image file would be the same */
        this.im = im;
        this.images.push(this.im);
    }

    public void setCropMode() {
        this.cropMode = true;
//        mainAnchorPane.getChildren().add(selectionRect);
    }

    public void unsetCropMode() {
        this.cropMode = false;
    }

    private boolean cropMode = false;

    public void addImage(ActionEvent event) {
        try {

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Select an Image:");
            chooser.setInitialDirectory(new File(homeDir + "/Pictures"));
            var file = chooser.showOpenDialog(null);
            this.setImageAsFile(file);
            this.showInfo();

            this.imagePlaceHolder.setVisible(false);
            this.imageView.setImage(this.getImage());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            System.out.println("No image selected!");
        }
    }

    public void cropImage(ActionEvent event) {
        this.cropMode = !this.cropMode;
    }

    public void searchImage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("search-view.fxml"));
            Scene scene = new Scene(loader.load());
            SearchController controller = loader.getController();
            controller.setSelectedImage(this.getImageAsBufferedImage());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.setAlwaysOnTop(true);
            stage.setTitle("Image Search");
            stage.show();
        } catch (Exception e) {

        }
    }

    public void quantizeImage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("color-quantization-view.fxml"));
            Scene scene = new Scene(loader.load());
            ColorQuantizationController controller = loader.getController();
            controller.setMainController(this);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (Exception e) {
            System.out.println("MainController: quantizeImage: Error");
            e.printStackTrace();
        }
    }

    public void showHistogram(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("show-histogram-view.fxml"));
            Scene scene = new Scene(loader.load());
            ShowHistogramController controller = loader.getController();
            controller.buildHistogram(this.getImageAsBufferedImage());

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();

        } catch (Exception e) {
            System.out.println("MainController: showHistogram: Error");
            e.printStackTrace();
        }
    }

    public void saveImageAsIndexed(ActionEvent event) {
        try {
            var im = this.getImageAsBufferedImage();
            // Convert the image to an indexed image with 256 colors
            BufferedImage indexedImage = new BufferedImage(im.getWidth(), im.getHeight(),
                    BufferedImage.TYPE_BYTE_INDEXED);
            Graphics2D g = indexedImage.createGraphics();
            g.drawImage(im, 0, 0, null);
            g.dispose();

            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save image as indexed:");
            chooser.setInitialDirectory(new File(homeDir + "/Pictures"));

            var path = chooser.showSaveDialog(null);
            var segments = this.getImageAsFile().getAbsolutePath().split("\\.");
            if (Arrays.stream(ImageIO.getReaderFormatNames())
                    .toList().contains(segments[segments.length-1])) {
                ImageIO.write(indexedImage, segments[segments.length-1], path);
            } // else throw an error
            this.setImageAsFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void saveImage(ActionEvent event) {
        try {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Save image:");
            chooser.setInitialDirectory(new File(homeDir + "/Pictures"));

            var im = this.getImageAsBufferedImage();
            var path = chooser.showSaveDialog(null);

            var segments = this.getImageAsFile().getAbsolutePath().split("\\.");
            if (Arrays.stream(ImageIO.getReaderFormatNames())
                    .toList().contains(segments[segments.length-1])) {
                ImageIO.write(im, segments[segments.length-1], path);
            } // else throw an error
            this.setImageAsFile(path);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void showInfo() {
        /* clear buffer past */
        this.infoBox.getChildren().clear();
        int
                width = this.getImageAsBufferedImage().getWidth(),
                height = this.getImageAsBufferedImage().getHeight();
        String filePath = this.getImageAsFile().getAbsolutePath();

        var box = new VBox(
                new Label("Image Info"),
                new HBox(new Label("File Path"), new Label(filePath)),
                new HBox(new Label("Resolution"), new Label(width + "x" + height))
        );

        this.infoBox.getChildren().add(box);
    }

    public void toggleFiltersBar(ActionEvent event) {
        this.filtersBar.setVisible(this.filtersBarVisibility = !this.filtersBarVisibility);
    }

    static private String colorToHex(Color c) {
        return String.format("#%02x%02x%02x",
                c.getRed(), c.getGreen(), c.getBlue());
    }

    public void displayColorPalette(List<Color> colors, VBox vbox) {
        HBox box = new HBox();
        box.setPrefHeight(100);

        for (Color c : colors) {
            Pane pane = new Pane();
            pane.setStyle("-fx-background-color: %s;".formatted(colorToHex(c)));
            box.getChildren().add(pane);
            HBox.setHgrow(pane, Priority.SOMETIMES);
        }

        vbox.getChildren().add(box);
    }
}

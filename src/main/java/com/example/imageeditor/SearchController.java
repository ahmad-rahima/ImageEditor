package com.example.imageeditor;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;


class IntTextField extends TextField {
    IntTextField() {
        super();
        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                this.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }
}

public class SearchController {

    @FXML
    public ChoiceBox<String> searchChoice;
    @FXML
    public VBox searchSettings;
    private BufferedImage selectedImage;
    private ArrayList<File> selectedDirectory = new ArrayList<>();
    private final ImageSearcherFn imageSearcherFn = new ImageSearcherFn();
    static final private String homeDir = System.getProperty("user.home");

    public void initialize() {
        this.searchChoice.setOnAction(e -> {
            this.searchSettings.getChildren().clear();

            String selectedOption = this.searchChoice.getValue();
            System.out.println("Selected option: " + selectedOption);
            // Execute your code here
            this.setSettings(e);
        });

        this.searchSettings.setSpacing(8);
    }

    public void search(ActionEvent event) {
        var formats = ImageIO.getReaderFormatNames();
        var extractor = new FilesExtractor(this.selectedDirectory,
                Arrays.stream(formats).toList());
        var choice = searchChoice.getValue();

        System.out.println(extractor.getFiles());
        var searcher = new ImageSearcher(
                this.selectedImage,
                extractor.getFiles(),
                imageSearcherFn.getSearchFn(),
                choice.equals("Image Colors") ? .2f : .5f
        );

        System.out.println("Eventually, Result is of size: " + searcher.getResult().size());
        ((Stage)((Node)event.getTarget()).getScene().getWindow()).close();

        var result = new ArrayList<>(searcher.getResult());
        result.sort((ir0, ir1) -> {
            var prt0 =  ir0.getPriority();
            var prt1 = ir1.getPriority();
            return prt0 > prt1 ? -1
                    : prt0 == prt1 ? 0
                    : 1;
        });

        List<File> files = new ArrayList<>();
        int maxfiles = (choice.equals("Colors") || choice.equals("Image Colors"))
                ? Math.min(3, result.size())
                : result.size();

        System.out.println(choice + ", " + maxfiles);
        for (var r : result) {
            System.out.println("  " + r.get());
        }
        for (var r : result) {
            files.add(r.get());

            if (--maxfiles == 0) break;
        }

        this.createFilesView(files);
    }

    public void setSettings(ActionEvent event) {
        var searchChoice = this.searchChoice.getValue().strip().toLowerCase();

        switch (searchChoice) {
            case "size": setSettingsSize(this.searchSettings); break;
            case "mtime": setSettingsMTime(this.searchSettings); break;
            case "colors": setSettingsColors(this.searchSettings); break;
        }

    }

    private static ArrayList<javafx.scene.paint.Color> selectedColors = new ArrayList<>();
    public static void setSettingsColors(VBox searchSettings) {
        ColorPicker picker = new ColorPicker();

        selectedColors.clear();
        picker.valueProperty().addListener((_obs, _oldColor, newColor) -> {
            selectedColors.add(newColor);
        });

        searchSettings.getChildren().addAll(picker);
    }

    public static void setSettingsMTime(VBox searchSettings) {
        var startField = new DatePicker();
        startField.setPromptText("Start MTime");
        startField.setDisable(true);

        var endField = new DatePicker();
        endField.setPromptText("End MTime");
        endField.setDisable(true);
        var mtimeBox = new HBox(new Label("Fixed MTime: "), startField, endField);
        mtimeBox.setAlignment(Pos.CENTER);
        mtimeBox.setSpacing(8);

        var mtimeCheck = new CheckBox("Search by fixed mtime");
        mtimeCheck.setOnAction(e -> {
            var cond = !((CheckBox)e.getTarget()).isSelected();
            startField.setDisable(cond);
            endField.setDisable(cond);
        });

        searchSettings.getChildren().addAll(/*ascending, */mtimeCheck, mtimeBox);
    }

    public static void setSettingsSize(VBox searchSettings) {
        var startField = new IntTextField();
        startField.setPromptText("Start Size (in bytes)");
        startField.setDisable(true);
        startField.setStyle("-fx-text-fill: black;");

        var endField = new IntTextField();
        endField.setPromptText("End Size (in bytes)");
        endField.setStyle("-fx-text-fill: black;");
        endField.setDisable(true);
        var fixedSizeText = new HBox(new Label("Fixed size: "), startField, endField);
        fixedSizeText.setAlignment(Pos.CENTER);
        fixedSizeText.setSpacing(8);

        var fixedSizeCheck = new CheckBox("Search by fixed size");
        fixedSizeCheck.setOnAction(e -> {
            var cond = !((CheckBox)e.getTarget()).isSelected();
            startField.setDisable(cond);
            endField.setDisable(cond);
        });

        //var ascending = new CheckBox("Ascending");

        searchSettings.getChildren().addAll(/*ascending, */fixedSizeCheck, fixedSizeText);
    }

    public HashMap<String, Object> getSettings() {
        var searchChoice = this.searchChoice.getValue().strip().toLowerCase();

        switch (searchChoice) {
            case "size": return getSettingsSize(this.searchSettings);
            case "mtime": return getSettingsMTime(this.searchSettings);
            case "image colors": return getSettingsImageColors(this.searchSettings);
            case "colors": return getSettingsColors(this.searchSettings);
        }

        return null;
    }

    private HashMap<String, Object> getSettingsColors(VBox searchSettings) {
        var data = new HashMap<String, Object>();
        data.put("selectedColors", selectedColors);

        return data;
    }

    private HashMap<String, Object> getSettingsImageColors(VBox searchSettings) {
        var data = new HashMap<String, Object>();
        data.put("selectedImage", this.selectedImage);

        return data;
    }


    private static HashMap<String, Object> getSettingsMTime(VBox searchSettings) {
        var children = searchSettings.getChildren();
        var data = new HashMap<String, Object>();
//        var selected = ((CheckBox)children.get(0)).isSelected();
//        data.put("ascending", selected);

        var fixedMTime = ((CheckBox) children.get(0)).isSelected();
        data.put("fixedSizeCheck", fixedMTime);

        if (fixedMTime) {
            var startField = (DatePicker) ((HBox) children.get(1)).getChildren().get(1);
            var endField = (DatePicker) ((HBox) children.get(1)).getChildren().get(2);
            data.put("startField", startField.getValue());
            data.put("endField", endField.getValue());
        }

        return data;
    }

    private static HashMap<String, Object> getSettingsSize(VBox searchSettings) {
        var children = searchSettings.getChildren();
        var data = new HashMap<String, Object>();
//        var selected = ((CheckBox)children.get(0)).isSelected();
//        data.put("ascending", selected);

        var fixedSize = ((CheckBox)children.get(0)).isSelected();
        data.put("fixedSizeCheck", fixedSize);

        if (fixedSize)  {
            var startField = (TextField)((HBox)children.get(1)).getChildren().get(1);
            var endField = (TextField)((HBox)children.get(1)).getChildren().get(2);
            data.put("startField", Long.parseLong(startField.getText()));
            data.put("endField", Long.parseLong(endField.getText()));
        }

        return data;
    }

    private void createFilesView(List<File> images) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("files-view.fxml"));
            Scene scene = new Scene(loader.load());
            FilesController controller = loader.getController();
            controller.setImages(images);

            Stage stage = new Stage();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setSelectedImage(BufferedImage selectedImage) {
        this.selectedImage = selectedImage;
    }

    public void selectDirectory(ActionEvent event) {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select a Directory:");
        chooser.setInitialDirectory(new File(homeDir));
        var dir = chooser.showDialog(null);
        if (dir != null)
            this.selectedDirectory.add(dir);
    }

    public void addColor() {
        ArrayList<Color> colors = new ArrayList<Color>();
        colors.add(Color.RED);

        int width = colors.size();
        int height = 1;

        BufferedImage im = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < width; ++y)
            im.setRGB(1, y, colors.get(0).getRGB());
    }


    private HashMap<String, Object> grabChoiceData() {
        String choice = this.searchChoice.getValue().strip().toLowerCase();

        HashMap<String, Object> data = new HashMap<>();
        data.put("ascending", true);
        return data;
    }


    private class ImageSearcherFn {

        private static final
        HashMap<String,
                Function<HashMap<String, Object>,
                        Function<File, Number>>>
                choiceActions = new HashMap<>();

        static {
            // TODO: consider changing these from String to Enum type.
            // GUI should not be part of logic!
            choiceActions.put("size", ImageSearcherFn::searchBySize);
            choiceActions.put("mtime", ImageSearcherFn::searchByMTime);
            choiceActions.put("image colors", ImageSearcherFn::searchByImageColors);
            choiceActions.put("colors", ImageSearcherFn::searchByColors);
        }

        public Function<File, Number> getSearchFn() {
            String choice = searchChoice.getValue().strip().toLowerCase();
            HashMap<String, Object> data = getSettings();
            return choiceActions.get(choice).apply(data);
        }

        private static Function<File, Number> searchByExplicitSize(HashMap<String, Object> data) {
//            System.out.println(("Searching by size.."));
//            System.out.println("startField:" + (long)data.get("startField"));
//            System.out.println("endField:" + (long)data.get("endField"));
//            return (File f) -> 1;
//            byte sign = (byte) ((boolean)data.get("ascending") ? 1 : -1);
            return (File f) -> (f.length() >= (long)data.get("startField") &&
                    f.length() <= (long)data.get("endField")) ? 1
                    : 0;
//            return (File f) -> 1 * Math.abs(f.length() - (long)data.get("fixedSizeText"));
        }

        private static Function<File, Number> searchBySize(HashMap<String, Object> data) {
            if ((boolean) data.get("fixedSizeCheck")) {
                return searchByExplicitSize(data);
            }

//            byte sign = (byte) ((boolean)data.get("ascending") ? 1 : -1);
            return (File f) -> 1 * f.length();
        }

        private static Function<File, Number> searchByMTime(HashMap<String, Object> data) {
            LocalDate startDate = (LocalDate) data.get("startField");
            LocalDate endDate = (LocalDate) data.get("endField");
            return (File f) -> {
                // Get the last modified date of the file as a LocalDate
                LocalDate fileDate = LocalDate.ofEpochDay(f.lastModified() / (24 * 60 * 60 * 1000) + 1);
                return (startDate.isBefore(fileDate) && endDate.isAfter(fileDate)) ? 1
                        : 0;
            };
        }

        private static Function<File, Number> searchByATime(HashMap<String, Object> data) {
            throw new RuntimeException("Not implemented yet!");
        }

        private static Function<File, Number> searchByImageColors(HashMap<String, Object> data) {
            BufferedImage selectedImage = (BufferedImage)data.get("selectedImage");

            return (File f) -> {
                BufferedImage im = null;
                try {
                    im = ImageIO.read(f);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                OctreeColorQuantizer q = new OctreeColorQuantizer(selectedImage);
                OctreeColorQuantizer q1 = new OctreeColorQuantizer(im);

                return OctreeColorQuantizer.compare(q, q1);
            };
        }


        private static Function<File, Number> searchByColors(HashMap<String, Object> data) {
            System.out.println("Searching by colors..");
            ArrayList<Color> colors;
            colors = selectedColors.stream()
                    .map(c -> new Color((int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255)))
                    .collect(Collectors.toCollection(ArrayList::new));

            System.out.println("Selected Colors are: " + selectedColors);
            System.out.println("Selected G: " + selectedColors.get(0).getGreen());
            System.out.println("Colors are: " + colors);


            return (File f) -> {
                BufferedImage im;
                try {
                    im = ImageIO.read(f);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                OctreeColorQuantizer q = new OctreeColorQuantizer(im);

                return OctreeColorQuantizer.compare(q, colors);
            };
        }
    }

}

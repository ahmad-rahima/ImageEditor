package com.example.imageeditor;

import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class ShowHistogramController {

    private static final int MAX_COLORS = 255;
    @FXML
    public BarChart<String, Number> chart;


    public void buildHistogram(BufferedImage image) {

        // Calculate the color frequencies
        Map<Integer, Integer> frequencyMap = new HashMap<>();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int color = image.getRGB(x, y) & 0xFFFFFF;
                frequencyMap.put(color, frequencyMap.getOrDefault(color, 0) + 1);
            }
        }

        // Sort the colors by frequency
        Integer[] colors = frequencyMap.keySet().toArray(new Integer[0]);
        Arrays.sort(colors, (c1, c2) -> frequencyMap.get(c1) - frequencyMap.get(c2));

        // Create the chart
        CategoryAxis xAxis = new CategoryAxis();
        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Frequency");

        chart.setTitle("Color Frequency");

        // Add the color bars to the chart
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        for (int i = 0; i < Math.min(MAX_COLORS, colors.length); i++) {
            int color = colors[i];
            int frequency = frequencyMap.get(color);
            String colorCode = String.format("#%06X", color);
            XYChart.Data<String, Number> data = new XYChart.Data<>(colorCode, frequency);
            series.getData().add(data);
        }
        chart.getData().add(series);

    }


}
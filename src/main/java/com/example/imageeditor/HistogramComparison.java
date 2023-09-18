package com.example.imageeditor;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;


// TODO: make new class `Histogram` this one should be passed to this, instead of `BufferedImage`
public class HistogramComparison {
    public static double compareHistograms(BufferedImage image1, BufferedImage image2, int k) {
        // Calculate histograms for image1 and image2
        int[] histogram1 = calculateHistogram(image1, k);
        int[] histogram2 = calculateHistogram(image2, k);

        // Normalize histograms
        double[] normalizedHist1 = normalizeHistogram(histogram1, image1.getWidth() * image1.getHeight());
        double[] normalizedHist2 = normalizeHistogram(histogram2, image2.getWidth() * image2.getHeight());

        // Calculate Bhattacharyya coefficient
        double similarity = calculateBhattacharyyaCoefficient(normalizedHist1, normalizedHist2);

        return similarity;
    }

    private static int[] calculateHistogram(BufferedImage image, int k) {
        // Initialize histogram array
        int[] histogram = new int[k];

        // Iterate over each pixel in the image
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                // Get the quantized color index for the pixel
                int colorIndex = Math.abs(image.getRGB(x, y) % k);

                // Increment the count for the corresponding color index in the histogram
                histogram[colorIndex]++;
            }
        }

        return histogram;
    }

    private static double[] normalizeHistogram(int[] histogram, int totalPixels) {
        // Create a new array for the normalized histogram
        double[] normalizedHistogram = new double[histogram.length];

        // Normalize each bin value by dividing by the total number of pixels
        for (int i = 0; i < histogram.length; i++) {
            normalizedHistogram[i] = (double) histogram[i] / totalPixels;
        }

        return normalizedHistogram;
    }

    private static double calculateBhattacharyyaCoefficient(double[] histogram1, double[] histogram2) {
        // Calculate the Bhattacharyya coefficient
        double bCoefficient = 0.0;

        for (int i = 0; i < histogram1.length; i++) {
            bCoefficient += Math.sqrt(histogram1[i] * histogram2[i]);
        }

        return bCoefficient;
    }

    public static void main(String[] args) {
        try {
            // Example usage
            BufferedImage image1 = ImageIO.read(new File("nice-wallpaper.png"));
            BufferedImage image2 = ImageIO.read(new File("output.png"));
            int k = 2;

            double similarity = compareHistograms(image1, image2, k);
            System.out.println("Histogram similarity: " + similarity);
        } catch (IOException e) {
            System.out.println("some error happened while reading/writing image.");
        }
    }
}

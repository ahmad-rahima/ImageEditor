package com.example.imageeditor;

import java.awt.*;
import java.nio.Buffer;
import java.util.List;
import java.awt.image.BufferedImage;

public abstract class ImageQuantizer<InternalColors> implements ImageFilter {
    protected static final int PREF_LEAVES = 256;
    protected int pref_colors;
    protected List<Color> palette;
    protected BufferedImage resultImage;
    protected InternalColors colors;
    protected BufferedImage tmpIm;

    protected ImageScaler imageScaler;

    public static int[] getColorsForImage(BufferedImage im) {
        return im.getRGB(0, 0, im.getWidth(), im.getHeight(), null, 0, im.getWidth());
    }

    public InternalColors getImageColors() {
        return (InternalColors) ImageQuantizer.getColorsForImage(this.tmpIm);
    }


    private double distance(int color1, int color2) {
        int redDiff = ((color1 >> 16) & 0xFF) - ((color2 >> 16) & 0xFF);
        int greenDiff = ((color1 >> 8) & 0xFF) - ((color2 >> 8) & 0xFF);
        int blueDiff = (color1 & 0xFF) - (color2 & 0xFF);
        return Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);
    }

    protected static double distance(float[] color0, float[] color1) {
        float redDiff = color0[0] - color1[0];
        float greenDiff = color0[1] - color1[1];
        float blueDiff = color0[2] - color1[2];
        return Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);
    }

    protected static double distance(Color color0, Color color1) {
        int redDiff = color0.getRed() - color1.getRed();
        int greenDiff = color0.getGreen() - color1.getGreen();
        int blueDiff = color0.getBlue() - color1.getBlue();
        return Math.sqrt(redDiff * redDiff + greenDiff * greenDiff + blueDiff * blueDiff);
    }

    public ImageQuantizer(BufferedImage im) {
        this.imageScaler = new ImageScaler(im);
        this.tmpIm = imageScaler.getImage();
        this.colors = this.getImageColors();
        this.pref_colors = this.findBestColorsNo();
        this.palette = this.compute();
        this.resultImage = this.generateImage(im, this.palette);
    }

    public ImageQuantizer(BufferedImage im, int pref_colors) {
        this.imageScaler = new ImageScaler(im);
        this.tmpIm = imageScaler.getImage();
        this.colors = this.getImageColors();
        this.pref_colors = pref_colors;
        this.palette = this.compute();
        this.resultImage = this.generateImage(im, this.palette);
    }

    protected abstract List<Color> compute();

    protected int findBestColorsNo() {
        return PREF_LEAVES;
    }

    public List<Color> getPalette() {
        return this.palette;
    }

    public BufferedImage getImage() {
        return this.resultImage;
    }

    private BufferedImage generateImage(BufferedImage im, List<Color> palette) {
        int width = im.getWidth(), height = im.getHeight();
        var colors = ImageQuantizer.getColorsForImage(im);

        // Generate quantized image
        BufferedImage quantizedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int i = 0; i < colors.length; i++) {
            int clusterIndex = 0;
            double minDistance = Double.MAX_VALUE;
            for (int j = 0; j < palette.size(); j++) {
                double distance = distance(colors[i], palette.get(j).getRGB());
                if (distance < minDistance) {
                    minDistance = distance;
                    clusterIndex = j;
                }
            }
            quantizedImage.setRGB(i % width, i / width, palette.get(clusterIndex).getRGB());
        }

        return quantizedImage;
    }
}

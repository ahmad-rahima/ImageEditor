package com.example.imageeditor;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;


public class UniformQuantizer extends ImageQuantizer<Color[]> {
    private int[][] rRegionMappings;
    private int[][] gRegionMappings;
    private int[][] bRegionMappings;
    private int[] rRepresentativeColorPerRegion;
    private int[] gRepresentativeColorPerRegion;
    private int[] bRepresentativeColorPerRegion;

    public UniformQuantizer(BufferedImage image) {
        super(image);
    }

    @Override
    protected List<Color> compute() {
        this.rRegionMappings = new int[8][];
        this.gRegionMappings = new int[8][];
        this.bRegionMappings = new int[4][];
        this.rRepresentativeColorPerRegion = new int[8];
        this.gRepresentativeColorPerRegion = new int[8];
        this.bRepresentativeColorPerRegion = new int[4];
        for (int i = 0; i < 8; i++) {
            this.rRegionMappings[i] = new int[this.tmpIm.getWidth() * this.tmpIm.getHeight()];
            this.gRegionMappings[i] = new int[this.tmpIm.getWidth() * this.tmpIm.getHeight()];
            if (i < 4) {
                this.bRegionMappings[i] = new int[this.tmpIm.getWidth() * this.tmpIm.getHeight()];
            }
        }

        return this.quantize();
    }

    public List<Color> quantize() {
        // loop through all pixels and the put the colors into the respective color regions
        int index = 0;
        for (int y = 0; y < this.tmpIm.getHeight(); y++) {
            for (int x = 0; x < this.tmpIm.getWidth(); x++) {
                Color pixel = new Color(this.tmpIm.getRGB(x, y));
                int red = pixel.getRed();
                int green = pixel.getGreen();
                int blue = pixel.getBlue();
                // find the index where the color is supposed to go and add it
                rRegionMappings[getRegionIndex(red)][index] = red;
                gRegionMappings[getRegionIndex(green)][index] = green;
                bRegionMappings[getRegionIndex(blue)/2][index] = blue;
                index++;
            }
        }

        // find the color that represents each region
        for (int i = 0; i < 8; i++) {
            rRepresentativeColorPerRegion[i] = (int) Math.round(getAverage(rRegionMappings[i]));
            gRepresentativeColorPerRegion[i] = (int) Math.round(getAverage(gRegionMappings[i]));
            if (i < 4) {
                bRepresentativeColorPerRegion[i] = (int) Math.round(getAverage(bRegionMappings[i]));
            }
        }

        ArrayList<Color> palette = new ArrayList<>();
        for (var r : rRepresentativeColorPerRegion) {
            for (var g : gRepresentativeColorPerRegion) {
                for (var b : bRepresentativeColorPerRegion) {
                    palette.add(new Color(r, g, b));
                }
            }
        }

        return palette;
    }

    public void saveQuantized(String filename) throws IOException {
        File file = new File(filename);
        ImageIO.write(this.tmpIm, "png", file);
    }

    private int getRegionIndex(int colorValue) {
        int[][] regionRanges = {{0, 31}, {32, 63}, {64, 95}, {96, 127}, {128, 159}, {160, 191}, {192, 223}, {224, 255}};
        for (int i = 0; i < regionRanges.length; i++) {
            int[] regionValue = regionRanges[i];
            if (colorValue >= regionValue[0] && colorValue <= regionValue[1]) {
                return i;
            }
        }
        return -1;
    }

    private double getAverage(int[] values) {
        double sum = 0;
        int count = 0;
        for (int value : values) {
            if (value !=0) {
                sum += value;
                count++;
            }
        }
        return (count > 0) ? sum / count : 0;
    }

    public static void main(String[] args) {
        try {
            BufferedImage im = ImageIO.read(new File("/home/adr/Downloads/andrew-tate.png"));
            UniformQuantizer quantizer = new UniformQuantizer(im);
            ImageIO.write(quantizer.getImage(), "png", new File("newimage1.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
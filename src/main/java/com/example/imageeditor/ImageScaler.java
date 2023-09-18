package com.example.imageeditor;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageScaler {

    static final private int MAX = 256;
    private final BufferedImage resultImage;
    private final BufferedImage originalImage;
    private final int originalWidth;
    private final int originalHeight;
    private final int width;
    private final int height;

    public ImageScaler(BufferedImage im) {
        this.originalImage = im;
        // Determine original image dimensions
        this.originalWidth = im.getWidth();
        this.originalHeight = im.getHeight();

        // Determine temporary image dimensions based on aspect ratio
        if (originalHeight < originalWidth) {
            width = MAX;
            height = (int) (((double) width / (double) originalWidth) * originalHeight);
        } else {
            height = MAX;
            width = (int) (((double) height / (double) originalHeight) * originalWidth);
        }

        // Create temporary image and scale down the input image
        this.resultImage = new BufferedImage(width, height, im.getType());
        Graphics2D g2d = this.resultImage.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.drawImage(im, 0, 0, width, height, null);
        g2d.dispose();
    }

    public BufferedImage getImage() {
        return this.resultImage;
    }

    public int getOriginalWidth() {
        return this.originalWidth;
    }

    public int getOriginalHeight() {
        return this.originalHeight;
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public BufferedImage getOriginalImage() {
        return this.originalImage;
    }
}
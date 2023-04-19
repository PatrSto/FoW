package org.example.Utilities;

import java.awt.image.BufferedImage;
import java.io.Serializable;

public class ImageState implements Serializable {
    private final SerializableBufferedImage image;
    private final SerializableBufferedImage fogLayer;
    private final double scaleFactor;
    private final int rotation;
    private final String tabName;

    public ImageState(BufferedImage image, BufferedImage fogLayer, double scaleFactor, int rotation, String tabName) {
        this.image = new SerializableBufferedImage(image);
        this.fogLayer = new SerializableBufferedImage(fogLayer);
        this.scaleFactor = scaleFactor;
        this.rotation = rotation;
        this.tabName = tabName;
    }

    public BufferedImage getImage() {
        return image.getBufferedImage();
    }

    public BufferedImage getFogLayer() {
        return fogLayer.getBufferedImage();
    }

    public double getScaleFactor() {
        return scaleFactor;
    }

    public int getRotation() {
        return rotation;
    }

    public String getTabName() {
        return tabName;
    }
}


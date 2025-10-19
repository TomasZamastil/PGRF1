package rasterize;

import java.awt.*;
import java.awt.image.BufferedImage;

public class RasterBufferedImage implements Raster {

    private final BufferedImage image;

    public RasterBufferedImage(int width, int height) {
        image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    }

    @Override
    public int getColor(int x, int y) {
        return 0;
    }

    @Override
    public void setPixel(int x, int y, int color) {
        image.setRGB(x, y, color);
    }

    @Override
    public int[] getPixel() {
        return new int[0];
    }

    @Override
    public int getHeight() {
        return 0;
    }

    @Override
    public int getWidth() {
        return 0;
    }

    public BufferedImage getImage() {
        return image;
    }

    @Override
    public void clear() {
        Graphics graphics = image.getGraphics();
        graphics.clearRect(0, 0, image.getWidth(), image.getHeight());
    }
}

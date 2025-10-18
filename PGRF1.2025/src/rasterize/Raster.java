package rasterize;

public interface Raster {

    void setPixel(int x, int y, int color);
    int[] getPixel();
    int getColor(int x, int y);
    int getHeight();
    int getWidth();
    void clear();
}

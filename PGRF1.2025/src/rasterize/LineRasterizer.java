package rasterize;

import model.Point;

public abstract class LineRasterizer {
    protected RasterBufferedImage raster;

    public LineRasterizer(RasterBufferedImage raster) {
        this.raster = raster;
    }

    public void rasterize(int x1, int y1, int x2, int y2){

    }

    public void rasterize(Point p1, Point p2) {
        rasterize(p1.getX(), p1.getY(), p2.getX(), p2.getY());
    }
}

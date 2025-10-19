package rasterize;

import model.Point;
import model.Polygon;

public class PolygonRasterizer {

    private LineRasterizer lineRasterizer;

    public PolygonRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    public void rasterize(Polygon polygon) {
        if (polygon == null || polygon.getSize() < 2) return;

        var pts = polygon.getPoints();
        for (int i = 0; i < polygon.getSize() - 1; i++) {
            Point a = pts.get(i);
            Point b = pts.get(i + 1);
            lineRasterizer.rasterize(a.getX(), a.getY(), b.getX(), b.getY());
        }

        if (polygon.getSize() >= 3) {
            Point first = pts.get(0);
            Point last = pts.get(polygon.getSize() - 1);
            lineRasterizer.rasterize(last.getX(), last.getY(), first.getX(), first.getY());
        }
    }
}

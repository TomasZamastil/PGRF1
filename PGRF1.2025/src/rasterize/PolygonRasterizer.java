package rasterize;

import model.Polygon;

public class PolygonRasterizer {

    private LineRasterizer lineRasterizer;

    public PolygonRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }

    public void rasterize(Polygon polygon) {
        //kontrola jestli máme alespoň tři pointy - proloopovat
        for (int i = 0; i < polygon.getSize(); i++) {
            polygon.getPoints().get(i);
            //todo pomoc
        }
    }

    public void setLineRasterizer(LineRasterizer lineRasterizer) {
        this.lineRasterizer = lineRasterizer;
    }
}

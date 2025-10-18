package rasterize;

public class LineRasterizerTrivial extends LineRasterizer {

    public LineRasterizerTrivial(RasterBufferedImage raster) {
        super(raster);
    }

    @Override
    public void rasterize(int x1, int y1, int x2, int y2) {
        if (x1 == x2) {
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                raster.setPixel(x1, y, 0x00ff00);
            }
            return;
        }

        if (x1 > x2) {
            int tmpX = x1;
            int tmpY = y1;
            x1 = x2;
            y1 = y2;
            x2 = tmpX;
            y2 = tmpY;
        }

        float k = (float)(y2 - y1) / (x2 - x1);
        float q = y1 - k * x1;

        for (int x = x1; x <= x2; x++) {
            int y = Math.round(k * x + q);
            raster.setPixel(x, y, 0x00ff00);
        }
    }
}

package rasterize;

public class ShiftedLineRasterizer extends LineRasterizer {
    //To, že by šlo tuto třídu implementovat s logikou pro úhly zde a pak pouze zavolat už existující LineRasterizerTrivial mě napadlo až po této implementaci
    public ShiftedLineRasterizer(RasterBufferedImage raster) {
        super(raster);
    }

    @Override
    public void rasterize(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        double angle = Math.toDegrees(Math.atan2(Math.abs(dy), Math.abs(dx)));

        if (angle < 22.5) {
            y2 = y1;
        } else if (angle < 67.5) {
            int sgnY = Integer.signum(dy == 0 ? 1 : dy);
            y2 = y1 + sgnY * Math.abs(dx);
        } else if (angle < 112.5) {
            x2 = x1;
        }

        if (x1 == x2) {
            int startY = Math.min(y1, y2);
            int endY = Math.max(y1, y2);
            for (int y = startY; y <= endY; y++) {
                raster.setPixel(x1, y, 0xff0000);
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

        float k = (float) (y2 - y1) / (x2 - x1);
        float q = y1 - k * x1;

        for (int x = x1; x <= x2; x++) {
            int y = Math.round(k * x + q);
            raster.setPixel(x, y, 0xff0000);
        }
    }
}

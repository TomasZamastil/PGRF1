package rasterize;

import java.awt.*;

public class LineRasterizerColorTransition {

    private final Raster raster;

    public LineRasterizerColorTransition(Raster raster) {
        this.raster = raster;
    }

    public void rasterize(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;

        Color c1 = Color.RED;
        Color c2 = Color.GREEN;

        float[] comp1 = c1.getRGBColorComponents(null);
        float[] comp2 = c2.getRGBColorComponents(null);

        if (Math.abs(dx) >= Math.abs(dy)) {
            int sx = (x2 >= x1) ? 1 : -1;
            int steps = Math.abs(dx);
            if (steps == 0) {
                raster.setPixel(x1, y1, c1.getRGB());
                return;
            }

            for (int i = 0, x = x1; i <= steps; i++, x += sx) {
                float t = i / (float) steps;
                int y = Math.round(y1 + t * dy);

                float r = comp1[0] + (comp2[0] - comp1[0]) * t;
                float g = comp1[1] + (comp2[1] - comp1[1]) * t;
                float b = comp1[2] + (comp2[2] - comp1[2]) * t;
                int color = new Color(r, g, b).getRGB();

                raster.setPixel(x, y, color);
            }

        } else {
            int sy = (y2 >= y1) ? 1 : -1;
            int steps = Math.abs(dy);
            if (steps == 0) {
                raster.setPixel(x1, y1, c1.getRGB());
                return;
            }

            for (int i = 0, y = y1; i <= steps; i++, y += sy) {
                float t = i / (float) steps;
                int x = Math.round(x1 + t * dx);

                float r = comp1[0] + (comp2[0] - comp1[0]) * t;
                float g = comp1[1] + (comp2[1] - comp1[1]) * t;
                float b = comp1[2] + (comp2[2] - comp1[2]) * t;
                int color = new Color(r, g, b).getRGB();

                raster.setPixel(x, y, color);
            }
        }
    }
}

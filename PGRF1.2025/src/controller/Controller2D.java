package controller;

import model.Line;
import model.Point;
import view.Panel;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerTrivial;
import rasterize.ShiftedLineRasterizer;
import rasterize.LineRasterizerColorTransition;

import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

public class Controller2D {

    private int mostRecentPressedKey = KeyEvent.VK_M;

    private final Panel panel;
    private final LineRasterizer lineRasterizer;
    private final ShiftedLineRasterizer shifted;
    private final LineRasterizerColorTransition gradient;

    private final ArrayList<Line> lines = new ArrayList<>();
    private final ArrayList<Boolean> lineShiftFlags = new ArrayList<>();
    private final ArrayList<Boolean> lineGradientFlags = new ArrayList<>();

    private final ArrayList<List<Point>> polygons = new ArrayList<>();
    private final ArrayList<Point> buildingPolygon = new ArrayList<>();

    private boolean gradientNEnabled = false;

    private Point firstPoint = null;

    public Controller2D(Panel panel) {
        this.panel = panel;
        this.panel.setFocusable(true);
        this.panel.requestFocusInWindow();

        lineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        shifted = new ShiftedLineRasterizer(panel.getRaster());
        gradient = new LineRasterizerColorTransition(panel.getRaster());

        initListeners();
    }

    private void initListeners() {

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX(), y = e.getY();
                if (!inside(x, y)) return;

                if (mostRecentPressedKey == KeyEvent.VK_N) {
                    if (firstPoint == null) {
                        firstPoint = new Point(x, y);
                    } else {
                        boolean sh = e.isShiftDown();
                        int nx = x, ny = y;
                        if (sh) {
                            int[] s = snap(firstPoint.getX(), firstPoint.getY(), x, y);
                            nx = s[0]; ny = s[1];
                        }
                        lines.add(new Line(firstPoint, new Point(nx, ny)));
                        lineShiftFlags.add(sh);
                        lineGradientFlags.add(gradientNEnabled);
                        firstPoint = null;
                        redrawAll();
                    }
                } else if (mostRecentPressedKey == KeyEvent.VK_P) {
                    if (e.getClickCount() >= 2) {
                        closePolygon();
                        return;
                    }
                    if (buildingPolygon.isEmpty()) {
                        buildingPolygon.add(new Point(x, y));
                    } else {
                        Point last = buildingPolygon.get(buildingPolygon.size() - 1);
                        int nx = x, ny = y;
                        if (e.isShiftDown()) {
                            int[] s = snap(last.getX(), last.getY(), x, y);
                            nx = s[0]; ny = s[1];
                        }
                        buildingPolygon.add(new Point(nx, ny));
                    }
                    redrawAll();
                }
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int x = e.getX(), y = e.getY();
                if (!inside(x, y)) return;

                if (mostRecentPressedKey == KeyEvent.VK_N && firstPoint != null) {
                    panel.getRaster().clear();
                    drawStatic();

                    boolean sh = e.isShiftDown();
                    int nx = x, ny = y;
                    if (sh) {
                        int[] s = snap(firstPoint.getX(), firstPoint.getY(), x, y);
                        nx = s[0]; ny = s[1];
                    }

                    if (gradientNEnabled) {
                        gradient.rasterize(firstPoint.getX(), firstPoint.getY(), nx, ny);
                    } else {
                        (sh ? shifted : lineRasterizer).rasterize(firstPoint.getX(), firstPoint.getY(), nx, ny);
                    }
                    panel.repaint();
                } else if (mostRecentPressedKey == KeyEvent.VK_P && !buildingPolygon.isEmpty()) {
                    panel.getRaster().clear();
                    drawStatic();

                    for (int i = 0; i < buildingPolygon.size() - 1; i++) {
                        Point a = buildingPolygon.get(i), b = buildingPolygon.get(i + 1);
                        lineRasterizer.rasterize(a.getX(), a.getY(), b.getX(), b.getY());
                    }
                    Point last = buildingPolygon.get(buildingPolygon.size() - 1);
                    int nx = x, ny = y;
                    if (e.isShiftDown()) {
                        int[] s = snap(last.getX(), last.getY(), x, y);
                        nx = s[0]; ny = s[1];
                    }
                    lineRasterizer.rasterize(last.getX(), last.getY(), nx, ny);
                    panel.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX(), y = e.getY();
                if (!inside(x, y)) return;

                if (mostRecentPressedKey == KeyEvent.VK_M) {
                    panel.getRaster().clear();
                    drawStatic();
                    (e.isShiftDown() ? shifted : lineRasterizer)
                            .rasterize(panel.getWidth() / 2, panel.getHeight() / 2, x, y);
                    panel.repaint();
                }
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_M:
                        mostRecentPressedKey = KeyEvent.VK_M;
                        break;
                    case KeyEvent.VK_N:
                        mostRecentPressedKey = KeyEvent.VK_N;
                        break;
                    case KeyEvent.VK_P:
                        mostRecentPressedKey = KeyEvent.VK_P;
                        break;
                    case KeyEvent.VK_G:
                        gradientNEnabled = !gradientNEnabled;
                        break;
                    case KeyEvent.VK_ENTER:
                        if (mostRecentPressedKey == KeyEvent.VK_P) closePolygon();
                        break;
                    case KeyEvent.VK_C:
                        lines.clear();
                        lineShiftFlags.clear();
                        lineGradientFlags.clear();
                        polygons.clear();
                        buildingPolygon.clear();
                        firstPoint = null;
                        panel.getRaster().clear();
                        panel.repaint();
                        break;
                }
            }
        });
    }

    private boolean inside(int x, int y) {
        return !(x < 0 || y < 0 || x >= panel.getWidth() || y >= panel.getHeight());
    }

    private void closePolygon() {
        if (buildingPolygon.size() >= 3) {
            polygons.add(new ArrayList<>(buildingPolygon));
        }
        buildingPolygon.clear();
        redrawAll();
    }

    private int[] snap(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1, dy = y2 - y1;
        double a = Math.toDegrees(Math.atan2(Math.abs(dy), Math.abs(dx)));
        //pak ještě zjisti proč to nešlo s těma radianama a přepiš to na ně zpátky, protože tohle je hrozně implementovaný
        if (a < 22.5) return new int[]{x2, y1};
        if (a < 67.5) return new int[]{x2, y1 + Integer.signum(dy == 0 ? 1 : dy) * Math.abs(dx)};
        if (a < 112.5) return new int[]{x1, y2};
        return new int[]{x2, y2};
    }

    private void drawStatic() {
        for (int i = 0; i < lines.size(); i++) {
            Line L = lines.get(i);
            boolean sh = lineShiftFlags.get(i);
            boolean gr = lineGradientFlags.get(i);
            if (gr) {
                gradient.rasterize(L.getX1(), L.getY1(), L.getX2(), L.getY2());
            } else {
                (sh ? shifted : lineRasterizer).rasterize(L.getX1(), L.getY1(), L.getX2(), L.getY2());
            }
        }
        for (List<Point> poly : polygons) {
            if (poly.size() < 2) continue;
            for (int i = 0; i < poly.size() - 1; i++) {
                Point a = poly.get(i), b = poly.get(i + 1);
                lineRasterizer.rasterize(a.getX(), a.getY(), b.getX(), b.getY());
            }
            Point last = poly.get(poly.size() - 1), first = poly.get(0);
            lineRasterizer.rasterize(last.getX(), last.getY(), first.getX(), first.getY());
        }
    }

    private void redrawAll() {
        panel.getRaster().clear();
        drawStatic();
        if (mostRecentPressedKey == KeyEvent.VK_P && buildingPolygon.size() >= 2) {
            for (int i = 0; i < buildingPolygon.size() - 1; i++) {
                Point a = buildingPolygon.get(i), b = buildingPolygon.get(i + 1);
                lineRasterizer.rasterize(a.getX(), a.getY(), b.getX(), b.getY());
            }
        }
        panel.repaint();
    }
}

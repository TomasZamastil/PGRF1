package controller;

import model.Line;
import model.Point;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerTrivial;
import rasterize.ShiftedLineRasterizer;
import view.Panel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

public class Controller2D {

    private static class DrawnPolygon {
        List<Point> points = new ArrayList<>();
        List<Boolean> edgeShiftFlags = new ArrayList<>();
    }

    private int mostRecentPressedKey = 0x4D;
    private final LineRasterizer lineRasterizer;
    private final ShiftedLineRasterizer shifted;
    private final Panel panel;

    private final ArrayList<Line> lines = new ArrayList<>();
    private final ArrayList<Boolean> lineShiftFlags = new ArrayList<>();

    private final ArrayList<DrawnPolygon> polygons = new ArrayList<>();
    private final ArrayList<Point> buildingPoints = new ArrayList<>();
    private final ArrayList<Boolean> buildingEdgeShiftFlags = new ArrayList<>();
    private boolean lastMouseShiftDown = false;

    private Point firstPoint = null;

    public Controller2D(Panel panel) {
        this.panel = panel;
        this.panel.setFocusable(true);
        this.panel.requestFocusInWindow();
        lineRasterizer = new LineRasterizerTrivial(panel.getRaster());
        shifted = new ShiftedLineRasterizer(panel.getRaster());
        initListeners();
    }

    private void initListeners() {

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (x < 0 || y < 0 || x >= panel.getWidth() || y >= panel.getHeight()) return;

                if (mostRecentPressedKey == KeyEvent.VK_N) {
                    if (firstPoint == null) {
                        firstPoint = new Point(x, y);
                    } else {
                        int nx = x, ny = y;
                        boolean shift = e.isShiftDown();
                        if (shift) {
                            int[] s = snapEndpoint(firstPoint.getX(), firstPoint.getY(), x, y);
                            nx = s[0]; ny = s[1];
                        }
                        lines.add(new Line(firstPoint, new Point(nx, ny)));
                        lineShiftFlags.add(shift);
                        firstPoint = null;
                        redrawAll();
                    }
                } else if (mostRecentPressedKey == KeyEvent.VK_P) {
                    if (e.getClickCount() >= 2) {
                        closePolygon(lastMouseShiftDown);
                        return;
                    }
                    if (buildingPoints.isEmpty()) {
                        buildingPoints.add(new Point(x, y));
                    } else {
                        Point prev = buildingPoints.get(buildingPoints.size() - 1);
                        int nx = x, ny = y;
                        boolean shift = e.isShiftDown();
                        if (shift) {
                            int[] s = snapEndpoint(prev.getX(), prev.getY(), x, y);
                            nx = s[0]; ny = s[1];
                        }
                        buildingPoints.add(new Point(nx, ny));
                        buildingEdgeShiftFlags.add(shift);
                    }
                    redrawAll();
                }
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                int x = e.getX(), y = e.getY();
                if (x < 0 || y < 0 || x >= panel.getWidth() || y >= panel.getHeight()) return;
                lastMouseShiftDown = e.isShiftDown();

                if (mostRecentPressedKey == KeyEvent.VK_N && firstPoint != null) {
                    panel.getRaster().clear();
                    drawStatic();
                    int nx = x, ny = y;
                    boolean shift = e.isShiftDown();
                    if (shift) {
                        int[] s = snapEndpoint(firstPoint.getX(), firstPoint.getY(), x, y);
                        nx = s[0]; ny = s[1];
                    }
                    LineRasterizer active = shift ? shifted : lineRasterizer;
                    active.rasterize(firstPoint.getX(), firstPoint.getY(), nx, ny);
                    panel.repaint();
                } else if (mostRecentPressedKey == KeyEvent.VK_P && !buildingPoints.isEmpty()) {
                    panel.getRaster().clear();
                    drawStatic();
                    drawBuildingPolygonEdges();
                    Point last = buildingPoints.get(buildingPoints.size() - 1);
                    int nx = x, ny = y;
                    boolean shift = e.isShiftDown();
                    if (shift) {
                        int[] s = snapEndpoint(last.getX(), last.getY(), x, y);
                        nx = s[0]; ny = s[1];
                    }
                    LineRasterizer active = shift ? shifted : lineRasterizer;
                    active.rasterize(last.getX(), last.getY(), nx, ny);
                    panel.repaint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                int x = e.getX();
                int y = e.getY();
                if (x < 0 || y < 0 || x >= panel.getWidth() || y >= panel.getHeight()) return;

                if (mostRecentPressedKey == KeyEvent.VK_M) {
                    panel.getRaster().clear();
                    drawStatic();
                    boolean shift = e.isShiftDown();
                    LineRasterizer active = shift ? shifted : lineRasterizer;
                    active.rasterize(panel.getWidth() / 2, panel.getHeight() / 2, x, y);
                    panel.repaint();
                }
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_0:
                        mostRecentPressedKey = 0x4F;
                        break;
                    case KeyEvent.VK_P:
                        mostRecentPressedKey = 0x50;
                        break;
                    case KeyEvent.VK_M:
                        mostRecentPressedKey = 0x4D;
                        break;
                    case KeyEvent.VK_N:
                        mostRecentPressedKey = 0x4E;
                        break;
                    case KeyEvent.VK_ENTER:
                        if (mostRecentPressedKey == KeyEvent.VK_P) closePolygon(lastMouseShiftDown);
                        break;
                    case KeyEvent.VK_C:
                        lines.clear();
                        lineShiftFlags.clear();
                        polygons.clear();
                        buildingPoints.clear();
                        buildingEdgeShiftFlags.clear();
                        firstPoint = null;
                        panel.getRaster().clear();
                        panel.repaint();
                        break;
                }
            }
        });
    }

    private void closePolygon(boolean shiftForClosingEdge) {
        if (buildingPoints.size() >= 3) {
            DrawnPolygon poly = new DrawnPolygon();
            poly.points.addAll(buildingPoints);
            poly.edgeShiftFlags.addAll(buildingEdgeShiftFlags);
            poly.edgeShiftFlags.add(shiftForClosingEdge);
            polygons.add(poly);
        }
        buildingPoints.clear();
        buildingEdgeShiftFlags.clear();
        redrawAll();
    }

    private int[] snapEndpoint(int x1, int y1, int x2, int y2) {
        int dx = x2 - x1;
        int dy = y2 - y1;
        double angle = Math.toDegrees(Math.atan2(Math.abs(dy), Math.abs(dx)));
        if (angle < 22.5) {
            return new int[]{x2, y1};
        } else if (angle < 67.5) {
            int sgnY = Integer.signum(dy == 0 ? 1 : dy);
            return new int[]{x2, y1 + sgnY * Math.abs(dx)};
        } else if (angle < 112.5) {
            return new int[]{x1, y2};
        } else {
            return new int[]{x2, y2};
        }
    }

    private void drawStatic() {
        for (int i = 0; i < lines.size(); i++) {
            Line L = lines.get(i);
            boolean withShift = lineShiftFlags.get(i);
            LineRasterizer r = withShift ? shifted : lineRasterizer;
            r.rasterize(L.getX1(), L.getY1(), L.getX2(), L.getY2());
        }
        for (DrawnPolygon p : polygons) {
            if (p.points.size() < 2) continue;
            for (int i = 0; i < p.points.size() - 1; i++) {
                Point a = p.points.get(i);
                Point b = p.points.get(i + 1);
                boolean edgeShift = i < p.edgeShiftFlags.size() ? p.edgeShiftFlags.get(i) : false;
                LineRasterizer r = edgeShift ? shifted : lineRasterizer;
                r.rasterize(a.getX(), a.getY(), b.getX(), b.getY());
            }
            Point last = p.points.get(p.points.size() - 1);
            Point first = p.points.get(0);
            boolean closeShift = !p.edgeShiftFlags.isEmpty() ? p.edgeShiftFlags.get(p.edgeShiftFlags.size() - 1) : false;
            LineRasterizer r = closeShift ? shifted : lineRasterizer;
            r.rasterize(last.getX(), last.getY(), first.getX(), first.getY());
        }
    }

    private void drawBuildingPolygonEdges() {
        if (buildingPoints.size() < 2) return;
        for (int i = 0; i < buildingPoints.size() - 1; i++) {
            Point a = buildingPoints.get(i);
            Point b = buildingPoints.get(i + 1);
            boolean edgeShift = i < buildingEdgeShiftFlags.size() && buildingEdgeShiftFlags.get(i);
            LineRasterizer r = edgeShift ? shifted : lineRasterizer;
            r.rasterize(a.getX(), a.getY(), b.getX(), b.getY());
        }
    }

    private void redrawAll() {
        panel.getRaster().clear();
        drawStatic();
        drawBuildingPolygonEdges();
        panel.repaint();
    }
}

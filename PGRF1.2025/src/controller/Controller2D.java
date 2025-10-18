package controller;


import model.Line;
import model.Point;
import rasterize.LineRasterizer;
import rasterize.LineRasterizerGraphics;
import view.Panel;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

public class Controller2D {

    private LineRasterizer lineRasterizer;
    private int color = 0xff0000;
    private final Panel panel;

    Point firstPoint = null;
    Point secondPoint;

    private ArrayList<Line> lines = new ArrayList<Line>();

    public Controller2D(Panel panel) {
        this.panel = panel;
        initListeners();
        lineRasterizer = new LineRasterizerGraphics(panel.getRaster());
        //lineRasterizer = new LineRasterizerTrivial(panel.getRaster());
    }


    private void initListeners() {

        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                panel.getRaster().setPixel(e.getX(), e.getY(), color);
                panel.repaint();
            }
        });

        panel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                if (e.getX() < panel.getWidth() && e.getX() > 0 && e.getY() < panel.getHeight() && e.getY() > 0) {
                    panel.getRaster().clear();
                    lineRasterizer.rasterize(panel.getWidth() / 2, panel.getHeight() / 2, e.getX(), e.getY());
                    panel.repaint();
                }
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
                    int x1 = panel.getWidth() / 2;
                    int x2 = panel.getWidth();
                    int y = panel.getHeight() / 2;

                    while (x1 != x2) {
                        panel.getRaster().setPixel(x1, y, color);
                        x1++;
                    }
                    panel.repaint();
                }
            }
        });

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_O) {
                    color = 0xff0000;
                }
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    color = 0x00ff00;
                }
            }
        });

        panel.getRaster().clear();
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (firstPoint == null) {
                    firstPoint = new Point(e.getX(), e.getY());
                } else {
                    secondPoint = new Point(e.getX(), e.getY());
                    lines.add(new Line(firstPoint, secondPoint));
                    firstPoint = null;
                }
                for (Line line : lines) {
                    lineRasterizer.rasterize(line.getX1(), line.getY1(), line.getX2(), line.getY2());
                }
                panel.repaint();
            }
        });
    }

}

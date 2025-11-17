package org.rezrov;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

import javax.swing.JPanel;

public class TileCanvas extends JPanel {

    public TileCanvas(int rows, int cols) {
        if (rows < 1 || cols < 1) {
            throw new IllegalArgumentException("TileCanvas rows and columns must each be at least 1");
        }
        if (rows > 100 || cols > 100) {
            throw new IllegalArgumentException("TileCanvas rows and columns can't be more than 100");
        }
        _rows = rows;
        _cols = cols;
        _cellContents = new Cell[rows][cols];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                _cellContents[i][j] = new Cell();
            }
        }
    }

    // Set up the graphics transform so that 0, 0 is the top left of the rendering
    // area and each cell is 1x1. Returns the pixel size of a cell.
    private void setUpTransform(Graphics2D g) {
        Dimension size = getSize();
        double aspectRatio = size.width / (double) size.height;
        double desiredAspectRatio = _cols / (double) _rows;

        // Account for some floating point arithmetic slop. If the
        // aspect ratios are very close, they are probably actually identical.
        // (And if they aren't identical, they are close enough that not making
        // a translation adjustement should be fine anyways)
        Dimension usedSize = new Dimension(size);
        if (Math.abs(aspectRatio - desiredAspectRatio) > .0001) {
            if (desiredAspectRatio > aspectRatio) {
                usedSize.height = (int) Math.round(size.width / desiredAspectRatio);
            } else {
                usedSize.width = (int) Math.round(size.height * desiredAspectRatio);
            }
            g.translate((size.width - usedSize.width) / 2.0, (size.height - usedSize.height) / 2.0);
        }

        // Scale so that each cell is 1x1.
        double scale = usedSize.width / (double) _cols;
        g.scale(scale, scale);
    }

    @Override
    public void paintComponent(Graphics gr) {
        Graphics2D g = (Graphics2D) gr;

        g.setColor(Color.BLACK);
        g.fillRect(0, 0, getSize().width, getSize().height);

        setUpTransform(g);
        g.setColor(_backgroundColor);
        g.fillRect(0, 0, _cols, _rows);

        synchronized (this) { // Synchronzied for access to cell contents.
            for (int r = 0; r < _rows; ++r) {
                for (int c = 0; c < _cols; ++c) {
                    if (_cellContents[r][c].tile != null) {
                        AffineTransform savedTransform = g.getTransform();
                        g.translate(c, r);
                        g.rotate(_cellContents[r][c].rotation, 0.5, 0.5);
                        _cellContents[r][c].tile.draw(g, new Rectangle2D.Double(0, 0, 1, 1));
                        g.setTransform(savedTransform);
                    }
                }
            }
        }
    }

    public void setTile(int row, int col, Tile tile) {
        setTile(row, col, tile, 0);
    }

    synchronized public void setTile(int row, int col, Tile tile, double rotationDegrees) {
        _cellContents[row][col].tile = tile;
        _cellContents[row][col].rotation = Math.toRadians(rotationDegrees);
        scheduleRepaint();
    }

    private void scheduleRepaint() {
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                TileCanvas.this.repaint();
            }
        });
    }

    public void clearTile(int row, int col) {
        setTile(row, col, null, 0);
    }

    private class Cell {
        Tile tile = null;
        double rotation = 0;
    }

    private Cell[][] _cellContents;

    private int _rows;
    private int _cols;

    private Color _backgroundColor = Color.WHITE;

}

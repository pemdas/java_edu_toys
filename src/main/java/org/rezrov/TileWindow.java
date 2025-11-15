package org.rezrov;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;

public class TileWindow {
    static final int QUIT_POLL_INTERVAL_MS = 50;

    // The longer dimension of the internal panel will have a minimum size of this
    // many pixels.
    static final int MIN_DIMENSION = 400;

    private class KeyHandler extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            char c = e.getKeyChar();
            if (!pressedKeys.contains(c)) {
                addInput(c);
                pressedKeys.add(c);
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            pressedKeys.remove(e.getKeyChar());
        }

        private HashSet<Character> pressedKeys = new HashSet<Character>();
    }

    private class DrawCanvas extends JPanel {

        // Set up the graphics transform so that 0, 0 is the top left of the rendering
        // area and each cell is 1x1. Returns the pixel size of a cell.
        private void setUpTransform(Graphics2D g) {
            Dimension size = getSize();
            double aspectRatio = size.width / (double) size.height;
            double desiredAspectRatio = cols / (double) rows;

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
            double scale = usedSize.width / (double) cols;
            g.scale(scale, scale);
        }

        @Override
        public void paintComponent(Graphics gr) {
            Graphics2D g = (Graphics2D) gr;

            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getSize().width, getSize().height);

            setUpTransform(g);
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, cols, rows);

            g.setColor(Color.RED);
            for (int r = 0; r < rows; ++r) {
                for (int c = 0; c < cols; ++c) {
                    if (_cellContents[r][c].tile != null) {
                        AffineTransform savedTransform = g.getTransform();

                        /*
                         * g.translate(-c + 0.5, -r + 0.5);
                         * g.rotate(Math.PI / 4);
                         * g.translate(c - 0.5, r - 0.5);
                         */
                        // g.translate(c + 0.5, r + 0.5);
                        g.translate(c, r);
                        g.rotate(_cellContents[r][c].rotation, 0.5, 0.5);
                        // g.setColor(Color.RED);
                        // g.fill(new Rectangle2D.Double(0, 0, 0.5, 0.5));
                        // g.setColor(Color.GREEN);
                        // g.fill(new Rectangle2D.Double(0, 0.5, 0.5, 0.5));
                        // g.setColor(Color.BLUE);
                        // g.fill(new Rectangle2D.Double(0.5, 0, 0.5, 0.5));
                        // g.setColor(Color.YELLOW);
                        // g.fill(new Rectangle2D.Double(0.5, 0.5, 0.5, 0.5));
                        // g.fillRect(0, 0, 1, 1);
                        _cellContents[r][c].tile.draw(g, new Rectangle2D.Double(0, 0, 1, 1));
                        g.setTransform(savedTransform);
                    }
                }
            }
        }

    }

    public TileWindow() {
        this(10, 10);
    }

    public TileWindow(int rows, int cols) {
        if (rows < 1 || cols < 1) {
            throw new IllegalArgumentException("TileWindow rows and columns must each be at least 1");
        }
        if (rows > 100 || cols > 100) {
            throw new IllegalArgumentException("TileWindow rows and columns can't be more than 100");
        }
        this.rows = rows;
        this.cols = cols;
        _cellContents = new Cell[rows][cols];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                _cellContents[i][j] = new Cell();
            }
        }
        creator = Thread.currentThread();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        quitPoll = new Timer(QUIT_POLL_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (!creator.isAlive()) {
                    if (verbosity > 0) {
                        System.out.println("Detected main thread exit.  Closing TileWindow.");
                    }
                    window.dispose();
                    quitPoll.stop();
                }
            }
        });
        quitPoll.start();

    }

    public int loadImage(String filename) {
        _images.add(new MipMap2D(filename));
        return _images.size() - 1;
    }

    private void createAndShowGUI() {
        // Create and set up the window.
        window = new JFrame("Arena v2");
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension minDim = new Dimension();
        if (rows >= cols) {
            minDim.height = MIN_DIMENSION;
            minDim.width = (int) Math.round(MIN_DIMENSION * cols / (double) rows);
        } else {
            minDim.width = MIN_DIMENSION;
            minDim.height = (int) Math.round(MIN_DIMENSION * rows / (double) cols);
        }

        canvas = new DrawCanvas();
        canvas.setPreferredSize(minDim);
        window.setContentPane(canvas);

        window.pack();
        window.setMinimumSize(window.getSize());
        window.addKeyListener(new KeyHandler());

        window.setVisible(true);

    }

    public void setVerbosity(int level) {
        verbosity = level;
    }

    private void addInput(char c) {
        synchronized (inputBuffer) {
            inputBuffer.add(c);
            inputBuffer.notify();
        }
    }

    public void setTile(int row, int col, Tile tile) {
        setTile(row, col, tile, 0);
    }

    public void setTile(int row, int col, Tile tile, double rotationDegrees) {
        _cellContents[row][col].tile = tile;
        _cellContents[row][col].rotation = Math.toRadians(rotationDegrees);
    }

    public void clearTile(int row, int col) {
        _cellContents[row][col].tile = null;
    }

    public char nextInput() {
        synchronized (inputBuffer) {
            if (inputBuffer.isEmpty()) {
                try {
                    inputBuffer.wait();
                } catch (InterruptedException e) {
                    System.err.println("Interrupted");
                    System.exit(-1);
                }
            }
            return inputBuffer.remove();
        }
    }

    private Queue<Character> inputBuffer = new ArrayDeque<Character>();

    private class Cell {
        Tile tile = null;
        double rotation = 0;
    }

    private Cell[][] _cellContents;

    private ArrayList<MipMap2D> _images = new ArrayList<MipMap2D>();

    private int rows;
    private int cols;

    private JFrame window;
    private DrawCanvas canvas;
    private Thread creator;
    private Timer quitPoll;
    private int verbosity = 0;
}

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
            g.setColor(Color.WHITE);
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
        this._rows = rows;
        this._cols = cols;
        _cellContents = new Cell[rows][cols];
        for (int i = 0; i < rows; ++i) {
            for (int j = 0; j < cols; ++j) {
                _cellContents[i][j] = new Cell();
            }
        }
        _creator = Thread.currentThread();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI();
            }
        });
        _quitPollTimer = new Timer(QUIT_POLL_INTERVAL_MS, new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (!_creator.isAlive()) {
                    if (_verbosity > 0) {
                        System.out.println("Detected main thread exit.  Closing TileWindow.");
                    }
                    _window.dispose();
                    _quitPollTimer.stop();
                }
            }
        });
        _quitPollTimer.start();

    }

    private void createAndShowGUI() {
        // Create and set up the window.
        _window = new JFrame("Arena v2");
        _window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension minDim = new Dimension();
        if (_rows >= _cols) {
            minDim.height = MIN_DIMENSION;
            minDim.width = (int) Math.round(MIN_DIMENSION * _cols / (double) _rows);
        } else {
            minDim.width = MIN_DIMENSION;
            minDim.height = (int) Math.round(MIN_DIMENSION * _rows / (double) _cols);
        }

        _canvas = new DrawCanvas();
        _canvas.setPreferredSize(minDim);
        _window.setContentPane(_canvas);

        _window.pack();
        _window.setMinimumSize(_window.getSize());
        _window.addKeyListener(new KeyHandler());

        _window.setVisible(true);

    }

    public void setVerbosity(int level) {
        _verbosity = level;
    }

    private void addInput(char c) {
        synchronized (_inputBuffer) {
            _inputBuffer.add(c);
            _inputBuffer.notify();
        }
    }

    public void setTile(int row, int col, Tile tile) {
        setTile(row, col, tile, 0);
    }

    synchronized public void setTile(int row, int col, Tile tile, double rotationDegrees) {
        _cellContents[row][col].tile = tile;
        _cellContents[row][col].rotation = Math.toRadians(rotationDegrees);
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                _window.getContentPane().repaint();
            }
        });
    }

    public void clearTile(int row, int col) {
        setTile(row, col, null, 0);
    }

    public char nextInput() {
        synchronized (_inputBuffer) {
            if (_inputBuffer.isEmpty()) {
                try {
                    _inputBuffer.wait();
                } catch (InterruptedException e) {
                    System.err.println("Interrupted");
                    System.exit(-1);
                }
            }
            return _inputBuffer.remove();
        }
    }

    private class Cell {
        Tile tile = null;
        double rotation = 0;
    }

    private Cell[][] _cellContents;

    private Queue<Character> _inputBuffer = new ArrayDeque<Character>();

    private int _rows;
    private int _cols;

    private JFrame _window;
    private DrawCanvas _canvas;
    private Thread _creator;

    // Timer used to poll for whether the creating thread has exited.
    private Timer _quitPollTimer;
    private int _verbosity = 0;
}

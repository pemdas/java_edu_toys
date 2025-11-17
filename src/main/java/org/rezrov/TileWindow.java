package org.rezrov;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;

import javax.swing.JFrame;
import javax.swing.Timer;

public class TileWindow {
    static final int QUIT_POLL_INTERVAL_MS = 50;

    // The longer dimension of the internal panel will have a minimum size of this
    // many pixels.
    static final int MIN_DIMENSION = 800;

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

    public TileWindow(String windowTitle) {
        this(windowTitle, 11, 21);
    }

    public TileWindow(String windowTitle, int rows, int cols) {
        _creator = Thread.currentThread();
        javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                createAndShowGUI(windowTitle, rows, cols);
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

    private void createAndShowGUI(String windowTitle, int rows, int cols) {
        // Create and set up the window.
        _window = new JFrame(windowTitle);
        _window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        Dimension minDim = new Dimension();
        if (rows >= cols) {
            minDim.height = MIN_DIMENSION;
            minDim.width = (int) Math.round(MIN_DIMENSION * cols / (double) rows);
        } else {
            minDim.width = MIN_DIMENSION;
            minDim.height = (int) Math.round(MIN_DIMENSION * rows / (double) cols);
        }

        _canvas = new TileCanvas(rows, cols);
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
        _canvas.setTile(row, col, tile);
    }

    synchronized public void setTile(int row, int col, Tile tile, double rotationDegrees) {
        _canvas.setTile(row, col, tile, rotationDegrees);
    }

    public void clearTile(int row, int col) {
        _canvas.clearTile(row, col);
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

    private Queue<Character> _inputBuffer = new ArrayDeque<Character>();

    private JFrame _window;
    private TileCanvas _canvas;
    private Thread _creator;

    // Timer used to poll for whether the creating thread has exited.
    private Timer _quitPollTimer;
    private int _verbosity = 0;
}

package org.rezrov;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.Queue;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.Timer;

public class SoccerGameWindow {
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

    public SoccerGameWindow(String windowTitle) {
        this(windowTitle, 11, 21);
    }

    public SoccerGameWindow(String windowTitle, int rows, int cols) {
        _creator = Thread.currentThread();

        synchronized (this) {
            javax.swing.SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    createAndShowGUI(windowTitle, rows, cols);
                }
            });

            _quitPollTimer = new Timer(QUIT_POLL_INTERVAL_MS, new ActionListener() {
                public void actionPerformed(ActionEvent event) {
                    if (!_creator.isAlive()) {
                        if (_verbosity > 0) {
                            System.out.println("Detected main thread exit.  Closing SoccerGameWindow.");
                        }
                        _window.dispose();
                        _quitPollTimer.stop();
                    }
                }
            });
            _quitPollTimer.start();
            try {
                // This will hold execution of this thread until the createAndShowGUI call
                // finishes. That way, the calling thread gets to pretend there's no threading
                // going on here; when the SoccerGameWindow constructor returns, the methods
                // are safe to use.
                wait();
            } catch (InterruptedException ie) {
                throw new IllegalStateException(ie);
            }
        }
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
        // Make the background green.
        _canvas.setBackgroundColor(new Color(16, 126, 0));
        _canvas.setPreferredSize(minDim);

        Container pane = _window.getContentPane();
        pane.setLayout(new BoxLayout(pane, BoxLayout.PAGE_AXIS));
        _scoreLabel = new JLabel("0 - 0");
        _scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        _scoreLabel.setMaximumSize(new Dimension(100000, 50));
        _scoreLabel.setBackground(Color.WHITE);
        _scoreLabel.setOpaque(true);
        _scoreLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        _scoreLabel.setFont(_scoreLabel.getFont().deriveFont(30.f));
        pane.add(_scoreLabel);
        pane.add(_canvas);

        _window.pack();
        _window.setMinimumSize(_window.getSize());
        _window.addKeyListener(new KeyHandler());

        _window.setVisible(true);
        synchronized (this) {
            // Now that the gui is constructed, we can let the SoccerGameWindow construction
            // complete.
            notify();
        }
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

    public void showGrid(boolean val) {
        _canvas.showGrid(val);
    }

    public char nextInput() {
        synchronized (_inputBuffer) {
            if (_inputBuffer.isEmpty()) {
                try {
                    _inputBuffer.wait();
                } catch (InterruptedException e) {
                    throw new IllegalStateException(e);
                }
            }
            return _inputBuffer.remove();
        }
    }

    public void setScore(int left, int right) {
        _scoreLabel.setText(String.format("%d - %d", left, right));
    }

    public void setBackgroundColor(int red, int green, int blue) {
        _canvas.setBackgroundColor(new Color(red, green, blue));, 
    }

    private Queue<Character> _inputBuffer = new ArrayDeque<Character>();

    private JFrame _window;
    private JLabel _scoreLabel;
    private TileCanvas _canvas;
    private Thread _creator;

    // Timer used to poll for whether the creating thread has exited.
    private Timer _quitPollTimer;
    private int _verbosity = 0;
}

package org.rezrov;

import javax.imageio.ImageIO;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.Timer;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.awt.image.BufferedImage;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.KeyAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.Queue;
import java.util.Map;
import java.util.TreeMap;
import java.util.ArrayDeque;

public class Draw {
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
        private double setUpTransform(Graphics2D g) {
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
            g.scale(usedSize.width / (double) cols, usedSize.height / (double) rows);
            return usedSize.width / (double) cols;
        }

        @Override
        public void paintComponent(Graphics gr) {
            Graphics2D g = (Graphics2D) gr;
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, getSize().width, getSize().height);
            int mipmapSize = (int) Math.ceil(setUpTransform(g));
            g.setColor(Color.WHITE);
            g.fillRect(0, 0, cols, rows);

            for (int r = 0; r < rows; ++r) {
                for (int c = 0; c < cols; ++c) {
                    if (cellContents[r][c] != -1) {
                        Image img = images.get(cellContents[r][c]).get(mipmapSize);
                        double s = 1.0 / img.getWidth(this);
                        AffineTransform t = AffineTransform.getScaleInstance(s, s);
                        g.drawImage(img, t, this);
                    }
                }
            }

        }

    }

    public Draw() {
        this(10, 10);
    }

    public Draw(int rows, int cols) {
        if (rows < 1 || cols < 1) {
            System.err.println("Drawing area rows and columns must be at least 1");
            System.exit(-1);
        }
        if (rows > 100 || cols > 100) {
            System.err.println("Drawing area rows and columns can't be more than 100");
            System.exit(-1);
        }
        this.rows = rows;
        this.cols = cols;
        cellContents = new int[rows][cols];
        for (int i = 0; i < rows; ++i) {
            Arrays.fill(cellContents[i], -1);
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
                        System.out.println("Detected main thread exit.  Shutting down.");
                    }
                    window.dispose();
                    quitPoll.stop();
                }
            }
        });
        quitPoll.start();

    }

    public int loadImage(String filename) {
        images.add(new MipMappedImage(filename));
        return images.size() - 1;
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

    public void setCell(int row, int col, int id) {
        cellContents[row][col] = id;
    }

    public void clearCell(int row, int col) {
        cellContents[row][col] = -1;
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

    // static int maxDimension()

    class MipMappedImage {
        final int MAX_RESOLUTION = 256;
        final int SCALING_HINT = Image.SCALE_AREA_AVERAGING;

        MipMappedImage(String filename) {
            BufferedImage loadedImage = null;
            try {
                File pathToFile = new File(filename);
                loadedImage = ImageIO.read(pathToFile);
            } catch (IOException ex) {
                System.err.println("Couldn't load " + filename);
                System.exit(-1);
            }
            int w = loadedImage.getWidth();
            int h = loadedImage.getHeight();

            if (w == 0 || h == 0) {
                System.err.println("Image has no contents: " + filename);
                System.exit(-1);
            }

            int size = Math.max(w, h);
            if (w != h) {
                // Make it square by adding transparent pixels on each side.
                BufferedImage tmp = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
                Graphics2D g = tmp.createGraphics();
                // Fill with transparent.
                g.setColor(new Color(0, 0, 0, 0));
                g.fillRect(0, 0, size, size);
                if (w > h) {
                    g.translate(0, (size - h) / 2.0);
                } else {
                    g.translate((size - w) / 2.0, 0);
                }
                // Might be better to avoid the half-pixel in the odd case, not sure.
                g.drawImage(loadedImage, new AffineTransform(), null);
                loadedImage = tmp;
            }

            Image mipmap;
            // If someone gives us an absurdly large graphic, scale it down.
            if (size > MAX_RESOLUTION) {
                mipmap = loadedImage.getScaledInstance(MAX_RESOLUTION,
                        MAX_RESOLUTION, SCALING_HINT);
                size = MAX_RESOLUTION;
            } else {
                mipmap = loadedImage;
            }

            while (true) {
                mipmaps.put(size, mipmap);

                // This gives is the first power of two which is less than size.
                size = Integer.highestOneBit(size - 1);
                if (size == 0) {
                    break;
                }
                mipmap = mipmap.getScaledInstance(size, size, SCALING_HINT);
            }
        }

        // Get the smallest mipmap which is at least cellSize.
        public Image get(int cellSize) {
            assert !mipmaps.isEmpty();
            Map.Entry<Integer, Image> entry = mipmaps.ceilingEntry(cellSize);
            if (entry == null) {
                entry = mipmaps.lastEntry();
            }
            return entry.getValue();
        }

        private TreeMap<Integer, Image> mipmaps = new TreeMap<Integer, Image>();
    }

    private Queue<Character> inputBuffer = new ArrayDeque<Character>();

    private int[][] cellContents;

    private ArrayList<MipMappedImage> images = new ArrayList<MipMappedImage>();

    private int rows;
    private int cols;

    private JFrame window;
    private DrawCanvas canvas;
    private Thread creator;
    private Timer quitPoll;
    private int verbosity = 1;
}

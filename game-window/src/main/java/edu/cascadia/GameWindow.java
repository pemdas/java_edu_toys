package edu.cascadia;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class GameWindow {

   // -------------------------------------------------------------------------
   // Public sprite type
   // -------------------------------------------------------------------------

   private static final Map<String, BufferedImage> _imageCache = new HashMap<>();

   // -------------------------------------------------------------------------
   // Built-in sprite constants
   // -------------------------------------------------------------------------

   public static final BufferedImage RED_SHIP = loadResource("red_ufo.png");
   public static final BufferedImage BLUE_SHIP = loadResource("blue_ufo.png");
   // public static final BufferedImage STAR = loadResource("star.png");
   public static final BufferedImage FLAME = loadResource("flame1.png");
   public static final BufferedImage FLAME2 = loadResource("flame2.png");
   public static final BufferedImage FLAME3 = loadResource("flame3.png");
   public static final BufferedImage FLAME4 = loadResource("flame4.png");

   public static final BufferedImage PLANET = loadResource("earth_100px.png");
   public static final BufferedImage BLACK_HOLE = loadResource("black_hole_100px.png");
   public static final BufferedImage ASTEROID = loadResource("asteroid.png");
   public static final BufferedImage SATELLITE = loadResource("satellite.png");

   // -------------------------------------------------------------------------
   // Factory for custom sprites
   // -------------------------------------------------------------------------

   /** Load a built-in sprite image bundled in the jar. */
   private static BufferedImage loadResource(String name) {
      try (InputStream in = GameWindow.class.getResourceAsStream(name)) {
         if (in == null)
            throw new IOException("Built-in resource not found: " + name);
         return ImageIO.read(in);
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   /** Read an image from a file, caching the result. */
   public static BufferedImage readImage(String filename) {
      try {
         String path = new File(filename).getCanonicalPath();
         synchronized (_imageCache) {
            return _imageCache.computeIfAbsent(path, k -> {
               try {
                  return ImageIO.read(new File(filename));
               } catch (IOException e) {
                  throw new UncheckedIOException(e);
               }
            });
         }
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   // -------------------------------------------------------------------------
   // Constructor
   // -------------------------------------------------------------------------

   /**
    * Create and display the game window.
    *
    * @param width  width of the game window in pixels
    * @param height height of the game window in pixels
    */
   public GameWindow(String windowTitle, double width, double height) {
      if (width < 200 || width > 2000 || height < 200 || height > 2000) {
         throw new IllegalArgumentException(
               "Bad GameWindow size.  Please use heights and widths in the range [200, 2000]");
      }
      _windowTitle = windowTitle;
      _width = width;
      _height = height;
      _lastFrameNs = System.nanoTime();

      synchronized (this) {
         try {
            SwingUtilities.invokeAndWait(this::createAndShowGUI);
         } catch (Exception e) {
            throw new IllegalStateException(e);
         }
      }
   }

   // -------------------------------------------------------------------------
   // Public API
   // -------------------------------------------------------------------------

   /**
    * Add a sprite to the current frame's draw queue, centered at (x, y).
    * The origin (0, 0) is the top-left corner of the window; positive x is
    * right and positive y is down (standard screen coordinates).
    * All queued sprites are rendered together on the next call to nextFrame().
    */
   public void addSprite(double x, double y, BufferedImage sprite) {
      addSprite(x, y, new AffineTransform(), sprite);
   }

   public void addSprite(double x, double y, BufferedImage sprite, double rotationDegrees) {
      // AffineTransform.getRotateInstance
      addSprite(x, y, AffineTransform.getRotateInstance(Math.toRadians(rotationDegrees)), sprite);
   }

   /**
    * 
    * Queue a sprite to be drawn. The sprite is centered at (x, y), but transform
    * is applied to the translated position.
    * before rendering.
    */
   public void addSprite(double x, double y, AffineTransform transform, BufferedImage sprite) {
      synchronized (_drawQueue) {
         AffineTransform finalTransform = AffineTransform.getTranslateInstance(x, y);
         finalTransform.concatenate(transform);
         finalTransform.translate(-sprite.getWidth() / 2.0, -sprite.getHeight() / 2.0);
         // finalTransform.concatenate(transform);
         // finalTransform.translate(x, y);
         _drawQueue.add(new SpriteEntry(sprite, finalTransform));
      }
   }

   /**
    * Display all sprites queued since the last call, sleep to maintain ~60 fps,
    * then clear the sprite list for the next frame.
    * Call once per game-loop iteration.
    */
   public void nextFrame() {
      // Snapshot the sprite list into _lastRenderedCommands and clear the working
      // list. paintComponent always reads from _lastRenderedCommands, so any
      // Swing-initiated repaint (now or later) re-draws this frame correctly.
      synchronized (_drawQueue) {
         _lastRenderedCommands = new ArrayList<>(_drawQueue);
         _drawQueue.clear();
      }

      try {
         SwingUtilities.invokeAndWait(() -> _canvas.paintImmediately(0, 0, _canvas.getWidth(), _canvas.getHeight()));
      } catch (Exception ignored) {
      }

      // Sleep for the remainder of the frame budget.
      long nowNs = System.nanoTime();
      long targetNs = _lastFrameNs + FRAME_NS;
      long sleepNs = targetNs - nowNs;
      if (sleepNs > 0) {
         try {
            Thread.sleep(sleepNs / 1_000_000L, (int) (sleepNs % 1_000_000L));
         } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
         }
      }
      _lastFrameNs = (sleepNs > 0) ? targetNs : nowNs;
   }

   /**
    * Return true if the given key is currently held down.
    * Does not block; call each frame to check the current keyboard state.
    *
    * Valid key names: "left", "right", "up", "down", "space", "shift",
    * "control", "alt", and single letters "a" through "z".
    *
    * Example: window.isKeyDown("right")
    *
    * @throws IllegalArgumentException if keyName is not a recognized key name
    */
   public boolean isKeyDown(String keyName) {
      if (!VALID_KEY_NAMES.contains(keyName)) {
         throw new IllegalArgumentException(
               "Unrecognized key: \"" + keyName + "\".  " +
                     "Key names are lowercase strings like \"left\", \"right\", \"a\", \"space\".");
      }
      synchronized (_pressedKeys) {
         return _pressedKeys.contains(keyName);
      }
   }

   /**
    * Return the mouse position in window coordinates, relative to the top-left
    * corner of the canvas. Works even when the cursor is outside the window.
    * Positive x is right, positive y is down.
    */
   public Point2D.Double mousePosition() {
      Point screen = MouseInfo.getPointerInfo().getLocation();
      SwingUtilities.convertPointFromScreen(screen, _canvas);
      return new Point2D.Double(screen.x, screen.y);
   }

   /** Return true if the left mouse button is currently held down. */
   public boolean isLeftMouseButtonDown() {
      synchronized (_pressedMouseButtons) {
         return _pressedMouseButtons.contains(MouseEvent.BUTTON1);
      }
   }

   /** Return true if the right mouse button is currently held down. */
   public boolean isRightMouseButtonDown() {
      synchronized (_pressedMouseButtons) {
         return _pressedMouseButtons.contains(MouseEvent.BUTTON3);
      }
   }

   /** Return true if the game window currently has keyboard/input focus. */
   public boolean isWindowFocused() {
      return _windowFocused;
   }

   public void dispose() {
      _window.dispose();
   }

   /**
    * Set the background color used to clear the screen each frame.
    * The default is black.
    * Call before the game loop or between frames.
    */
   public void setBackgroundColor(Color color) {
      _backgroundColor = color;
   }

   /**
    * Add a filled circle to the current frame's draw queue, centered at (x, y).
    * The circle is drawn in the given color with the specified radius in pixels.
    */
   public void addCircle(double x, double y, double radius, Color color) {
      synchronized (_drawQueue) {
         _drawQueue.add(new CircleEntry(x, y, radius, color));
      }
   }
   // -------------------------------------------------------------------------
   // Private: GUI construction
   // -------------------------------------------------------------------------

   private void createAndShowGUI() {
      _window = new JFrame(_windowTitle);
      _window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      _canvas = new GameCanvas();
      _canvas.setPreferredSize(new Dimension((int) _width, (int) _height));

      _window.add(_canvas);
      _window.pack();
      _window.setMinimumSize(_window.getSize());
      _window.addKeyListener(new KeyHandler());
      _canvas.addMouseListener(new MouseHandler());
      _window.addWindowFocusListener(new WindowAdapter() {
         @Override public void windowGainedFocus(WindowEvent e) { _windowFocused = true; }
         @Override public void windowLostFocus(WindowEvent e)   { _windowFocused = false; }
      });
      _window.setVisible(true);
      _window.setResizable(false);
   }

   // -------------------------------------------------------------------------
   // Private constants and fields
   // -------------------------------------------------------------------------

   // private static final int QUIT_POLL_MS = 50;
   private static final long FRAME_NS = 1_000_000_000L / 60; // ~60 fps

   private static final Map<Integer, String> KEY_NAME_BY_VK;
   private static final Set<String> VALID_KEY_NAMES;
   static {
      Map<Integer, String> m = new HashMap<>();
      m.put(KeyEvent.VK_LEFT, "left");
      m.put(KeyEvent.VK_RIGHT, "right");
      m.put(KeyEvent.VK_UP, "up");
      m.put(KeyEvent.VK_DOWN, "down");
      m.put(KeyEvent.VK_SHIFT, "shift");
      m.put(KeyEvent.VK_CONTROL, "control");
      m.put(KeyEvent.VK_ALT, "alt");
      m.put(KeyEvent.VK_SPACE, "space");
      for (int vk = KeyEvent.VK_A; vk <= KeyEvent.VK_Z; vk++) {
         m.put(vk, String.valueOf((char) ('a' + vk - KeyEvent.VK_A)));
      }
      for (int vk = KeyEvent.VK_0; vk <= KeyEvent.VK_9; vk++) {
         m.put(vk, String.valueOf((char) ('0' + vk - KeyEvent.VK_0)));
      }
      KEY_NAME_BY_VK = Collections.unmodifiableMap(m);
      VALID_KEY_NAMES = Collections.unmodifiableSet(new HashSet<>(m.values()));
   }

   private final String _windowTitle;
   private final double _width;
   private final double _height;
   // private final Thread _creator;
   private JFrame _window;
   private GameCanvas _canvas;
   // private Timer _quitPollTimer;
   private long _lastFrameNs;
   private volatile Color _backgroundColor = Color.BLACK;

   private final List<DrawCommand> _drawQueue = new ArrayList<>();
   private volatile List<DrawCommand> _lastRenderedCommands = Collections.emptyList();
   private final Set<String> _pressedKeys = new HashSet<>();
   private final Set<Integer> _pressedMouseButtons = new HashSet<>();
   private volatile boolean _windowFocused = false;

   // -------------------------------------------------------------------------
   // Private nested classes
   // -------------------------------------------------------------------------

   private interface DrawCommand {
      void draw(Graphics2D g);
   }

   private static class SpriteEntry implements DrawCommand {
      final AffineTransform transform;
      final BufferedImage image;

      SpriteEntry(BufferedImage image, AffineTransform transform) {
         this.image = image;
         this.transform = transform;
      }

      @Override
      public void draw(Graphics2D g) {
         g.drawImage(image, transform, null);
      }
   }

   private static class CircleEntry implements DrawCommand {
      final double x, y, radius;
      final Color color;

      CircleEntry(double x, double y, double radius, Color color) {
         this.x = x;
         this.y = y;
         this.radius = radius;
         this.color = color;
      }

      @Override
      public void draw(Graphics2D g) {
         g.setColor(color);
         g.fillOval((int) (x - radius), (int) (y - radius),
               (int) (2 * radius), (int) (2 * radius));
      }
   }

   /** Custom panel that paints sprites at their real-valued game coordinates. */
   private class GameCanvas extends JPanel {
      @Override
      protected void paintComponent(Graphics gr) {
         super.paintComponent(gr);
         Graphics2D g = (Graphics2D) gr;
         g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
               RenderingHints.VALUE_ANTIALIAS_ON);
         g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
               RenderingHints.VALUE_INTERPOLATION_BILINEAR);

         g.setColor(_backgroundColor);
         g.fillRect(0, 0, getWidth(), getHeight());

         for (DrawCommand cmd : _lastRenderedCommands) {
            cmd.draw(g);
         }
      }
   }

   private class MouseHandler extends MouseAdapter {
      @Override
      public void mousePressed(MouseEvent e) {
         synchronized (_pressedMouseButtons) {
            _pressedMouseButtons.add(e.getButton());
         }
      }

      @Override
      public void mouseReleased(MouseEvent e) {
         synchronized (_pressedMouseButtons) {
            _pressedMouseButtons.remove(e.getButton());
         }
      }
   }

   private class KeyHandler extends KeyAdapter {
      @Override
      public void keyPressed(KeyEvent e) {
         String name = KEY_NAME_BY_VK.get(e.getKeyCode());
         if (name != null) {
            synchronized (_pressedKeys) {
               _pressedKeys.add(name);
            }
         }
      }

      @Override
      public void keyReleased(KeyEvent e) {
         String name = KEY_NAME_BY_VK.get(e.getKeyCode());
         if (name != null) {
            synchronized (_pressedKeys) {
               _pressedKeys.remove(name);
            }
         }
      }
   }
}

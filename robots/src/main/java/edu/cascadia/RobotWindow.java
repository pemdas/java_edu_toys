package edu.cascadia;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class RobotWindow extends JFrame {

   private JPanel paintPanel;
   private World world;

   public RobotWindow(String title, World world) {
      super(title);
      this.world = world;
      setMinimumSize(new Dimension(400, 400));
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      paintPanel = new JPanel() {
         public void paintComponent(Graphics g) {
            paintRobotPanel((Graphics2D) g);
         }
      };
      getContentPane().add(paintPanel, BorderLayout.CENTER);
      JPanel bottomPanel = new JPanel();
      ImageIcon playIcon = new ImageIcon(loadResource("play_button.png"));

      JButton button = new JButton();
      button.setIcon(playIcon);

      JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 100, 5);
      bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
      bottomPanel.add(button);
      bottomPanel.add(Box.createRigidArea(new Dimension(40, 0)));
      JLabel speedLabel = new JLabel("Speed");
      Font f = loadFont("NotoSans-Light.ttf");
      GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
      speedLabel.setFont(new Font("Noto Sans Light", Font.PLAIN, 18));
      bottomPanel.add(speedLabel);
      bottomPanel.add(slider);

      bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
      getContentPane().add(bottomPanel, BorderLayout.PAGE_END);

      // display it
      pack();

   }

   final static int MARGIN_PX = 10;

   // This is in terms of the width of 1 cell.
   final static float WALL_WIDTH = .07f;
   final static float HALF_WALL_WIDTH = WALL_WIDTH / 2;

   final static Color WALL_COLOR = Color.RED;

   private void paintRobotPanel(Graphics2D g) {
      AffineTransform saved = g.getTransform();

      g.setRenderingHints(new RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON));

      g.setColor(new Color(110, 168, 254));

      g.fillRect(0, 0, paintPanel.getWidth(), paintPanel.getHeight());

      float pW = paintPanel.getWidth() - 2 * MARGIN_PX;
      float pH = paintPanel.getHeight() - 2 * MARGIN_PX;

      // These are the world rendering area, including space for the walls around the
      // outside edges (1/2 wall on each side).
      float wW = world.getWidth() + WALL_WIDTH;
      float wH = world.getHeight() + WALL_WIDTH;

      float cellSize;
      if ((wW / wH) > (pW / pH)) {
         // The world is wider than the panel.
         cellSize = pW / wW;
         g.translate(MARGIN_PX,
               MARGIN_PX + (pH - cellSize * wH) / 2.0);
      } else {
         // The world is taller than the panel.
         cellSize = pH / wH;
         g.translate(MARGIN_PX + (pW - cellSize * wW) / 2.0,
               MARGIN_PX);
      }
      // Adjust for walls
      g.scale(cellSize, cellSize);
      g.setColor(Color.WHITE);

      g.fill(new Rectangle2D.Float(0, 0, wW, wH));
      g.translate(HALF_WALL_WIDTH, HALF_WALL_WIDTH);
      // Set up the transform so each cell is 1 unit wide.

      g.setColor(Color.BLACK);
      g.setStroke(new BasicStroke(WALL_WIDTH));
      // Draw the outer walls.
      g.draw(new Line2D.Float(0, 0, world.getWidth(), 0)); // Top
      g.draw(new Line2D.Float(0, world.getHeight(), world.getWidth(), world.getHeight())); // Bottom
      g.draw(new Line2D.Float(0, 0, 0, world.getHeight())); // Left
      g.draw(new Line2D.Float(world.getWidth(), 0, world.getWidth(), world.getHeight())); // Right

      // g.setStroke(new BasicStroke(WALL_WIDTH, BasicStroke.CAP_ROUND,
      // BasicStroke.JOIN_BEVEL));
      // Draw top walls
      for (int x = 0; x < world.getWidth(); x++) {
         for (int y = 1; y < world.getHeight(); y++) {
            if (world.isFacingWall(new Coord2D(x, y), Direction.UP)) {
               g.draw(new Line2D.Float(x, y, x + 1, y));
            }
         }
      }
      // Draw left walls
      for (int x = 1; x < world.getWidth(); x++) {
         for (int y = 0; y < world.getHeight(); y++) {
            if (world.isFacingWall(new Coord2D(x, y), Direction.LEFT)) {
               g.draw(new Line2D.Float(x, y, x, y + 1));
            }
         }
      }
      // Draw "pillars"
      for (int x = 1; x < world.getWidth(); x++) {
         for (int y = 1; y < world.getHeight(); y++) {
            g.fill(new Ellipse2D.Float(x - WALL_WIDTH, y - WALL_WIDTH, 2 * WALL_WIDTH, 2 * WALL_WIDTH));
         }
      }

      /*
       * g.setColor(Color.RED);
       * g.setStroke(new BasicStroke(WALL_WIDTH_PX));
       * for (int i = 0; i <= wW; i++) {
       * g.draw(new Line2D.Float(WALL_WIDTH_PX / 2 + i * cellSize, WALL_WIDTH_PX / 2,
       * WALL_WIDTH_PX / 2 + i * cellSize,
       * wH * cellSize));
       * }
       * for (int i = 0; i <= wH; i++) {
       * g.draw(new Line2D.Float(WALL_WIDTH_PX / 2, WALL_WIDTH_PX / 2 + i * cellSize,
       * cellSize * wW,
       * WALL_WIDTH_PX / 2 + i * cellSize));
       * }
       */
      g.setTransform(saved);

   }

   private static BufferedImage loadResource(String name) {
      try (InputStream in = RobotWindow.class.getResourceAsStream(name)) {
         if (in == null)
            throw new IOException("Built-in resource not found: " + name);
         return ImageIO.read(in);
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   private static Font loadFont(String name) {
      try (InputStream in = RobotWindow.class.getResourceAsStream(name)) {
         if (in == null)
            throw new IOException("Built-in resource not found: " + name);
         return Font.createFont(Font.TRUETYPE_FONT, in);
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      } catch (FontFormatException e) {
         throw new Error(e.getMessage());
      }
   }

   public static void main(String[] args) {
      World w = new World(3, 4);
      w.addWall(new Coord2D(0, 0), Direction.RIGHT);
      w.addWall(new Coord2D(1, 1), Direction.DOWN);
      new RobotWindow("Robot Land", w).setVisible(true);

   }
}

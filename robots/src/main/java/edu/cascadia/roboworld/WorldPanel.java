package edu.cascadia.roboworld;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;

import javax.swing.JPanel;

/**
 * Swing widget for displaying the state of the World.
 */
public class WorldPanel extends JPanel {

   // Maybe make these configurable, eventually?
   final static float WALL_WIDTH = .07f;
   final static float HALF_WALL_WIDTH = WALL_WIDTH / 2;
   final static Color WALL_COLOR = Color.BLACK;

   // Cache the rendering of the walls to a buffered image. Updated when the size
   // changes.
   private BufferedImage cachedBackgroundImage = null;
   // Transform that sets up the rendering coordinate system such that each cell is
   // 1x1, with the origin in the
   // middle of the wall in the top left of the map.
   private AffineTransform cachedTransform = null;

   final private static AffineTransform IDENTITY_TRANSFORM = new AffineTransform();

   // Color used to fill in bars at the edges when the aspect ratio isn't perfect.
   private Color letterboxColor;

   // Environment and robot we're rendering
   private Environment env;
   private ContinuousRobot robot;

   public WorldPanel(Environment e, ContinuousRobot r, Color letterboxColor) {
      env = e;
      robot = r;
      this.letterboxColor = letterboxColor;
   }

   @Override
   public void paintComponent(Graphics gr) {

      // Shouldn't happen, but be paranoid.
      if (getWidth() == 0 || getHeight() == 0) {
         return;
      }
      Graphics2D g = (Graphics2D) gr;
      AffineTransform savedTransform = g.getTransform();
      Dimension worldSizePx = worldSizePx();
      if (cachedBackgroundImage == null || worldSizePx.height != cachedBackgroundImage.getHeight()
            || worldSizePx.width != cachedBackgroundImage.getWidth()) {
         updateBackgroundImage(worldSizePx);
      }

      if (worldSizePx.getWidth() < getWidth()) {
         // Letterbox left and right edges.
         int leftLetterboxWidth = (getWidth() - worldSizePx.width) / 2;
         if (letterboxColor != null) {
            g.setColor(letterboxColor);
            g.fillRect(0, 0, leftLetterboxWidth, getHeight());
            // This may be 1 pixel different from the left side.
            int rightLetterboxWidth = (getWidth() - leftLetterboxWidth);
            g.fillRect(getWidth() - rightLetterboxWidth, 0, rightLetterboxWidth, getHeight());
         }
         g.translate(leftLetterboxWidth, 0);
      } else if (worldSizePx.height < getHeight()) {
         int topLetterboxHeight = (getHeight() - worldSizePx.height) / 2;
         if (letterboxColor != null) {
            // Letterbox top and bottom.
            g.setColor(letterboxColor);
            g.fillRect(0, 0, getWidth(), topLetterboxHeight);
            // This may be 1 pixel different from the top side.
            int bottomLetterboxHeight = (getHeight() - topLetterboxHeight);
            g.fillRect(0, getHeight() - topLetterboxHeight, getWidth(), bottomLetterboxHeight);
         }
         g.translate(0, topLetterboxHeight);
      }
      g.drawRenderedImage(cachedBackgroundImage, IDENTITY_TRANSFORM);
      g.transform(cachedTransform);
      Pose2D robotPose = robot.getPose();
      drawSprite(g, Resources.ROBOT_SPRITE, .6, robotPose.x, robotPose.y, robotPose.heading);
      g.setTransform(savedTransform);
   }

   // Draw the given sprite to g. g should be set up with the world transform
   // (origin at top left, cell size is 1 unit).
   // spriteCellSize is the size we want to render the sprite in terms of a cell
   // length. The longer dimension of the sprite
   // will be scaled to this.
   //
   // Rotation is clockwise, and in radians.
   static private void drawSprite(Graphics2D g, RenderedImage sprite, double spriteCellSize, double centerX,
         double centerY,
         double rotation) {
      AffineTransform saved = g.getTransform();
      double scale = spriteCellSize / Math.max(sprite.getHeight(), sprite.getWidth());
      double cellWidth = scale * sprite.getWidth();
      double cellHeight = scale * sprite.getHeight();
      g.translate(centerX - cellWidth / 2.0, centerY - cellHeight / 2.0);
      g.rotate(rotation, cellWidth / 2.0, cellHeight / 2.0);
      g.drawRenderedImage(sprite, AffineTransform.getScaleInstance(scale, scale));
      g.setTransform(saved);
   }

   // Return the pixel dimensions we'll use to render the env.
   private Dimension worldSizePx() {
      float worldRenderWidth = env.getWidth() + WALL_WIDTH;
      float worldRenderHeight = env.getHeight() + WALL_WIDTH;
      float worldRenderAspectRatio = worldRenderWidth / worldRenderHeight;
      float panelAspectRatio = getWidth() / (float) getHeight();
      Dimension ret = new Dimension();
      if (worldRenderAspectRatio > panelAspectRatio) {
         ret.width = getWidth();
         ret.height = Math.round(getWidth() / worldRenderAspectRatio);
      } else {
         ret.height = getHeight();
         ret.width = Math.round(getHeight() * worldRenderAspectRatio);
      }
      return ret;
   }

   private void updateBackgroundImage(Dimension worldSizePx) {
      int hPx = worldSizePx.height;
      int wPx = worldSizePx.width;
      cachedBackgroundImage = new BufferedImage(wPx, hPx,
            BufferedImage.TYPE_INT_RGB);

      Graphics2D g = cachedBackgroundImage.createGraphics();
      g.setColor(Color.WHITE);
      g.fillRect(0, 0, wPx, hPx);

      g.setRenderingHints(new RenderingHints(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON));

      // Due to the canvas area being rounded to the nearest pixel, the cell size may
      // not be precisely the same in the
      // horizontal and vertical directions, but it should be close enough that any
      // distortion is unnoticeable.
      float hCellSize = (float) (wPx / (env.getWidth() + WALL_WIDTH));
      float vCellSize = (float) (hPx / (env.getHeight() + WALL_WIDTH));

      g.scale(hCellSize, vCellSize);
      g.translate(HALF_WALL_WIDTH, HALF_WALL_WIDTH);

      g.setColor(WALL_COLOR);
      g.setStroke(new BasicStroke(WALL_WIDTH));
      // Draw the outer walls.
      g.draw(new Line2D.Float(0, 0, env.getWidth(), 0)); // Top
      g.draw(new Line2D.Float(0, env.getHeight(), env.getWidth(), env.getHeight())); // Bottom
      g.draw(new Line2D.Float(0, 0, 0, env.getHeight())); // Left
      g.draw(new Line2D.Float(env.getWidth(), 0, env.getWidth(), env.getHeight())); // Right

      // Draw top walls
      for (int x = 0; x < env.getWidth(); x++) {
         for (int y = 1; y < env.getHeight(); y++) {
            if (env.isFacingWall(new Coord2D(x, y), Direction.UP)) {
               g.draw(new Line2D.Float(x, y, x + 1, y));
            }
         }
      }
      // Draw left walls
      for (int x = 1; x < env.getWidth(); x++) {
         for (int y = 0; y < env.getHeight(); y++) {
            if (env.isFacingWall(new Coord2D(x, y), Direction.LEFT)) {
               g.draw(new Line2D.Float(x, y, x, y + 1));
            }
         }
      }
      // Draw "pillars"
      for (int x = 1; x < env.getWidth(); x++) {
         for (int y = 1; y < env.getHeight(); y++) {
            g.fill(new Ellipse2D.Float(x - WALL_WIDTH, y - WALL_WIDTH, 2 * WALL_WIDTH, 2 * WALL_WIDTH));
         }
      }
      // For other rendering, the coordinate system has integer coordinates *centered*
      // in those cells.
      g.translate(.5f, .5f);
      cachedTransform = g.getTransform();
   }
}

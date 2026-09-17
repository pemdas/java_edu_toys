package edu.cascadia.roboworld;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;

/**
 * Thin wrapper around BufferedImage to support drawing static graphics onto the
 * panel.
 */
public class MapPanelSprite extends MapPanelDrawable {
   private Pose2D position = new Pose2D();
   private BufferedImage image;
   double spriteScale;

   public MapPanelSprite(BufferedImage image, double spriteCellSize) {
      this.image = image;
      spriteScale = spriteCellSize / Math.max(image.getHeight(), image.getWidth());
   }

   public void setPosition(Pose2D position) {
      this.position.x = position.x;
      this.position.y = position.y;
      this.position.heading = position.heading;
   }

   @Override
   public void draw(Graphics2D g, Dimension d, AffineTransform transform) {
      AffineTransform saved = g.getTransform();
      double cellWidth = spriteScale * image.getWidth();
      double cellHeight = spriteScale * image.getHeight();
      g.translate(position.x - cellWidth / 2.0, position.y - cellHeight / 2.0);
      g.rotate(position.heading, cellWidth / 2.0, cellHeight / 2.0);
      g.scale(spriteScale, spriteScale);
      g.drawRenderedImage(image, transform);
      g.setTransform(saved);
   }
}

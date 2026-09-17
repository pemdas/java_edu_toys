package edu.cascadia.roboworld;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

/**
 * Something that draws itself onto the world map, and scales in size with the
 * map.
 *
 * Items implementing this should keep their desired size and position in terms
 * of map cells.
 */
public abstract class MapPanelDrawable {
   private static final AffineTransform IDENTITY = new AffineTransform();

   /**
    * Render this drawable to the given graphics context. The graphics transform
    * will be set such that rendering coordinates a map coordinates, with the
    * the origin at the middle of the top left cell, and each
    * cell being length 1.
    * 
    * If you want window coordinates instead, reset the graphics transform to the
    * identity
    * transform before drawing.
    * 
    * @param g         Graphics context for the MapPanel
    * @param panelSize Current size of the full panel
    */
   public void draw(Graphics2D g, Dimension panelSize) {
      draw(g, panelSize, IDENTITY);
   }

   /**
    * Render this drawable to the given graphics context. The graphics transform
    * will be set such that rendering coordinates a map coordinates, with the
    * the origin at the middle of the top left cell, and each cell being length
    * 1.
    *
    * If you want window coordinates instead, reset the graphics transform to
    * the identity transform before drawing.
    *
    * @param g         Graphics context for the MapPanel
    * @param panelSize Current size of the full panel
    * @param transform Extra transform to apply before rendering.
    */
   public abstract void draw(Graphics2D g, Dimension panelSize, AffineTransform transform);

   /**
    * For time-dependent renders (e.g. animations) advance the current time.
    */
   public void advanceTime(double seconds) {
   }

   /**
    * Returns true if this particular drawable should no longer be drawn.
    */
   public boolean done() {
      return false;
   }
}

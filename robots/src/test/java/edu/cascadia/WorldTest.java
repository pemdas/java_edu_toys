package edu.cascadia;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class WorldTest {

   @Test
   public void testImplicitWalls() {
      // Create an empty world, check that the exterior walls exist.
      // @formatter:off
      // +---+---+
      // |       |
      // +   +   +
      // |       |
      // +---+---+
      // @formatter:on
      World w = new World(2, 2);
      assertTrue(w.isFacingWall(new Coord2D(0, 0), Direction.UP));
      assertTrue(w.isFacingWall(new Coord2D(0, 0), Direction.LEFT));
      assertFalse(w.isFacingWall(new Coord2D(0, 0), Direction.DOWN));
      assertFalse(w.isFacingWall(new Coord2D(0, 0), Direction.RIGHT));

      assertTrue(w.isFacingWall(new Coord2D(1, 0), Direction.UP));
      assertFalse(w.isFacingWall(new Coord2D(1, 0), Direction.LEFT));
      assertFalse(w.isFacingWall(new Coord2D(1, 0), Direction.DOWN));
      assertTrue(w.isFacingWall(new Coord2D(1, 0), Direction.RIGHT));

      assertFalse(w.isFacingWall(new Coord2D(0, 1), Direction.UP));
      assertTrue(w.isFacingWall(new Coord2D(0, 1), Direction.LEFT));
      assertTrue(w.isFacingWall(new Coord2D(0, 1), Direction.DOWN));
      assertFalse(w.isFacingWall(new Coord2D(0, 1), Direction.RIGHT));

      assertFalse(w.isFacingWall(new Coord2D(1, 1), Direction.UP));
      assertFalse(w.isFacingWall(new Coord2D(1, 1), Direction.LEFT));
      assertTrue(w.isFacingWall(new Coord2D(1, 1), Direction.DOWN));
      assertTrue(w.isFacingWall(new Coord2D(1, 1), Direction.RIGHT));
   }

   @Test
   public void testVerticalWalls() {
      // @formatter:off
      // +---+---+
      // |       |
      // +   +   +
      // |   |   |
      // +   +   +
      // |   |   |
      // +---+---+
      // @formatter:on
      World w = new World(2, 3);
      w.addWall(new Coord2D(0, 1), Direction.RIGHT);
      w.addWall(new Coord2D(1, 2), Direction.LEFT);
      w.addWall(new Coord2D(1, 1), Direction.LEFT); // nop, wall exists
      assertFalse(w.isFacingWall(new Coord2D(0, 0), Direction.RIGHT));
      assertFalse(w.isFacingWall(new Coord2D(1, 0), Direction.LEFT));
      assertTrue(w.isFacingWall(new Coord2D(0, 1), Direction.RIGHT));
      assertTrue(w.isFacingWall(new Coord2D(1, 1), Direction.LEFT));
      assertTrue(w.isFacingWall(new Coord2D(0, 2), Direction.RIGHT));
      assertTrue(w.isFacingWall(new Coord2D(1, 2), Direction.LEFT));
   }

   @Test
   public void testHorzontalWalls() {
      // @formatter:off
      // +---+---+---+
      // |           |
      // +---+   +---+
      // |           |
      // +---+---+---+
      // @formatter:on
      World w = new World(3, 2);
      w.addWall(new Coord2D(0, 1), Direction.UP);
      w.addWall(new Coord2D(0, 0), Direction.DOWN); // nop, wall exists
      w.addWall(new Coord2D(2, 0), Direction.DOWN);
      assertTrue(w.isFacingWall(new Coord2D(0, 0), Direction.DOWN));
      assertFalse(w.isFacingWall(new Coord2D(1, 0), Direction.DOWN));
      assertTrue(w.isFacingWall(new Coord2D(2, 0), Direction.DOWN));
      assertTrue(w.isFacingWall(new Coord2D(0, 1), Direction.UP));
      assertFalse(w.isFacingWall(new Coord2D(1, 1), Direction.UP));
      assertTrue(w.isFacingWall(new Coord2D(2, 1), Direction.UP));
   }

}

package edu.cascadia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

public class RobotTest {
   private World world;

   public RobotTest() {
      world = new World(2, 2);
      world.addWall(new Coord2D(0, 0), Direction.RIGHT);
   }

   @Test
   public void turnLeft() {
      Robot r = new Robot(world, new Coord2D(0, 1), Direction.RIGHT);
      assertEquals(r.getPosition(), new Coord2D(0, 1));
      assertEquals(r.getDirection(), Direction.RIGHT);
      r.turnLeft();
      assertEquals(r.getPosition(), new Coord2D(0, 1));
      assertEquals(r.getDirection(), Direction.UP);
      r.turnLeft();
      assertEquals(r.getPosition(), new Coord2D(0, 1));
      assertEquals(r.getDirection(), Direction.LEFT);
      r.turnLeft();
      assertEquals(r.getPosition(), new Coord2D(0, 1));
      assertEquals(r.getDirection(), Direction.DOWN);
      r.turnLeft();
      assertEquals(r.getPosition(), new Coord2D(0, 1));
      assertEquals(r.getDirection(), Direction.RIGHT);
   }

   @Test
   public void turnRight() {
      Robot r = new Robot(world, new Coord2D(1, 0), Direction.DOWN);
      assertEquals(r.getPosition(), new Coord2D(1, 0));
      assertEquals(r.getDirection(), Direction.DOWN);
      r.turnRight();
      assertEquals(r.getPosition(), new Coord2D(1, 0));
      assertEquals(r.getDirection(), Direction.LEFT);
      r.turnRight();
      assertEquals(r.getPosition(), new Coord2D(1, 0));
      assertEquals(r.getDirection(), Direction.UP);
      r.turnRight();
      assertEquals(r.getPosition(), new Coord2D(1, 0));
      assertEquals(r.getDirection(), Direction.RIGHT);
      r.turnRight();
      assertEquals(r.getPosition(), new Coord2D(1, 0));
      assertEquals(r.getDirection(), Direction.DOWN);
   }

   @Test
   public void movement() {
      // Starting state:
      // @formatter:off
      // +---+---+
      // |R->|   |
      // +   +   +
      // |       |
      // +---+---+
      // @formatter:on

      Robot r = new Robot(world, new Coord2D(0, 0), Direction.RIGHT);
      assertFalse(r.moveForward());
      assertEquals(r.getPosition(), new Coord2D(0, 0));
      assertEquals(r.getDirection(), Direction.RIGHT);
      r.turnLeft();
      assertFalse(r.moveForward());
      assertEquals(r.getPosition(), new Coord2D(0, 0));
      assertEquals(r.getDirection(), Direction.UP);
      r.turnLeft();
      assertFalse(r.moveForward());
      assertEquals(r.getPosition(), new Coord2D(0, 0));
      assertEquals(r.getDirection(), Direction.LEFT);
      r.turnLeft();
      assertTrue(r.moveForward());
      assertEquals(r.getPosition(), new Coord2D(0, 1));
      assertEquals(r.getDirection(), Direction.DOWN);
      assertFalse(r.moveForward());
      assertEquals(r.getPosition(), new Coord2D(0, 1));
      assertEquals(r.getDirection(), Direction.DOWN);
      r.turnRight();
      assertFalse(r.moveForward());
      assertEquals(r.getPosition(), new Coord2D(0, 1));
      assertEquals(r.getDirection(), Direction.LEFT);
      r.turnRight();
      r.turnRight();
      assertTrue(r.moveForward());
      assertEquals(r.getPosition(), new Coord2D(1, 1));
      assertEquals(r.getDirection(), Direction.RIGHT);
      assertFalse(r.moveForward());
      assertEquals(r.getPosition(), new Coord2D(1, 1));
      assertEquals(r.getDirection(), Direction.RIGHT);
      r.turnLeft();
      assertTrue(r.moveForward());
      assertEquals(r.getPosition(), new Coord2D(1, 0));
      assertEquals(r.getDirection(), Direction.UP);
      r.turnLeft();
      assertFalse(r.moveForward());
      assertEquals(r.getPosition(), new Coord2D(1, 0));
      assertEquals(r.getDirection(), Direction.LEFT);

   }

}

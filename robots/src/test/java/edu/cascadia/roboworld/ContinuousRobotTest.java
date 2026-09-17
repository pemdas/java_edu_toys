package edu.cascadia.roboworld;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

public class ContinuousRobotTest {
   private Environment env;

   public ContinuousRobotTest() {
      env = new Environment(2, 2);
      env.addWall(new Coord2D(0, 0), Direction.RIGHT);
   }

   @Test
   public void turnLeft() {
      ContinuousRobot r = new ContinuousRobot(env, new Pose2D(0, 1, Direction.RIGHT));
      r.advance(1000);
      assertEquals(r.getPose(), new Pose2D(0, 1, Direction.RIGHT));
      r.turnLeft();
      assertEquals(r.getPose(), new Pose2D(0, 1, Direction.UP));
      r.turnLeft();
      assertEquals(r.getPose(), new Pose2D(0, 1, Direction.LEFT));
      r.turnLeft();
      assertEquals(r.getPose(), new Pose2D(0, 1, Direction.DOWN));
      r.turnLeft();
      assertEquals(r.getPose(), new Pose2D(0, 1, Direction.RIGHT));
   }

   @Test
   public void turnRight() {
      ContinuousRobot r = new ContinuousRobot(env, new Pose2D(1, 0, Direction.DOWN));
      r.advance(1000);
      assertEquals(r.getPose(), new Pose2D(1, 0, Direction.DOWN));
      r.turnRight();
      assertEquals(r.getPose(), new Pose2D(1, 0, Direction.LEFT));
      r.turnRight();
      assertEquals(r.getPose(), new Pose2D(1, 0, Direction.UP));
      r.turnRight();
      assertEquals(r.getPose(), new Pose2D(1, 0, Direction.RIGHT));
      r.turnRight();
      assertEquals(r.getPose(), new Pose2D(1, 0, Direction.DOWN));
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

      ContinuousRobot r = new ContinuousRobot(env, new Pose2D(0, 0, Direction.RIGHT));
      r.advance(1000);
      assertEquals(new Pose2D(0, 0, Direction.RIGHT), r.getPose());
      r.turnLeft();
      assertEquals(new Pose2D(0, 0, Direction.UP), r.getPose());
      r.turnLeft();
      assertEquals(new Pose2D(0, 0, Direction.LEFT), r.getPose());
      r.turnLeft();
      assertEquals(new Pose2D(0, 0, Direction.DOWN), r.getPose());
      r.moveForward();
      assertEquals(new Pose2D(0, 1, Direction.DOWN), r.getPose());
      r.turnRight();
      assertEquals(new Pose2D(0, 1, Direction.LEFT), r.getPose());
      r.turnRight();
      r.turnRight();
      r.moveForward();
      assertEquals(new Pose2D(1, 1, Direction.RIGHT), r.getPose());
      r.turnLeft();
      r.moveForward();
      assertEquals(new Pose2D(1, 0, Direction.UP), r.getPose());
      r.turnLeft();
      assertEquals(new Pose2D(1, 0, Direction.LEFT), r.getPose());
   }

   /*
    * @Test
    * public void crash() {
    * Robot r = new Robot(env, new Coord2D(0, 0), Direction.RIGHT);
    * assertFalse(r.crashed());
    * r.moveForward();
    * assertTrue(r.crashed());
    * assertEquals(r.getPosition(), new Coord2D(0, 0));
    * assertEquals(r.getDirection(), Direction.RIGHT);
    * 
    * // Turns should not work when crashed.
    * r.turnLeft();
    * assertEquals(r.getDirection(), Direction.RIGHT);
    * r.turnRight();
    * assertEquals(r.getDirection(), Direction.RIGHT);
    * 
    * }
    */
}

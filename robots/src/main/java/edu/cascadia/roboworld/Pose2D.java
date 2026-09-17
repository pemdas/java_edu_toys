package edu.cascadia.roboworld;

import java.util.Arrays;

/**
 * Pose of a robot in continuous 2d space.
 */
public class Pose2D {
   // These need to match the robot sprites, which face up.
   static private double RIGHT = Math.PI / 2;
   static private double DOWN = Math.PI;
   static private double LEFT = 3 * Math.PI / 2;
   static private double UP = 0;

   static private double TRANSLATION_SPEED = 1.0;
   static private double ROTATION_SPEED = 1.0;

   public double x;
   public double y;
   public double heading; // Radians, normalized to [0, 2*PI)

   // Default constructor.
   public Pose2D() {
   }

   public Pose2D(Pose2D other) {
      x = other.x;
      y = other.y;
      heading = other.heading;
   }

   public Pose2D(int x, int y, Direction direction) {
      this.x = x;
      this.y = y;
      switch (direction) {
         case UP:
            heading = Pose2D.UP;
            break;
         case DOWN:
            heading = Pose2D.DOWN;
            break;
         case LEFT:
            heading = Pose2D.LEFT;
            break;
         case RIGHT:
            heading = Pose2D.RIGHT;
            break;
      }
   }

   @Override
   public boolean equals(Object other) {
      if (!(other instanceof Pose2D)) {
         throw new Error("What are you comparing?");
      }
      Pose2D op = (Pose2D) other;
      return x == op.x && y == op.y && heading == op.heading;
   }

   @Override
   public String toString() {
      return "(" + x + ", " + y + ", " + heading + ")";
   }

   static double normalizeHeading(double heading) {
      heading = heading % (2 * Math.PI);
      if (heading < 0) {
         heading += 2 * Math.PI;
      }
      return heading;
   }

   // Convert the heading to a direction.
   public Direction direction() {
      if (heading == RIGHT) {
         return Direction.RIGHT;
      } else if (heading == DOWN) {
         return Direction.DOWN;
      } else if (heading == LEFT) {
         return Direction.LEFT;
      } else if (heading == UP) {
         return Direction.UP;
      }
      throw new Error("Can't get direction from indeterminate heading");
   }

   // Get the pose integer coordinates. Requires the robot not be mid-move.
   public int cellX() {
      int ret = (int) x;
      assert (ret == x);
      return ret;
   }

   public int cellY() {
      int ret = (int) y;
      assert (ret == y);
      return ret;
   }

   public Coord2D coord2D() {
      return new Coord2D(cellX(), cellY());
   }

   /**
    * Move towards the target pose for at most seconds of time.
    * 
    * @return the remaining time after reaching the target pose, or 0 if the
    *         target pose was not reached.
    */
   public double moveTowards(Pose2D target, double seconds) {
      // Robots don't do combined movements; at any given time they should only be
      // moving in one of x, y, or rotation.
      if (target.x != x) {
         assert target.y == y;
         assert target.heading == heading;
         double timeToTarget = Math.abs(target.x - x) / TRANSLATION_SPEED;
         if (seconds < timeToTarget) {
            x += Math.signum(target.x - x) * TRANSLATION_SPEED * seconds;
            return 0;
         } else {
            x = target.x;
            return seconds - timeToTarget;
         }
      } else if (target.y != y) {
         assert target.heading == heading;
         double timeToTarget = Math.abs(target.y - y) / TRANSLATION_SPEED;
         if (seconds < timeToTarget) {
            y += Math.signum(target.y - y) * TRANSLATION_SPEED * seconds;
            return 0;
         } else {
            y = target.y;
            return seconds - timeToTarget;
         }
      } else {
         assert target.heading != heading;
         double deltaH = target.heading - heading;
         if (deltaH > Math.PI) {
            deltaH -= 2 * Math.PI;
         } else if (deltaH < -Math.PI) {
            deltaH += 2 * Math.PI;
         }
         double timeToTarget = Math.abs(deltaH) / ROTATION_SPEED;
         if (seconds < timeToTarget) {
            heading += Math.signum(deltaH) * ROTATION_SPEED * seconds;
            return 0;
         } else {
            heading = target.heading;
            return seconds - timeToTarget;
         }
      }
   }

   @Override
   public int hashCode() {
      double[] tmp = new double[3];
      tmp[0] = x;
      tmp[1] = y;
      tmp[2] = heading;
      return Arrays.hashCode(tmp);
   }
}

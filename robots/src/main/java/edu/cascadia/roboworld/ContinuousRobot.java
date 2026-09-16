package edu.cascadia.roboworld;

public class ContinuousRobot implements Robot {
   private Environment env;
   private Pose2D pose;
   private Pose2D targetPose = null;
   private boolean isCrashed = false;

   // How much time the robot has remaining to make moves before pausing to let the
   // UI update.
   private double timeRemaining;

   // Heading constants.
   // Note that since the coordinate system is x-right, y-down, rotation is
   // clockwise.

   public ContinuousRobot(Environment env, Pose2D pose) {
      this.env = env;
      this.pose = new Pose2D(pose);
      assert env.isInBounds(pose.cellX(), pose.cellY());
   }

   synchronized public void run(double seconds) {
      timeRemaining += seconds;
      notify();
   }

   synchronized public Pose2D getPose() {
      return new Pose2D(pose);
   }

   // Update position until either we reach our target pose or we run out of time.
   synchronized private void run() {
      while (!pose.equals(targetPose)) {
         while (timeRemaining == 0) {
            try {
               wait();
            } catch (InterruptedException e) {
               // Ignored
            }
         }
         timeRemaining = pose.moveTowards(targetPose, timeRemaining);
      }
      targetPose = null;
   }

   /** Turn left 90 degrees */
   public void turnLeft() {
      assert targetPose == null;
      if (!isCrashed) {
         targetPose = new Pose2D(pose.cellX(), pose.cellY(), pose.direction().left());
         run();
      }
   }

   /** Turn right 90 degrees */
   public void turnRight() {
      assert targetPose == null;
      if (!isCrashed) {
         targetPose = new Pose2D(pose.cellX(), pose.cellY(), pose.direction().right());
         run();
      }
   }

   /**
    * Attempt to move forward one space. Returns true on success, false if the way
    * was blocked
    */
   public void moveForward() {
      assert targetPose == null;
      if (isCrashed) {
         return;
      }
      if (blocked()) {
         isCrashed = true;
         return;
      }
      // Don't check explicitly for moving out of bounds -- the map enforces
      // walls around the edges, so it should be impossible.
      Direction dir = pose.direction();
      switch (dir) {
         case UP:
            targetPose = new Pose2D(pose.cellX(), pose.cellY() - 1, dir);
            break;
         case LEFT:
            targetPose = new Pose2D(pose.cellX() - 1, pose.cellY(), dir);
            break;
         case DOWN:
            targetPose = new Pose2D(pose.cellX(), pose.cellY() + 1, dir);
            break;
         case RIGHT:
            targetPose = new Pose2D(pose.cellX() + 1, pose.cellY(), dir);
            break;
      }
      run();
   }

   /**
    * Check if the way forward is currently blocked.
    * 
    * @return true if the way is blocked, false otherwise.
    */
   public boolean blocked() {
      return env.isFacingWall(pose.coord2D(), pose.direction());
   }

   /**
    * Check if the robot has crashed.
    * 
    * @return true if the robot has crashed, false otherwise.
    */
   public boolean crashed() {
      return isCrashed;
   }

   static public void main(String[] args) {
      System.out.println("Hello, world!");
   }
}
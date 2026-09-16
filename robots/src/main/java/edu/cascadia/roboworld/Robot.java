package edu.cascadia.roboworld;

public interface Robot {

   /** Turn left 90 degrees */
   public void turnLeft();

   /** Attempt to move forward. If the way is blocked, the robot will crash. */
   public void moveForward();

   /** Turn right 90 degrees */
   public void turnRight();

   /**
    * Check if the way forward is currently blocked.
    * 
    * @return true if the way is blocked, false otherwise.
    */
   public boolean blocked();

   /**
    * Check if the robot has crashed.
    * 
    * @return true if the robot has crashed, false otherwise.
    */
   public boolean crashed();

}

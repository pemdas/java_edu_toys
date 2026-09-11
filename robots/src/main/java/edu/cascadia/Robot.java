package edu.cascadia;

public class Robot {

   private World world;
   private Coord2D position;
   private Direction direction;

   public Robot(World world, Coord2D position, Direction direction) {
      this.world = world;
      this.position = new Coord2D(position);
      this.direction = direction;
      assert world.isInBounds(position);
   }

   public Coord2D getPosition() {
      return new Coord2D(position);
   }

   public Direction getDirection() {
      return direction;
   }

   /** Turn left 90 degrees */
   public void turnLeft() {
      direction = direction.left();
   }

   /**
    * Attempt to move forward one space. Returns true on success, false if the way
    * was blocked
    */
   public boolean moveForward() {
      if (isBlocked()) {
         return false;
      }
      // Don't check explicitly for moving out of bounds -- the world enforces
      // walls around the edges, so it should be impossible.
      switch (direction) {
         case UP:
            position.y--;
            break;
         case LEFT:
            position.x--;
            break;
         case DOWN:
            position.y++;
            break;
         case RIGHT:
            position.x++;
            break;
      }
      return true;
   }

   /** Turn right 90 degrees */
   public void turnRight() {
      direction = direction.right();
   }

   /**
    * Check if the way forward is currently blocked.
    * 
    * @return true if the way is blocked, false otherwise.
    */
   public boolean isBlocked() {
      return world.isFacingWall(position, direction);
   }

   static public void main(String[] args) {
      System.out.println("Hello, world!");
   }
}
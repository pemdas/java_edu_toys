package edu.cascadia;

/**
 * This class represents a world containing robots and walls
 * 
 */
public class World {
   // Dimensions of the world. Valid robot x positions are from 0...width-1 and
   // y positions are from 0...height - 1
   // The World coordinates follow screen coordinates conventions -- the origin is
   // the top left, with positive x to the right and positive y down.

   // To simplify bounds checking
   private int width;
   private int height;

   // Every world implicitly has walls all round the edges of the world (e.g. every
   // cell with x=0 implicitly has a wall to the left)
   //
   // This means we just need to represent interior walls. The convention here is
   // to represent bottom and right walls based on cell coordinates. If you need
   // to check a top or left wall, adjust your coordinates. True means a wall is
   // present.

   private boolean[][] rightWalls;
   private boolean[][] bottomWalls;

   public World(int width, int height) {
      assert width > 0;
      assert height > 0;
      this.width = width;
      this.height = height;

      // -1 because the far right column has implicit right walls, and the
      // bottom row has implicit bottom walls.
      rightWalls = new boolean[width - 1][height];
      bottomWalls = new boolean[width][height - 1];
   }

   public void addWall(Coord2D pos, Direction direction) {
      assert isInBounds(pos);
      if (isExternalBoundary(pos, direction)) {
         // Nothing to do.
         return;
      }
      switch (direction) {
         case UP:
            bottomWalls[pos.x][pos.y - 1] = true;
            break;
         case LEFT:
            rightWalls[pos.x - 1][pos.y] = true;
            break;
         case DOWN:
            bottomWalls[pos.x][pos.y] = true;
            break;
         case RIGHT:
            rightWalls[pos.x][pos.y] = true;
            break;
      }
   }

   // Valid robot coordinates range from 0 to width-1
   public int getWidth() {
      return width;
   }

   // Valid robot coordinates range from 0 to height-1
   public int getHeight() {
      return height;
   }

   public boolean isInBounds(Coord2D pos) {
      return pos.x >= 0 && pos.y >= 0 && pos.x < width && pos.y < height;
   }

   private boolean isExternalBoundary(Coord2D pos, Direction direction) {
      return (direction == Direction.UP && pos.y == 0)
            || (direction == Direction.DOWN && pos.y == height - 1)
            || (direction == Direction.LEFT && pos.x == 0)
            || (direction == Direction.RIGHT && pos.x == width - 1);
   }

   public boolean isFacingWall(Coord2D pos, Direction direction) {
      assert isInBounds(pos);
      // Take care of the implicit boundary walls first.
      if (isExternalBoundary(pos, direction)) {
         return true;
      }
      switch (direction) {
         case UP:
            return bottomWalls[pos.x][pos.y - 1];
         case LEFT:
            return rightWalls[pos.x - 1][pos.y];
         case DOWN:
            return bottomWalls[pos.x][pos.y];
         case RIGHT:
            return rightWalls[pos.x][pos.y];
         default:
            throw new AssertionError("Bad direction");
      }
   }

}
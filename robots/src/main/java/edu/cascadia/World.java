package edu.cascadia;

/**
 * This class represents a world containing robots and walls
 * 
 */
public class World {
   // Dimensions of the world
   private int width;
   private int height;

   // Wall representations. For cell (x, y)
   // hWalls[x][y] - Top wall
   // hWalls[x][y+1] - Bottom wall
   // vWalls[x][y] - Left wall
   // vWalls[x+1][y] - Right wall
   // Where true means a wall is present.
   private boolean[][] vWalls;
   private boolean[][] hWalls;

   private void init(int width, int height) {
      this.width = width;
      this.height = height;

      vWalls = new boolean[width + 1][height];
      hWalls = new boolean[width][height + 1];

   }

}
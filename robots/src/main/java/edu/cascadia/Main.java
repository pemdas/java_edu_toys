package edu.cascadia;

public class Main {
   public static void main(String[] args) {
      Environment env = new Environment(4, 5);
      env.addWall(new Coord2D(0, 0), Direction.RIGHT);
      // Robot r = new Robot(world, new Coord2D(1, 1), Direction.LEFT);
      // r.turnLeft();
   }
}

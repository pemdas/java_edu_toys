package edu.cascadia.roboworld;

public enum Direction {
   UP(0), LEFT(1), DOWN(2), RIGHT(3);

   private final int direction;

   private Direction(int val) {
      this.direction = val;
   }

   public Direction left() {
      return values()[(direction + 1) % 4];
   }

   public Direction right() {
      return values()[(direction + 3) % 4];
   }

   public Direction opposite() {
      return values()[(direction + 2) % 4];
   }

}

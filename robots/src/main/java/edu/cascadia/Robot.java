package edu.cascadia;

public class Robot {
   /** Turn left 90 degrees */
   public void left() {
   }

   /**
    * Attempt to move forward one space. Returns true on success, false if the way
    * was blocked
    */
   public boolean forward() {
      return true;
   }

   /** Turn right 90 degrees */
   public void right() {
   }

   /**
    * Check if the way forward is currently blocked.
    * 
    * @return true if the way is blocked, false otherwise.
    */
   public boolean isBlocked() {
      return false;
   }

   static public void main(String[] args) {
      System.out.println("Hello, world!");
   }
}
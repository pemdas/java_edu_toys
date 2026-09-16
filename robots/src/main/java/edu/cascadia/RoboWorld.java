package edu.cascadia;

// This is the class that students actually interact with.  

public class RoboWorld {
   public enum Scene {
      TEST1
   };

   static Robot createScenario(Scene s) {
      switch (s) {
         case TEST1:
            return test1Scene();
      }
      // This is here and not a default case so that the compiler enforces that
      // the switch has a case for each defined enum value.
      throw new Error("Unknown scenario: " + s);
   }

   private static Robot test1Scene() {

   }
}

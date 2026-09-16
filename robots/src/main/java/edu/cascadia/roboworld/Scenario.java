package edu.cascadia.roboworld;

// FIXME - Create GUI stuff on the swing thread, not the main thread.
//
// Also, need to detect when the main thread has exited, both to stop timers and detect goal conditions.   May not be able
// do to anything better than polling.  :P

//
// Slight refactor plan to enable loading and goal states:  
//   Current "World" class becomes "Environment", includes walls, eventually includes items.
//   Robot references its environment
//   World class becomes a bundle of Environment + Robot, includes support for loading from text strings, comparison to goal.
//   Goal state loading and initial state loading share the same code path.  
//      (It would be nice to also support max number of moves?  Why oh why doesn't java have built-in support for any reasonable
//       structured filetypes???)

//

// This encapsulates everything we need to run a scenario -- the starting states of the robot, environment, and also any goals that must be satisfied for 
// the scenario to be complete.
//
// It's also the entry point for users to create a scenario and play it. 
public class Scenario {
   static final public int TEST1 = 0;

   static Robot setUp(int scenarioId) {
      switch (scenarioId) {
         case TEST1:
            return test1Scene();
         default:
            throw new Error("Unknown scenario id: " + scenarioId);
      }
   }

   private static Robot test1Scene() {
      return null;
   }
}
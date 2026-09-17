package edu.cascadia.roboworld;

import java.util.HashSet;
import java.util.NoSuchElementException;

import javax.swing.SwingUtilities;

import edu.cascadia.roboworld.Environment.MapParseException;

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

   private Environment env;
   private ContinuousRobot robot;

   private String description;

   // (Make goal validation a callback? Makes scenarios impossible to create
   // declaratively, but adds the most flexibility...)

   public Scenario(Environment env, ContinuousRobot r) {
      this.env = env;
      this.robot = r;
   }

   HashSet<Pose2D> goalPoses = new HashSet<>();

   // Add a goal position for the robot. If the robot ends in any goal position, it
   // has met the goal. If no goal poses are added, then the robot can end in any
   // position.
   void addGoalPose(Pose2D pose) {
      goalPoses.add(pose);
   }

   public boolean goalsMet() {
      return goalPoses.isEmpty() || goalPoses.contains(robot.getPose());
   }

   static Robot setUp(int scenarioId) {
      Scenario scene = createScenarioFromId(scenarioId);
      try {
         Thread appThread = Thread.currentThread();
         SwingUtilities.invokeAndWait(() -> scene.createAndShowGUI(appThread));
      } catch (Exception e) {
         throw new IllegalStateException(e);
      }

      return scene.robot;
   }

   static Scenario createScenarioFromId(int scenarioId) {
      switch (scenarioId) {
         case TEST1:
            return test1Scene();
         default:
            throw new NoSuchElementException("Unknown scenario id: " + scenarioId);
      }
   }

   void createAndShowGUI(Thread appThread) {
      new RobotWindow("Robot Land", appThread, env, robot).setVisible(true);

   }

   static final public int TEST1 = 0;

   private static Scenario test1Scene() throws MapParseException {
      Environment e = new Environment("" +
            "+-+-+-+\n" +
            "|     |\n" +
            "+ + + +\n" +
            "|     |\n" +
            "+ + + +\n" +
            "| | | |\n" +
            "+ +-+ +\n" +
            "|     |\n" +
            "+-+-+-+\n");
      ContinuousRobot r = new ContinuousRobot(e, new Pose2D(2, 1, Direction.RIGHT));
      return new Scenario(e, r);
   }

   public Environment getEnvironment() {
      return env;
   }

   public ContinuousRobot getRobot() {
      return robot;
   }

}
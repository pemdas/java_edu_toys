package edu.cascadia.roboworld;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.Timer;

public class RobotWindow extends JFrame {
   private final static int TARGET_FPS = 30;

   private JPanel paintPanel;

   // The application (not swing) thread.
   private Thread appThread;

   private javax.swing.Timer timer;
   private ContinuousRobot robot;
   long lastTimerNanos;

   public RobotWindow(String title, Thread appThread, Environment env, ContinuousRobot robot) {
      super(title);
      this.appThread = appThread;
      this.robot = robot;
      System.out.println("App thread id is " + appThread.getId());
      setMinimumSize(new Dimension(400, 400));
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      paintPanel = new WorldPanel(env, robot, Color.CYAN);
      getContentPane().add(paintPanel, BorderLayout.CENTER);
      JPanel bottomPanel = new JPanel();
      ImageIcon playIcon = new ImageIcon(Resources.loadImage("play_button.png"));

      JButton button = new JButton();
      button.setIcon(playIcon);

      JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 100, 5);
      bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
      bottomPanel.add(button);
      bottomPanel.add(Box.createRigidArea(new Dimension(40, 0)));
      JLabel speedLabel = new JLabel("Speed");
      Resources.loadFont("NotoSans-Light.ttf");
      speedLabel.setFont(new Font("Noto Sans Light", Font.PLAIN, 18));
      bottomPanel.add(speedLabel);
      bottomPanel.add(slider);

      bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
      getContentPane().add(bottomPanel, BorderLayout.PAGE_END);

      // display it
      pack();
      setVisible(true);
      timer = new Timer(Math.round(1000.0f / TARGET_FPS), new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            timerFired();
         }
      });
      lastTimerNanos = System.nanoTime();
      timer.start();

   }

   public void timerFired() {
      if (!appThread.isAlive()) {
         // TODO - Check goal states.
         System.out.println("App thread exited");
         timer.stop();
      } else {
         long now = System.nanoTime();
         double elapsed = (now - lastTimerNanos) / 1_000_000_000.0;
         lastTimerNanos = now;
         // System.out.println("Running " + elapsed + " seconds");
         robot.run(elapsed);
         paintPanel.paintImmediately(0, 0, paintPanel.getWidth(), paintPanel.getHeight());
         Toolkit.getDefaultToolkit().sync();
      }
   }

   final static int MARGIN_PX = 10;

   /*
    * public static void main(String[] args) {
    * System.out.println("Main thread is " + Thread.currentThread().getId());
    * Environment e;
    * try {
    * e = new Environment(
    * 
    * "" +
    * "+-+-+-+\n" +
    * "|     |\n" +
    * "+ + + +\n" +
    * "|     |\n" +
    * "+ + + +\n" +
    * "| | | |\n" +
    * "+ +-+ +\n" +
    * "|     |\n" +
    * "+-+-+-+\n");
    * } catch (MapParseException ex) {
    * throw new Error(ex);
    * }
    * // Environment e = new Environment(3, 4);
    * // e.addWall(new Coord2D(0, 0), Direction.RIGHT);
    * // e.addWall(new Coord2D(1, 1), Direction.DOWN);
    * new RobotWindow("Robot Land", e, new ContinuousRobot(e, new Pose2D(1, 2,
    * Direction.LEFT))).setVisible(true);
    * 
    * }
    */
}

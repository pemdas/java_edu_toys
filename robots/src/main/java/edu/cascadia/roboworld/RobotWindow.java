package edu.cascadia.roboworld;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Hashtable;

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

   private JPanel worldPanel;

   private JSlider speedSlider;
   static private int[] SPEED_SLIDER_VALUES = { 1, 2, 5, 10, 100 };

   private JButton playButton;
   private boolean running = false;
   private ImageIcon playIcon = new ImageIcon(Resources.PLAY_ICON);
   private ImageIcon pauseIcon = new ImageIcon(Resources.PAUSE_ICON);

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
      worldPanel = new WorldPanel(env, robot, Color.CYAN);
      getContentPane().add(worldPanel, BorderLayout.CENTER);
      JPanel bottomPanel = new JPanel();
      // ImageIcon playIcon = new ImageIcon(Resources.loadImage("play_button.png"));

      playButton = new JButton();
      playButton.setIcon(playIcon);
      playButton.setFocusPainted(false);
      playButton.addActionListener(new ActionListener() {
         @Override
         public void actionPerformed(ActionEvent e) {
            playButton.setIcon(running ? playIcon : pauseIcon);
            running = !running;
         }
      });

      bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
      bottomPanel.add(playButton);
      bottomPanel.add(Box.createRigidArea(new Dimension(40, 0)));

      JLabel speedLabel = new JLabel("Speed");
      speedLabel.setFont(Resources.PRIMARY_FONT.deriveFont(18.f));
      bottomPanel.add(speedLabel);
      speedSlider = new JSlider(JSlider.HORIZONTAL, 0, SPEED_SLIDER_VALUES.length - 1, 0);
      Hashtable<Integer, JLabel> sliderLabels = new Hashtable<>();
      for (int i = 0; i < SPEED_SLIDER_VALUES.length; i++) {
         JLabel l = new JLabel("" + SPEED_SLIDER_VALUES[i] + "x");
         l.setFont(Resources.PRIMARY_FONT);
         sliderLabels.put(i, l);
      }
      speedSlider.setMajorTickSpacing(1);
      speedSlider.setLabelTable(sliderLabels);
      speedSlider.setPaintLabels(true);
      speedSlider.setPaintTicks(true);
      speedSlider.setSnapToTicks(true);
      speedSlider.setUI(new MetalSnapSliderUI());
      bottomPanel.add(speedSlider);
      bottomPanel.add(Box.createGlue());

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
         if (running) {
            robot.advance(SPEED_SLIDER_VALUES[speedSlider.getValue()] * elapsed);
         }
         worldPanel.paintImmediately(0, 0, worldPanel.getWidth(), worldPanel.getHeight());
         // X11 likes to kind of nagle algorithm events sometimes, which causes latency.
         // Flush rendering out immediately.
         Toolkit.getDefaultToolkit().sync();
      }
   }

}

package edu.cascadia.roboworld;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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
import javax.swing.JTextField;
import javax.swing.SwingConstants;
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

   // Status elements
   JTextField movesStatus = new JTextField();
   JTextField leftTurnsStatus = new JTextField();
   JTextField rightTurnsStatus = new JTextField();

   private javax.swing.Timer timer;
   private ContinuousRobot robot;
   long lastTimerNanos;

   static private JLabel makeLabel(String text, Font font) {
      JLabel ret = new JLabel(text);
      ret.setFont(font);
      return ret;
   }

   public RobotWindow(String title, Thread appThread, Environment env, ContinuousRobot robot) {
      super(title);
      this.appThread = appThread;
      this.robot = robot;
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
      bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
      bottomPanel.add(playButton);
      bottomPanel.add(Box.createRigidArea(new Dimension(40, 0)));

      bottomPanel.add(makeLabel("Speed", Resources.LARGE_FONT));
      speedSlider = new JSlider(JSlider.HORIZONTAL, 0, SPEED_SLIDER_VALUES.length - 1, 0);
      Hashtable<Integer, JLabel> sliderLabels = new Hashtable<>();
      for (int i = 0; i < SPEED_SLIDER_VALUES.length; i++) {
         sliderLabels.put(i, makeLabel("" + SPEED_SLIDER_VALUES[i] + "x", Resources.SMALL_FONT));
      }
      speedSlider.setLabelTable(sliderLabels);
      speedSlider.setPaintLabels(true);
      speedSlider.setSnapToTicks(true);
      speedSlider.setUI(new MetalSnapSliderUI());
      speedSlider.setMaximumSize(new Dimension(0, 1000)); // Don't stretch slider
      bottomPanel.add(Box.createRigidArea(new Dimension(5, 0))); // Add a little space between label and slider
      bottomPanel.add(speedSlider);
      bottomPanel.add(Box.createHorizontalGlue());

      getContentPane().add(bottomPanel, BorderLayout.PAGE_END);
      JPanel rightPanel = new JPanel();
      rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));

      JPanel statusPanel = new JPanel();
      var statusBorder = BorderFactory.createTitledBorder("Status");
      statusBorder.setTitleFont(Resources.MEDIUM_FONT);
      statusPanel.setBorder(statusBorder);
      statusPanel.setLayout(new GridBagLayout());

      String[] statusLabels = { "Moves:", "Left Turns:", "Right Turns:" };
      JTextField[] statusFields = { movesStatus, leftTurnsStatus, rightTurnsStatus };
      assert statusLabels.length == statusFields.length;
      var c = new GridBagConstraints();
      for (int i = 0; i < statusLabels.length; i++) {
         c.gridx = 0;
         c.gridy = i;
         c.insets.left = 10;
         c.insets.right = 5;
         c.fill = GridBagConstraints.HORIZONTAL;
         var l = makeLabel(statusLabels[i], Resources.MEDIUM_FONT);
         l.setHorizontalAlignment(SwingConstants.RIGHT);
         statusPanel.add(l, c);
         c.gridx = 1;
         c.insets.left = 0;
         c.insets.right = 10;
         c.fill = GridBagConstraints.NONE;
         statusFields[i].setText("0");
         statusFields[i].setFont(Resources.MEDIUM_FONT);
         statusFields[i].setHorizontalAlignment(SwingConstants.CENTER);
         statusFields[i].setEditable(false);
         statusFields[i].setBackground(Color.WHITE);
         statusFields[i].setPreferredSize(new Dimension(50, 25));
         statusPanel.add(statusFields[i], c);
         c.insets.top = 3; // For all rows after the first.
      }
      rightPanel.add(statusPanel);
      rightPanel.add(Box.createVerticalGlue());
      getContentPane().add(rightPanel, BorderLayout.LINE_END);

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
         timer.stop();
         running = false;
         playButton.setIcon(playIcon);
         playButton.setEnabled(false);
      } else {
         long now = System.nanoTime();
         double elapsed = (now - lastTimerNanos) / 1_000_000_000.0;
         lastTimerNanos = now;
         if (running) {
            robot.advance(SPEED_SLIDER_VALUES[speedSlider.getValue()] * elapsed);
            movesStatus.setText("" + robot.numMoveForwardCalls());
            leftTurnsStatus.setText("" + robot.numTurnLeftCalls());
            rightTurnsStatus.setText("" + robot.numTurnRightCalls());
         }
         worldPanel.paintImmediately(0, 0, worldPanel.getWidth(), worldPanel.getHeight());
         // X11 likes to kind of nagle algorithm events sometimes, which causes latency.
         // Flush rendering out immediately.
         Toolkit.getDefaultToolkit().sync();
      }
   }

}

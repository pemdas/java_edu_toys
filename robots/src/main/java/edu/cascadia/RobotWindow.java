package edu.cascadia;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class RobotWindow extends JFrame {

   private JPanel paintPanel;

   public RobotWindow(String title, World world, ContinuousRobot robot) {
      super(title);
      setMinimumSize(new Dimension(400, 400));
      setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      paintPanel = new MapPanel(world, robot, Color.CYAN);
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

   }

   final static int MARGIN_PX = 10;

   public static void main(String[] args) {
      World w = new World(3, 4);
      w.addWall(new Coord2D(0, 0), Direction.RIGHT);
      w.addWall(new Coord2D(1, 1), Direction.DOWN);
      new RobotWindow("Robot Land", w, new ContinuousRobot(w, new Pose2D(1, 2, Direction.LEFT))).setVisible(true);

   }
}

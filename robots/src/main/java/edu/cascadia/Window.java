package edu.cascadia;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EtchedBorder;

public class Window {

   static class PaintPanel extends JPanel {

      @Override
      public void paintComponent(Graphics gr) {
         Graphics2D g = (Graphics2D) gr;
         g.setColor(Color.RED);
         g.drawLine(0, 0, getWidth(), getHeight());
      }
   }

   private static BufferedImage loadResource(String name) {
      try (InputStream in = Window.class.getResourceAsStream(name)) {
         if (in == null)
            throw new IOException("Built-in resource not found: " + name);
         return ImageIO.read(in);
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   private static Font loadFont(String name) {
      try (InputStream in = Window.class.getResourceAsStream(name)) {
         if (in == null)
            throw new IOException("Built-in resource not found: " + name);
         return Font.createFont(Font.TRUETYPE_FONT, in);
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      } catch (FontFormatException e) {
         throw new Error(e.getMessage());
      }
   }

   public static void main(String[] args) {

      JFrame frame = new JFrame("RobotLand");
      frame.setMinimumSize(new Dimension(400, 400));
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      JPanel centerPanel = new JPanel();
      centerPanel.setLayout(new BorderLayout());
      centerPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
      PaintPanel paintPanel = new PaintPanel();
      paintPanel.setBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED));
      centerPanel.add(paintPanel, BorderLayout.CENTER);
      frame.getContentPane().add(centerPanel, BorderLayout.CENTER);
      JPanel bottomPanel = new JPanel();
      ImageIcon playIcon = new ImageIcon(loadResource("play_button.png"));
      ;

      JButton button = new JButton();
      button.setIcon(playIcon);

      JSlider slider = new JSlider(JSlider.HORIZONTAL, 1, 100, 5);
      bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 20, 5, 20));
      bottomPanel.add(button);
      bottomPanel.add(Box.createRigidArea(new Dimension(20, 0)));
      JLabel speedLabel = new JLabel("Speed");
      Font f = loadFont("NotoSans-Light.ttf");
      GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(f);
      // System.out.println("Registered " + f);
      speedLabel.setFont(new Font("Noto Sans Light", Font.PLAIN, 100));
      speedLabel.setBorder(BorderFactory.createLineBorder(Color.GREEN));
      bottomPanel.add(speedLabel);
      bottomPanel.add(slider);

      bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
      frame.getContentPane().add(bottomPanel, BorderLayout.PAGE_END);

      // display it
      frame.pack();
      frame.setVisible(true);

   }
}

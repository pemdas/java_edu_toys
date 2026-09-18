package edu.cascadia.roboworld;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import javax.imageio.ImageIO;

public class Resources {
   static {
      loadFont("NotoSans-Medium.ttf");
   }

   final public static BufferedImage ROBOT_SPRITE = loadImage("robot.png");
   final public static double ROBOT_SPRITE_CELL_SIZE = 0.6;

   final public static BufferedImage PLAY_ICON = loadImage("play_button.png");
   final public static BufferedImage PAUSE_ICON = loadImage("pause_button.png");

   final public static Font LARGE_FONT = new Font("Noto Sans Medium", Font.PLAIN, 16);
   final public static Font MEDIUM_FONT = new Font("Noto Sans Medium", Font.PLAIN, 16);
   final public static Font SMALL_FONT = new Font("Noto Sans Medium", Font.PLAIN, 10);

   public static BufferedImage loadImage(String name) {
      try (InputStream in = Resources.class.getResourceAsStream(name)) {
         if (in == null)
            throw new IOException("Built-in resource not found: " + name);
         return ImageIO.read(in);
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      }
   }

   public static void loadFont(String name) {
      try (InputStream in = Resources.class.getResourceAsStream(name)) {
         if (in == null)
            throw new IOException("Built-in resource not found: " + name);
         GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(Font.createFont(Font.TRUETYPE_FONT, in));
      } catch (IOException e) {
         throw new UncheckedIOException(e);
      } catch (FontFormatException e) {
         throw new Error(e.getMessage());
      }
   }

}

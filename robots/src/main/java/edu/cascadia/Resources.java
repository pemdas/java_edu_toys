package edu.cascadia;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

import javax.imageio.ImageIO;

public class Resources {
   final public static BufferedImage ROBOT_SPRITE = loadImage("robot.png");

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

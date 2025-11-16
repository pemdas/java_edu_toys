package org.rezrov;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

/**
 * Generate Mipmaps for an image.
 * 
 * Graphics2D does a terrible job scaling by default (nearest pixel, I think?),
 * so to
 * work around that we use some mipmaps.
 * 
 * This may get fancier in the future, but for now we just support rendering
 * the loaded image using the appropriate mipmap into an arbitrary rectangle.
 * The rendered image will be padded as necessary to fit into the (center of)
 * the specified rectangle. No support for stretching or similar at the moment,
 * not because it's particularly difficult, but just because I don't see a need
 * for it in the way I'm planning to use this.
 * 
 * This also doesn't support rendering through transforms with non-zero shear
 * values; it's not clear to me what the right way to handle that would be.
 */

public class MipMap2D implements Tile {
    final int SCALING_HINT = Image.SCALE_AREA_AVERAGING;

    /**
     * Load an image. Generate mipmaps of that image all the way down until
     * one dimension is 1 pixel.
     */
    MipMap2D(String filename) {
        BufferedImage loadedImage = null;
        File pathToFile = new File(filename);
        int w;
        int h;
        Dimension d;
        try {
            loadedImage = ImageIO.read(pathToFile);

            w = loadedImage.getWidth();
            h = loadedImage.getHeight();
            d = new Dimension(w, h);

            if (w == 0 || h == 0) {
                throw new IOException("Image \"" + filename + "\" has no contents");
            }
        } catch (IOException ioe) {
            throw new UncheckedIOException("Failed to load \"" + filename + "\"", ioe);
        }
        _aspectRatio = w / (double) h;
        Image mipmap = loadedImage;

        while (true) {
            _mipMaps.add(new Entry((Dimension) d.clone(), mipmap));
            d.height /= 2;
            d.width /= 2;
            if (d.height == 0 || d.width == 0) {
                break;
            }
            mipmap = mipmap.getScaledInstance(d.width, d.height, SCALING_HINT);
        }
    }

    /**
     * Draw the image into the destination rectangle using the given graphics
     * instance.
     */
    public void draw(Graphics2D g, Rectangle2D dest) {
        AffineTransform transform = g.getTransform();
        assert Math.abs(transform.getScaleX() - transform.getScaleY()) < .00001;
        assert transform.getShearX() == 0 && transform.getShearY() == 0;
        assert !dest.isEmpty();

        Point2D.Double destA = new Point2D.Double(dest.getX(), 0);
        Point2D.Double destB = new Point2D.Double(dest.getX() + dest.getWidth(), 0);

        Point2D.Double pxDestA = new Point2D.Double();
        Point2D.Double pxDestB = new Point2D.Double();
        transform.transform(destA, pxDestA);
        transform.transform(destB, pxDestB);

        // It seems like there must be an easier way to derive this.
        double pxDestWidth = pxDestA.distance(pxDestB);

        // Get the smallest mipmap which has a dimension larger than either the dest
        // width or the dest height.
        int idx = 0;
        for (Entry e : _mipMaps) {
            if (e.dimension.width < pxDestWidth) {
                break;
            }
            ++idx;
        }
        Entry entry = _mipMaps.get(Math.max(0, idx - 1));

        double destAspectRatio = dest.getWidth() / dest.getHeight();
        AffineTransform drawTransform = AffineTransform.getTranslateInstance(dest.getX(), dest.getY());
        if (destAspectRatio > _aspectRatio) {
            // The image will touch the top and the bottom of the rectangle and
            // have padding on the sides.
            double destActualWidth = _aspectRatio * dest.getHeight();
            drawTransform.translate((dest.getWidth() - destActualWidth) / 2, 0);
            double scale = dest.getHeight() / entry.dimension.height;
            drawTransform.scale(scale, scale);
        } else {
            // The image will touch the top and the bottom of the rectangle and
            // (might) have padding on the sides.
            double destActualHeight = dest.getWidth() / _aspectRatio;
            drawTransform.translate(0, (dest.getHeight() - destActualHeight) / 2);
            double scale = dest.getWidth() / entry.dimension.width;
            drawTransform.scale(scale, scale);
        }
        g.drawImage(entry.image, drawTransform, null);
    }

    private double _aspectRatio;

    private class Entry {
        Entry(Dimension d, Image i) {
            dimension = d;
            image = i;
        }

        Dimension dimension;
        Image image;
    }

    // As indices go up the mipmaps get smaller. Could use a TreeMap here, but we
    // expect the number of entries to be small (10 or less almost always), so
    // linear search is reasonable (and likely faster).
    private ArrayList<Entry> _mipMaps = new ArrayList<Entry>();

}

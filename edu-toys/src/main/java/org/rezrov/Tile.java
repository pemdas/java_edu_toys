package org.rezrov;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

public interface Tile {
    public void draw(Graphics2D g, Rectangle2D dest);
}

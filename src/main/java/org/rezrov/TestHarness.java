package org.rezrov;

public class TestHarness {
    static public void main(String[] args) throws InterruptedException {
        TileWindow d = new TileWindow("Woohoo", 10, 10);

        d.setVerbosity(10);
        // int kodiak = d.loadImage("/home/jcarlson/Desktop/kodiak.png");

        ImageTile robot = new ImageTile("/home/justin/Desktop/robot.png");
        ImageTile wide = new ImageTile("/home/justin/Desktop/wide.png");
        ImageTile tall = new ImageTile("/home/justin/Desktop/tall.png");

        d.setTile(0, 0, robot, 90);
        d.setTile(1, 1, robot, 90);
        d.setTile(2, 3, robot, 180);
        d.setTile(4, 6, robot, 270);
        d.setTile(4, 4, robot, 60);
        while (true) {
            char ch = d.nextInput();

            int rotation = 0;
            if (ch == 'a') {
                rotation = 270;
            } else if (ch == 's') {
                rotation = 180;
            } else if (ch == 'd') {
                rotation = 90;
            }
            for (int r = 0; r < 10; ++r) {
                for (int c = 0; c < 10; ++c) {
                    d.setTile(r, c, robot, rotation);
                }
            }

            if (ch == 'q') {
                break;
            }

        }
    }
}

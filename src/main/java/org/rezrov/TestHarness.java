package org.rezrov;

public class TestHarness {
    static public void main(String[] args) throws InterruptedException {
        TileWindow d = new TileWindow(10, 10);

        d.setVerbosity(10);
        // int kodiak = d.loadImage("/home/jcarlson/Desktop/kodiak.png");

        ImageTile robot = new ImageTile("/home/justin/Desktop/robot.png");
        ImageTile wide = new ImageTile("/home/justin/Desktop/wide.png");
        ImageTile tall = new ImageTile("/home/justin/Desktop/tall.png");

        d.setTile(0, 0, robot, 90);
        d.setTile(1, 1, robot, 90);
        d.setTile(2, 3, robot, 180);
        d.setTile(4, 6, robot, 270);
        while (true) {
            char c = d.nextInput();
            System.out.println("Got " + c);

            if (c == 'q') {
                break;
            }

        }
    }
}

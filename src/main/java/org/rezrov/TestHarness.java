package org.rezrov;

public class TestHarness {
    static public void main(String[] args) throws InterruptedException {
        TileWindow d = new TileWindow(10, 10);

        d.setVerbosity(10);
        // int kodiak = d.loadImage("/home/jcarlson/Desktop/kodiak.png");

        ImageTile robot = new ImageTile("/home/justin/Desktop/robot.png");
        ImageTile wide = new ImageTile("/home/justin/Desktop/wide.png");
        ImageTile tall = new ImageTile("/home/justin/Desktop/tall.png");

        d.setTile(0, 0, robot);
        d.setTile(1, 0, wide);
        d.setTile(0, 1, tall);
        while (true) {
            char c = d.nextInput();
            System.out.println("Got " + c);

            if (c == 'q') {
                break;
            }

        }
    }
}

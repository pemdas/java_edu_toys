package org.rezrov;

import org.rezrov.Draw;

public class TestHarness {
    static public void main(String [] args) throws InterruptedException {
        Draw d = new Draw(10, 10);
//        int kodiak = d.loadImage("/home/jcarlson/Desktop/kodiak.png");
        int robot = d.loadImage("/home/jcarlson/Desktop/robot.png");
         
        while (true) {
            char c = d.nextInput();
            System.out.println("Got " + c);

            if (c == 'q') {
                break;
            }

        }
    }   
}

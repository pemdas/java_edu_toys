package edu.cascadia.roboworld;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;

import javax.swing.JSlider;
import javax.swing.plaf.metal.MetalSliderUI;

public class MetalSnapSliderUI extends MetalSliderUI {

    private MouseMotionListener myMouseMotionListener = new MouseMotionAdapter() {
        @Override
        public void mouseDragged(MouseEvent e) {
            calculateThumbLocation();
            slider.repaint();
        }
    };

    @Override
    protected void installListeners(JSlider slider) {
        super.installListeners(slider);
        slider.addMouseMotionListener(myMouseMotionListener);
    }

    @Override
    protected void uninstallListeners(JSlider slider) {
        super.uninstallListeners(slider);
        slider.removeMouseMotionListener(myMouseMotionListener);
    }

}
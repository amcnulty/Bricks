package com.monkeystomp.aaron.bricks;

/**
 * Created by Aaron on 4/1/2015.
 */
public class Controls {

    // Dimensions of the screen.
    private int width, height;

    // Top of the control panel.
    private int topY;

    public Controls(int width, int height) {
        this.width = width;
        this.height = height;
        topY = height - 250;
    }

    public void update() {

    }

    public void render(BricksView view) {
        view.drawControls(topY);
    }
}

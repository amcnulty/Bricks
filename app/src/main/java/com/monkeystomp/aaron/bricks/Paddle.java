package com.monkeystomp.aaron.bricks;

/**
 * Created by Aaron on 4/1/2015.
 */
public class Paddle {

    // Width and height of the paddle.
    public static final int PADDLE_WIDTH = 75;
    public static final int PADDLE_HEIGHT = 7;

    // Bounds for the paddle x location.
    private int paddleLeftBound = 78;
    private int paddleRightBound;

    // Speed of paddle animation.
    private static final int PADDLE_SPEED_SEC = 170;

    // Used to keep track of time.
    long now;
    double elapsed;
    
    // Paddle coordinates.
    public double x;
    public int y;

    // Color of the paddle.
    private int paddleColor = 0xffff0000;
    
    // Dimensions of the screen.
    private int width, height;

    // Level the paddle is on. This is currently not being used.
    Level level;
    
    public Paddle(int width, int height, Level level) {
        this.width = width;
        this.height = height;
        this.level = level;
        this.paddleRightBound = width - 78;
        x = (width / 2);
        y = height - 270;
    }

    public boolean paddleHere(double x, double y) {
        if (x >= this.x - 37 && x <= this.x + 37 && y >= this.y) {
            return true;
        }
        return false;
    }
    
    public void update(long lastTime, BricksView view) {
        now = System.currentTimeMillis();
        if (now < lastTime) return;
        else {
            if (view.goLeft && x > paddleLeftBound){
                elapsed = (now - lastTime) / 1000.0;
                x -= PADDLE_SPEED_SEC * elapsed;
            }
            else if (view.goRight && x < paddleRightBound){
                elapsed = (now - lastTime) / 1000.0;
                x += PADDLE_SPEED_SEC * elapsed;
            }
            if (x < paddleLeftBound) x = paddleLeftBound;
            else if (x > paddleRightBound) x = paddleRightBound;
        }
    }

    public void render(BricksView view) {
        view.drawPaddle(x, y, paddleColor);
    }
        
}

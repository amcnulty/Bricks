package com.monkeystomp.aaron.bricks;

/**
 * Created by Aaron on 4/1/2015.
 */
public class Paddle {

    // Width and height of the paddle.
    public static final int PADDLE_WIDTH = 75;
    public static final int PADDLE_HEIGHT = 7;

    // Bounds for the paddle x location.
    private static final int PADDLE_LEFT_BOUND = 40;
    private static final int PADDLE_RIGHT_BOUND = 440;

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

    // Handle for BricksView
    BricksView view;
    
    public Paddle(int width, int height, BricksView view) {
        this.width = width;
        this.height = height;
        this.view = view;
        x = (width / 2);
        y = height - 270;
    }
    
    public void update(long lastTime) {
        now = System.currentTimeMillis();
        if (now < lastTime) return;
        else {
            if (view.goLeft && x > PADDLE_LEFT_BOUND){
                elapsed = (now - lastTime) / 1000.0;
                x -= PADDLE_SPEED_SEC * elapsed;
            }
            else if (view.goRight && x < PADDLE_RIGHT_BOUND){
                elapsed = (now - lastTime) / 1000.0;
                x += PADDLE_SPEED_SEC * elapsed;
            }
            if (x < PADDLE_LEFT_BOUND) x = PADDLE_LEFT_BOUND;
            else if (x > PADDLE_RIGHT_BOUND) x = PADDLE_RIGHT_BOUND;
        }
    }

    public void render(BricksView view) {
        view.drawPaddle(x, y, paddleColor);
    }
        
}

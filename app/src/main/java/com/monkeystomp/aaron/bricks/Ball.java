package com.monkeystomp.aaron.bricks;

import android.util.Log;

/**
 * Created by Aaron on 4/1/2015.
 */
public class Ball {

    // The ball's coordinates.
    private double x, y;

    private static final double BALL_RADIUS = 15.0;

    private int ballColor = 0xffffffff;

    // Handle for the paddle. The ball needs to know where the paddle is.
    Paddle paddle;

    public Ball(Paddle paddle) {
        this.paddle = paddle;
        x = paddle.x;
        y = paddle.y - BALL_RADIUS;
    }

    public void update() {
        x = paddle.x;
        y = paddle.y - BALL_RADIUS;
    }

    public void render(BricksView view) {
        view.drawBall(x, y, BALL_RADIUS, ballColor);
    }
}

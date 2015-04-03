package com.monkeystomp.aaron.bricks;

import android.util.Log;

import java.util.Random;

/**
 * Created by Aaron on 4/1/2015.
 */
public class Ball {

    // Access to random elements.
    Random random;

    // The ball's coordinates.
    private double x, y;

    // The ball's direction is based off of degrees.
    // ex. Up is 90 degrees, down is 270, right is 0 and, left is 180.
    private double dir = -1.0;

    // The radius of the ball.
    private static final double BALL_RADIUS = 15.0;

    // Speed of ball animation.
    private static final int BALL_SPEED_SEC = 200;

    private int ballColor = 0xffffffff;

    // Used in the move method for collision detection.
    private boolean collision = false;

    // Handle for the paddle. The ball needs to know where the paddle is.
    Paddle paddle;

    // Handle for the level. The ball needs to know where the blocks are.
    Level level;

    public Ball(Paddle paddle, Level level) {
        this.paddle = paddle;
        this.level = level;
        x = paddle.x;
        y = paddle.y - BALL_RADIUS;
        random = new Random();
    }

    public void followPaddle() {
        x = paddle.x;
        y = paddle.y - BALL_RADIUS;
    }

    public void launchBall() {
        dir = 80 + (20 * random.nextDouble());
    }

    public void move(double dir, long lastTime, double now) {
        double point = 0.0;
        double newDir = 0.0;
        double newPoint = 0.0;
        double elapsed = (now - lastTime) / 1000.0;
        // Calculate the new x and y based on dir and lastTime.
        double newX = Math.cos(dir) * BALL_SPEED_SEC * elapsed;
        double newY = Math.sin(dir) * BALL_SPEED_SEC * elapsed;
        // Check for collisions on the leading half of the circle.
        // ex. dir = 45 check between 45 - 90 degrees and 45 + 90 degrees.
        for (int step = - 90; step <= 90; step += 15) {
            if (level.blockHere(newX + (BALL_RADIUS * Math.cos(dir + step)), newY + (BALL_RADIUS * Math.sin(dir + step)))) {
                collision = true;
                point = dir + step;
                break;
            }
        }
        if (collision) {
            newPoint = point + 180;
            if (newPoint >= 360) newPoint -= 360;
            newDir = dir + 180;
            if (newDir >= 360) newDir -= 360;
            this.dir = (newPoint - newDir) + newPoint;
            if (this.dir >= 360) this.dir -= 360;
            else if (this.dir < 0) this.dir += 360;
            collision = false;
        }
        else {
            x += newX;
            y += newY;
        }
    }

    public void update(long lastTime) {
        long now = System.currentTimeMillis();
        if (now < lastTime) return;
        else {
            move(dir, lastTime, now);
        }
    }

    public void render(BricksView view) {
        view.drawBall(x, y, BALL_RADIUS, ballColor);
    }
}

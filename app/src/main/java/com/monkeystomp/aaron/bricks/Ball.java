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

    // Radius to check for collisions.
    private static final double BALL_COLLISION_RADIUS = 16.5;

    // Speed of ball animation.
    private static final int BALL_SPEED_SEC = 200;

    private int ballColor = 0xffffffff;

    // Used in the move method for collision detection.
    private boolean collision = false;

    // Handle for the paddle. The ball needs to know where the paddle is.
    Paddle paddle;

    // Handle for the level. The ball needs to know where the blocks are.
    Level level;

    // Several variables used in the move method.
    double point = 0.0;
    double elapsed;
    double nextX, nextY;
    long now;

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
        dir = 104;
        Log.v("_________launchBall", "Ball is launching in direction " + dir);
    }

    public void move(double dir, long lastTime, double now) {
//        double newDir = 0.0;
//        double newPoint = 0.0;
        elapsed = (now - lastTime) / 1000.0;
        // Calculate the new x and y based on direction and lastTime.
        nextX = x + (Math.cos(Math.toRadians(dir)) * BALL_SPEED_SEC * elapsed);
        nextY = y - (Math.sin(Math.toRadians(dir)) * BALL_SPEED_SEC * elapsed);
        // Check for collisions on the leading half of the circle.
        // ex. dir = 45 check between 45 - 90 degrees and 45 + 90 degrees.
        for (int i = 0; i < 4; i++) {
            switch (i) {
                case 0:
                    if (level.blockHere(nextX + BALL_COLLISION_RADIUS, nextY)) {
                        collision = true;
                        point = 0.0;
                    }
                    break;
                case 1:
                    if (level.blockHere(nextX, nextY - BALL_COLLISION_RADIUS)) {
                        collision = true;
                        point = 90.0;
                    }
                    break;
                case 2:
                    if (level.blockHere(nextX - BALL_COLLISION_RADIUS, nextY)) {
                        collision = true;
                        point = 180.0;
                    }
                    break;
                case 3:
                    if (level.blockHere(nextX, nextY + BALL_COLLISION_RADIUS)) {
                        collision = true;
                        point = 270.0;
                    }
                    break;
            }
        }
        if (!collision) {
            for (int step = -90; step <= 90; step += 15) {
                if (level.blockHere(nextX + (BALL_COLLISION_RADIUS * Math.cos(Math.toRadians(dir + step))), nextY - (BALL_COLLISION_RADIUS * Math.sin(Math.toRadians(dir + step))))) {
                    collision = true;
                    point = dir + step;
                    break;
                }
            }
        }
            Log.v("_________move", "Collision: " + collision + " direction: " + dir + " Ball X: " + x + " Ball Y: " + y);
        if (collision) {
//            newPoint = point + 180;
//            if (newPoint >= 360) newPoint -= 360;
//            newDir = dir + 180;
//            if (newDir >= 360) newDir -= 360;
//            this.dir = (newPoint - newDir) + newPoint;
            Log.v("_________move", " dir: " + dir + " this.dir: " + this.dir + " point: " + point);
            this.dir = (point - dir) + (point + 180);
            if (this.dir >= 360) this.dir -= 360;
            else if (this.dir < 0) this.dir += 360;
            collision = false;
        }
        else {
            x = nextX;
            y = nextY;
        }
    }

    public void update(long lastTime) {
        now = System.currentTimeMillis();
        if (now < lastTime) return;
        else {
            move(dir, lastTime, now);
        }
    }

    public void render(BricksView view) {
        view.drawBall(x, y, BALL_RADIUS, ballColor);
    }
}

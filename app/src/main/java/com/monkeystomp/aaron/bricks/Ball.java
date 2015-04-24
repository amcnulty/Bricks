package com.monkeystomp.aaron.bricks;

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
    private static final double BALL_RADIUS = 10.0;

    // Radius to check for collisions.
    private static final double BALL_COLLISION_RADIUS = 11.5;

    // Speed of ball animation.
    private static final int BALL_SPEED_SEC = 200;

    private int ballColor = 0xffffffff;

    // Used in the move method for collision detection.
    private boolean collision = false;

    // Used to signal the view that ball has been lost.
    private boolean ballInPlay = true;

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
        y = paddle.y - BALL_COLLISION_RADIUS;
        random = new Random();
    }

    public void followPaddle() {
        x = paddle.x;
        y = paddle.y - BALL_COLLISION_RADIUS;
    }

    public void launchBall() {
        dir = 80 + (20 * random.nextDouble());
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
        // Check for the paddle
        if (!collision) {
            if (level.paddleHere(nextX, nextY + BALL_COLLISION_RADIUS)) {
                collision = true;
                point = 270.0;
                if (nextX > paddle.x - 38 && nextX < paddle.x - 17) {
                    point = 280.0;
                }
                else if (nextX < paddle.x + 38 && nextX > paddle.x + 17) {
                    point = 260.0;
                }
            }
        }
        // checks for walls and ceiling.
        if (!collision) {
            for (int i = 0; i < 3; i++) {
                switch (i) {
                    case 0:
                        if (level.wallHere(nextX - BALL_COLLISION_RADIUS, nextY)) {
                            collision = true;
                            point = 180.0;
                        }
                        break;
                    case 1:
                        if (level.wallHere(nextX, nextY - BALL_COLLISION_RADIUS)) {
                            collision = true;
                            point = 90.0;
                        }
                        break;
                    case 2:
                        if (level.wallHere(nextX + BALL_COLLISION_RADIUS, nextY)) {
                            collision = true;
                            point = 0.0;
                        }
                        break;
                }
            }
        }
        if (collision) {
            this.dir = (point - dir) + (point + 180);
            if (this.dir >= 360) this.dir -= 360;
            else if (this.dir < 0) this.dir += 360;
            if (this.dir >= 0 && this.dir < 18) this.dir = 18;
            else if (this.dir <= 180 && this.dir > 162) this.dir = 162;
            else if (this.dir >= 180 && this.dir < 198) this.dir = 198;
            else if (this.dir <= 359.99999 && this.dir > 342) this.dir = 342;
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
        if (y + BALL_COLLISION_RADIUS > paddle.y) {
            ballInPlay = false;
        }
    }

    public void render(BricksView view) {
        if (ballInPlay) view.drawBall(x, y, BALL_RADIUS, ballColor);
        else {
            view.resetGame();
            ballInPlay = true;
        }
    }
}

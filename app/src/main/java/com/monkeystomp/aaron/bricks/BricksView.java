/**
 * A basic brick breaker game to learn many basic aspects of android game programming.
 */

package com.monkeystomp.aaron.bricks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.Random;

/**
 * Created by Aaron on 3/18/2015.
 */
class BricksView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    // Thread used to make the game loop and decide what is to be drawn to the Canvas
    private Thread thread;

    // Variables used for the game loop timer.
    private long timer;
    private int frames;

    // Variables used for drawing the ball.
    double xLeg, yLeg;

    // Object variable for SurfaceHolder class.
    // Used to lock canvas and unlockCanvasAndPost
    private SurfaceHolder mSurfaceHolder;

    // Used to allow the game loop.
    // When set false, game thread will reach end of run method and die.
    private boolean running = false;

    // Various states the game can be in.
    private static final int GAME_READY = 1;
    private static final int GAME_IN_PLAY = 2;
    public static final int GAME_PAUSED = 3;
    public static final int GAME_LOST = 4;
    private static final int GAME_RESTARTING = 5;
    public static final int LEVEL_CLEARED = 6;

    // The gameState variable is the switch control for updates and renders.
    public int gameState;

    // Width and height of the surface.
    private int width, height;

    // States if the keys are pressed down.
    public boolean goLeft = false;
    public boolean goRight = false;

    // Controls the movement of the paddle
    boolean leftButton = false;
    boolean rightButton = false;

    // Color constants for the buttons.
    private static final int DIRECTIONAL_BUTTON_COLOR_UP = 0xff0000ff;
    private static final int DIRECTIONAL_BUTTON_COLOR_DOWN = 0xffff0000;
    private static final int MIDDLE_BUTTON_COLOR_UP = 0xff00ff00;
    private static final int MIDDLE_BUTTON_COLOR_DOWN = 0xffff0000;

    // Color of the buttons.
    int leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
    int rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
    int middleButtonColor = MIDDLE_BUTTON_COLOR_UP;

    // Used to keep track of loop intervals.
    private long lastTime = 0;

    // Used to time the game won and lost messages.
    private long messageTimer;

    // Allows for random elements in the game.
    private Random random;

    // Bitmap of the game canvas.
    private Bitmap bitmap;

    // Array of pixel data to be given to the bitmap.
    public int[] pixels;

    // Length of the pixels array
    private int len;

    // The only level in the game right now.
    Level level;

    // The game's user controlled paddle.
    Paddle paddle;

    // The game ball.
    Ball ball;

    // The touch screen buttons at the bottom.
    Controls controls;

    // Paint object for the strings to be displayed.
    Paint scoreTextPaint;
    Paint buttonTextPaint;
    Paint middleButtonTextPaint;
    Paint multiplierTextPaint;
    Paint gameLostTextPaint;

    // The game score.
    int score = 0;

    // The game score multiplier.
    private String multiplier;
    private boolean showMultiplier = false;

    /**
     * Constructor for setting up the SurfaceView with the SurfaceHolder.
     * Game thread gets instantiated here.
     * @param context
     * @param attributeSet
     */
    public BricksView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;
        controls = new Controls(width, height);
        level = new Level(width, height, context, this);
        ball = new Ball(level.paddle, level);
        pixels = new int[width * height];
        len = pixels.length;
        resetBackground();
        drawWalls();
        random = new Random();
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        thread = new Thread(this, "Surface-View-Thread");
        setFocusable(true);
        setFocusableInTouchMode(true);
        scoreTextPaint = new Paint();
        scoreTextPaint.setTextSize(40);
        scoreTextPaint.setColor(0xff888888);
        buttonTextPaint = new Paint();
        buttonTextPaint.setColor(0xff000000);
        buttonTextPaint.setTextSize(180);
        middleButtonTextPaint = new Paint();
        middleButtonTextPaint.setColor(0xff000000);
        middleButtonTextPaint.setTextSize(90);
        multiplierTextPaint = new Paint();
        multiplierTextPaint.setTextSize(40);
        multiplierTextPaint.setColor(0xffFFFD01);
        gameLostTextPaint = new Paint();
        gameLostTextPaint.setTextSize(50);
        gameLostTextPaint.setColor(0xffff0000);
        gameState = GAME_READY;
    }

    public void stopMusic() {
        level.stopMusic();
    }

    public void resumeMusic() {
        level.resumeMusic();
    }

    private void resetBackground() {
        for (int y = 0; y < height; y++) {
            for (int x = 41; x < width - 40; x++) {
                pixels[x + y * width] = 0xff000000;
            }
        }
        for (int y = 0; y < 60; y++) {
            for (int x = width - 40; x < width; x++) {
                pixels[x + y * width] = 0xff000000;
            }
        }
    }

    private void drawWalls() {
        for (int y = 60; y < height; y++) {
            for (int x = 0; x <= 40; x++) {
                pixels[x + y * width] = 0xff00ff00;
            }
            for (int x = width - 40; x < width; x++) {
                pixels[x + y * width] = 0xff00ff00;
            }
        }
    }

    public void drawPaddle(double paddleX, int paddleY, int paddleColor) {
        for (int y = 0; y < Paddle.PADDLE_HEIGHT; y++) {
            for (int x = 0; x < Paddle.PADDLE_WIDTH; x++) {
                int xa = (int)paddleX - 37 + x;
                pixels[(xa) + (paddleY + y) * width] = paddleColor;
            }
        }
    }

    public void drawBall(double ballX, double ballY, double radius, int ballColor) {
        // Scan through a square around the center of the ball.
        // Only render a distance <= the radius.
        for (double yp = ballY - radius; yp <= ballY + radius; yp++) {
            for (double xp = ballX - radius; xp <= ballX + radius; xp++) {
                yLeg = Math.abs(yp - ballY);
                xLeg = Math.abs(xp - ballX);
                if (Math.sqrt(Math.pow(xLeg, 2.0) + Math.pow(yLeg, 2.0)) <= radius) {
                    pixels[(int)xp + (int)yp * width] = ballColor;
                }
            }
        }
    }

    public void drawBlock(int x, int y, int color) {
        for (int xx = 0; xx < Block.BLOCK_WIDTH; xx++) {
            for (int yy = 0; yy < Block.BLOCK_HEIGHT; yy++) {
                pixels[(x + xx) + (y + yy) * width] = color;
            }
        }
    }

    public void drawControls(int topY) {
        // A horizontal line.
        for (int y = topY; y < topY + 8; y++) {
            for (int x = 0; x < width; x++) {
                pixels[x + y * width] = 0xff333333;
            }
        }
        // A vertical line.
        for (int y = topY; y < height - 100; y++) {
            for (int x = (width / 2) - (width / 8) - 3; x <= (width / 2) - (width / 8) + 3; x++) {
                pixels[x + y * width] = 0xff333333;
            }
        }
        // A vertical line.
        for (int y = topY; y < height - 100; y++) {
            for (int x = (width / 2) + (width / 8) - 3; x <= (width / 2) + (width / 8) + 3; x++) {
                pixels[x + y * width] = 0xff333333;
            }
        }
        // Fill in with button current color.
        for (int y = topY + 8; y < height - 100; y++) {
            for (int x = 0; x < (width / 2) - (width / 8) - 4; x++) {
                pixels[x + y * width] = leftButtonColor;
            }
        }
        for (int y = topY + 8; y < height - 100; y++) {
            for (int x = (width / 2) + (width / 8) + 4; x < width; x++) {
                pixels[x + y * width] = rightButtonColor;
            }
        }
        // Middle button.
        for (int y = topY + 8; y < height - 100; y++) {
            for (int x = (width / 2) - (width / 8) + 4; x < (width / 2) + (width / 8) - 3; x++) {
                pixels[x + y * width] = middleButtonColor;
            }
        }
    }

    public void drawMultiplier(int multiplier) {
        showMultiplier = true;
        this.multiplier = Integer.toString(multiplier);
    }

    public void eraseMultiplier() {
        showMultiplier = false;
    }

    public void updateScore(int levelScore) {
        score = levelScore;
    }

    /**
     *
     * @param b true if you want the gameLoop to continue false if you want it to stop
     */
    public void setRunning(Boolean b) {
        this.running = b;
    }

    public void resetGame() {
        gameState = GAME_RESTARTING;
        showMultiplier = false;
    }

    public void startEndgameMessage() {
        messageTimer = System.currentTimeMillis();
    }

    /**
     * This is where the game loop will exist
     */
    @Override
    public void run() {
        timer = System.currentTimeMillis();
        frames = 0;
        Canvas c;
        lastTime = System.currentTimeMillis();
        while(running) {
            update();
            c = mSurfaceHolder.lockCanvas();
            frames++;
            render(c);
            mSurfaceHolder.unlockCanvasAndPost(c);
            if (System.currentTimeMillis() - timer >= 1000) {
                timer = System.currentTimeMillis();
                Log.v("________________run", "  |  FPS: " + frames);
                frames = 0;
            }
        }
    }

    /**
     * The game's main update method.
     */
    private void update() {
        switch (gameState) {
            case GAME_READY:
                level.update(lastTime, this);
                ball.followPaddle();
                if (leftButton) goLeft = true;
                else goLeft = false;
                if (rightButton) goRight = true;
                else goRight = false;
                break;
            case GAME_IN_PLAY:
                level.update(lastTime, this);
                ball.update(lastTime);
                if (leftButton) goLeft = true;
                else goLeft = false;
                if (rightButton) goRight = true;
                else goRight = false;
                break;
            case GAME_RESTARTING:
                level.update(lastTime, this);
                ball.followPaddle();
                if (leftButton) goLeft = true;
                else goLeft = false;
                if (rightButton) goRight = true;
                else goRight = false;
                break;
            case GAME_PAUSED:
                // this is where we will update a fragment that has a view and buttons asking about the game being paused.

                break;
            case GAME_LOST:
                if (System.currentTimeMillis() > messageTimer + 3000) {
                    level.resetLevel();
                    gameState = GAME_READY;
                }
                break;
            case LEVEL_CLEARED:
                if (System.currentTimeMillis() > messageTimer + 3000) {
                    level.resetLevel();
                    gameState = GAME_READY;
                }
                break;
        }
        // Recalculate lastTime variable.
        lastTime = System.currentTimeMillis();
    }

    /**
     * The game's main render method.
     * @param c - A Canvas returned from the SurfaceHolder.
     */
    private void render(Canvas c) {
        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        switch (gameState) {
            case GAME_READY:
                resetBackground();
                level.render(this);
                ball.render(this);
                controls.render(this);
                break;
            case GAME_IN_PLAY:
                resetBackground();
                level.render(this);
                ball.render(this);
                controls.render(this);
                break;
            case GAME_RESTARTING:
                resetBackground();
                level.render(this);
                ball.render(this);
                controls.render(this);
                break;
            case GAME_PAUSED:
                resetBackground();
                level.render(this);
                ball.render(this);
                controls.render(this);
                break;
            case GAME_LOST:
                break;
            case LEVEL_CLEARED:

                break;
        }
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        c.drawBitmap(bitmap, 0, 0, null);
        c.drawText("Score: " + score, 10, 40, scoreTextPaint);
        c.drawText("Balls: " + level.ballsRemaining, width - 150, 40, scoreTextPaint);
        c.drawText("<", 50, height - 125, buttonTextPaint);
        c.drawText(">", width - 140, height - 125, buttonTextPaint);
        if (gameState == GAME_LOST) c.drawText("GAME LOST", (width / 2) - 130, height - 400, gameLostTextPaint);
        if (gameState == LEVEL_CLEARED) c.drawText("LEVEL CLEARED", (width / 2) - 170, height - 400, gameLostTextPaint);
        if (showMultiplier) {
            c.drawText("X" + multiplier, width / 2, 40, multiplierTextPaint);
        }
        if (gameState == GAME_IN_PLAY || gameState == GAME_PAUSED || gameState == GAME_LOST || gameState == LEVEL_CLEARED) c.drawText("||", (width / 2) - 24, height - 165, middleButtonTextPaint);
        if (gameState == GAME_READY || gameState == GAME_RESTARTING) {
            c.drawText("^", (width / 2) - 20, height - 170, middleButtonTextPaint);
            c.drawText("^", (width / 2) - 20, height - 130, middleButtonTextPaint);
        }
        }

        /**
         * Standard override to get key-pressed events.
         */
        @Override
        public boolean onKeyDown(int keyCode, KeyEvent msg) {
            switch (keyCode) {
            case KeyEvent.KEYCODE_S:
                Log.v("___________onKeyDown", "S KEY DOWN");
                return true;
            case KeyEvent.KEYCODE_A:
                goLeft = true;
                return true;
            case KeyEvent.KEYCODE_D:
                goRight = true;
                return true;
            default:
                return super.onKeyDown(keyCode, msg);

        }
    }

    /**
     * Standard override to get key-released events.
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent msg) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_S:
                Log.v("___________onKeyDown", "S KEY UP");
                return true;
            case KeyEvent.KEYCODE_A:
                goLeft = false;
                return true;
            case KeyEvent.KEYCODE_D:
                goRight = false;
                return true;
            case KeyEvent.KEYCODE_SPACE:
                if (gameState == GAME_READY || gameState == GAME_RESTARTING) {
                    gameState = GAME_IN_PLAY;
                    ball.launchBall();
                }
                return true;
            default:
                return false;
        }
    }
    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // Here I will set the thread class to setRunning(true); and, start the thread with thread.start();
        setRunning(true);
        try {
            thread.start();
        }
        catch (IllegalThreadStateException e) {
            Log.i("_________SurfaceCreated", "Starting a new thread!");
            thread = new Thread(this, "Surface-View-Thread");
            thread.start();
        }
    }

    /*
     * Callback invoked when the surface dimensions change.
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched.
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        level.pauseMusic();
    }


    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (gameState) {
            case GAME_IN_PLAY:
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.v("_______onTouchEvent", "X: " + e.getX() + " Y: " + e.getY());
                    if (controls.isLeftButton((int)e.getX(), (int)e.getY())) {
                        leftButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                        leftButton = true;
                        rightButton = false;
                    }
                    if (controls.isRightButton((int)e.getX(), (int)e.getY())) {
                        rightButtonColor = 0xffff0000;
                        rightButton = true;
                        leftButton = false;
                    }
                    if (controls.isMiddleButton((int)e.getX(), (int)e.getY())) {
                        middleButtonColor = MIDDLE_BUTTON_COLOR_DOWN;
                        rightButton = false;
                        leftButton = false;
                        Log.v("_______onTouchEvent", "Pause Game");

                    }
                }
                if (e.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                    Log.v("_______onTouchEvent", "ACTION POINTER DOWN. X: " + e.getX(1) + " Y: " + e.getY(1));
                    switch (e.getActionIndex()) {
                        case 0:
                            if (controls.isLeftButton((int) e.getX(), (int) e.getY())) {
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButton = true;
                                rightButton = false;
                            }
                            if (controls.isRightButton((int) e.getX(), (int) e.getY())) {
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = true;
                                leftButton = false;
                            }
                            if (controls.isMiddleButton((int) e.getX(), (int) e.getY())) {
                                middleButtonColor = MIDDLE_BUTTON_COLOR_DOWN;
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = false;
                                leftButton = false;
                            }
                            break;
                        case 1:
                            if (controls.isLeftButton((int) e.getX(1), (int) e.getY(1))) {
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButton = true;
                                rightButton = false;
                            }
                            if (controls.isRightButton((int) e.getX(1), (int) e.getY(1))) {
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = true;
                                leftButton = false;
                            }
                            if (controls.isMiddleButton((int) e.getX(1), (int) e.getY(1))) {
                                middleButtonColor = MIDDLE_BUTTON_COLOR_DOWN;
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = false;
                                leftButton = false;
                            }
                            break;
                    }
                }
                if (e.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                    Log.v("_______onTouchEvent","Action Index: " + e.getActionIndex() + " ACTION POINTER UP. X: " + e.getX(1) + " Y: " + e.getY(1));
                    switch (e.getActionIndex()) {
                        case 0:
                            if (controls.isMiddleButton((int) e.getX(), (int) e.getY())) {
                                gameState = GAME_PAUSED;
                                level.pauseMusic();
                            }
                            else if (controls.isLeftButton((int)e.getX(1), (int)e.getY(1))) {
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                leftButton = true;
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = false;
                            }
                            else if (controls.isRightButton((int) e.getX(1), (int) e.getY(1))) {
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                rightButton = true;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButton = false;
                            }
                            else {
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = false;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButton = false;
                            }
                            break;
                        case 1:
                            if (controls.isMiddleButton((int) e.getX(1), (int) e.getY(1))) {
                                gameState = GAME_PAUSED;
                                level.pauseMusic();
                            } else if (controls.isLeftButton((int) e.getX(), (int) e.getY())) {
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                leftButton = true;
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = false;
                            } else if (controls.isRightButton((int) e.getX(), (int) e.getY())) {
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                rightButton = true;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButton = false;
                            } else {
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = false;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButton = false;
                            }
                            break;
                    }
                }
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    Log.v("_______onTouchEvent", "X: " + e.getX() + " Y: " + e.getY());
                    leftButton = false;
                    rightButton = false;
                    leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                    rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                    middleButtonColor = MIDDLE_BUTTON_COLOR_UP;
                    if (controls.isMiddleButton((int)e.getX(), (int)e.getY())) {
                        gameState = GAME_PAUSED;
                        level.pauseMusic();
                    }
                }
                return true;
            case GAME_RESTARTING:
            case GAME_READY:
                if (e.getAction() == MotionEvent.ACTION_MOVE) {
                    try {
                        if (!controls.isLeftButton((int) e.getX(), (int) e.getY()) && goLeft && e.getX(1) < 0) {
                            leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                            leftButton = false;
                        } else if (!controls.isRightButton((int) e.getX(), (int) e.getY()) && goRight && e.getX(1) < 0) {
                            rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                            rightButton = false;
                        }
                    }
                    catch (IllegalArgumentException event) {
                        if (!controls.isLeftButton((int) e.getX(), (int) e.getY()) && goLeft) {
                            leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                            leftButton = false;
                        } else if (!controls.isRightButton((int) e.getX(), (int) e.getY()) && goRight) {
                            rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                            rightButton = false;
                        }
                    }
                }
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    Log.v("_______onTouchEvent", "ACTION DOWN. X: " + e.getX() + " Y: " + e.getY());
                    if (controls.isLeftButton((int)e.getX(), (int)e.getY())) {
                        leftButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                        leftButton = true;
                        rightButton = false;
                    }
                    if (controls.isRightButton((int)e.getX(), (int)e.getY())) {
                        rightButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                        rightButton = true;
                        leftButton = false;
                    }
                    if (controls.isMiddleButton((int)e.getX(), (int)e.getY())) {
                        middleButtonColor = MIDDLE_BUTTON_COLOR_DOWN;
                        rightButton = false;
                        leftButton = false;
                    }
                }
                if (e.getActionMasked() == MotionEvent.ACTION_POINTER_DOWN) {
                    Log.v("_______onTouchEvent", "ACTION POINTER DOWN. X: " + e.getX(1) + " Y: " + e.getY(1));
                    switch (e.getActionIndex()) {
                        case 0:
                            if (controls.isLeftButton((int) e.getX(), (int) e.getY())) {
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButton = true;
                                rightButton = false;
                            }
                            if (controls.isRightButton((int) e.getX(), (int) e.getY())) {
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = true;
                                leftButton = false;
                            }
                            if (controls.isMiddleButton((int) e.getX(), (int) e.getY())) {
                                middleButtonColor = MIDDLE_BUTTON_COLOR_DOWN;
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = false;
                                leftButton = false;
                            }
                            break;
                        case 1:
                            if (controls.isLeftButton((int) e.getX(1), (int) e.getY(1))) {
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButton = true;
                                rightButton = false;
                            }
                            if (controls.isRightButton((int) e.getX(1), (int) e.getY(1))) {
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = true;
                                leftButton = false;
                            }
                            if (controls.isMiddleButton((int) e.getX(1), (int) e.getY(1))) {
                                middleButtonColor = MIDDLE_BUTTON_COLOR_DOWN;
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = false;
                                leftButton = false;
                            }
                            break;
                    }
                }
                if (e.getActionMasked() == MotionEvent.ACTION_POINTER_UP) {
                    Log.v("_______onTouchEvent","Action Index: " + e.getActionIndex() + " ACTION POINTER UP. X: " + e.getX(1) + " Y: " + e.getY(1));
                    switch (e.getActionIndex()) {
                        case 0:
                            if (controls.isMiddleButton((int) e.getX(), (int) e.getY())) {
                                gameState = GAME_IN_PLAY;
                                ball.launchBall();
                            }
                            else if (controls.isLeftButton((int)e.getX(1), (int)e.getY(1))) {
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                leftButton = true;
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = false;
                            }
                            else if (controls.isRightButton((int) e.getX(1), (int) e.getY(1))) {
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                rightButton = true;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButton = false;
                            }
                            else {
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = false;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButton = false;
                            }
                            break;
                        case 1:
                            if (controls.isMiddleButton((int) e.getX(1), (int) e.getY(1))) {
                                gameState = GAME_IN_PLAY;
                                ball.launchBall();
                            } else if (controls.isLeftButton((int) e.getX(), (int) e.getY())) {
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                leftButton = true;
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = false;
                            } else if (controls.isRightButton((int) e.getX(), (int) e.getY())) {
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_DOWN;
                                rightButton = true;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButton = false;
                            } else {
                                rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                rightButton = false;
                                leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                                leftButton = false;
                            }
                            break;
                    }
                }
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    Log.v("_______onTouchEvent", "ACTION UP. X: " + e.getX() + " Y: " + e.getY());
                    if (controls.isMiddleButton((int)e.getX(), (int)e.getY())) {
                        gameState = GAME_IN_PLAY;
                        ball.launchBall();
                    }
                    leftButton = false;
                    rightButton = false;
                    leftButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                    rightButtonColor = DIRECTIONAL_BUTTON_COLOR_UP;
                    middleButtonColor = MIDDLE_BUTTON_COLOR_UP;
                }
                Log.v("_______onTouchEvent", "rightButton: " + rightButton + " leftButton" + leftButton);
                return true;
            case GAME_PAUSED:
                if (e.getAction() == MotionEvent.ACTION_DOWN) {
                    if (controls.isMiddleButton((int) e.getX(), (int) e.getY())) {
                        middleButtonColor = MIDDLE_BUTTON_COLOR_DOWN;
                    }
                }
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    if (controls.isMiddleButton((int)e.getX(), (int)e.getY())) {
                        middleButtonColor = MIDDLE_BUTTON_COLOR_UP;
                        gameState = GAME_IN_PLAY;
                        level.resumeMusic();
                    }
                }
                return true;
            default:
                return super.onTouchEvent(e);
        }
    }

}

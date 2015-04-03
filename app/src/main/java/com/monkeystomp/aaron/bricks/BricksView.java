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
    private static final int NEW_GAME = 1;
    private static final int GAME_IN_PLAY = 2;
    private static final int GAME_PAUSED = 3;
    private static final int GAME_LOST = 4;

    // The gameState variable is the switch control for updates and renders.
    private int gameState;

    // Width and height of the surface.
    private int width, height;

    // States if the keys are pressed down.
    public boolean goLeft = false;
    public boolean goRight = false;

    // Used to keep track of loop intervals.
    private long lastTime = 0;

    // Allows for random elements in the game.
    private Random random;

    // Bitmap of the game canvas.
    private Bitmap bitmap;

    // Array of pixel data to be given to the bitmap.
    public int[] pixels;

    // The only level in the game right now.
    Level level;

    // The game's user controlled paddle.
    Paddle paddle;

    // The game ball.
    Ball ball;

    // The touch screen buttons at the bottom.
    Controls controls;

    // Paint object for the strings to be displayed.
    Paint paint;

    // The game score.
    int score = 0;

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
        level = new Level(width, height);
        paddle = new Paddle(width, height, this);
        ball = new Ball(paddle, level);
        pixels = new int[width * height];
        setMyBackgroundColor(0x000000);
        random = new Random();
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        thread = new Thread(this, "Surface-View-Thread");
        setFocusable(true);
        setFocusableInTouchMode(true);
        paint = new Paint();
        paint.setTextSize(40);
        paint.setColor(0xff888888);
        gameState = NEW_GAME;
    }

    private void setMyBackgroundColor(int color) {
        int len = pixels.length;
        for (int i = 0; i < len; i++) {
            pixels[i] = color;
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
                pixels[x + y * width] = 0xff00ff00;
            }
        }
        // A vertical line.
        int middle = width / 2;
        for (int y = topY; y < 678; y++) {
            for (int x = middle - 3; x <= middle + 3; x++) {
                pixels[x + y * width] = 0xff00ff00;
            }
        }
    }

    /**
     *
     * @param b true if you want the gameLoop to continue false if you want it to stop
     */
    public void setRunning(Boolean b) {
        this.running = b;
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
        Log.v("____________run", "Thread has reached end of run method. Thread is dying");
    }

    /**
     * The game's main update method.
     */
    private void update() {
        switch (gameState) {
            case NEW_GAME:
                paddle.update(lastTime);
                ball.followPaddle();
                if (leftButton) goLeft = true;
                if (rightButton) goRight = true;
                break;
            case GAME_IN_PLAY:
                paddle.update(lastTime);
                ball.update(lastTime);
                if (leftButton) goLeft = true;
                if (rightButton) goRight = true;
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
            //Log.v("_____________render", "Bitmap is empty, creating new one");
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        switch (gameState) {
            case NEW_GAME:
                setMyBackgroundColor(0xff000000);
                paddle.render(this);
                level.render(this);
                ball.render(this);
                controls.render(this);
                break;
            case GAME_IN_PLAY:
                setMyBackgroundColor(0xff000000);
                paddle.render(this);
                level.render(this);
                ball.render(this);
                controls.render(this);
                break;
        }


        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        c.drawBitmap(bitmap, 0, 0, null);
        c.drawText("Score: " + score, 10, 40, paint);
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
                if (gameState == NEW_GAME) {
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
    }

    boolean leftButton = false;
    boolean rightButton = false;

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        switch (gameState) {
            case GAME_IN_PLAY:
            case NEW_GAME:
                if (e.getAction() == MotionEvent.ACTION_DOWN && e.getX() < (width / 2) - 3 && e.getY() > 547) {
                    leftButton = true;
                    rightButton = false;
                }
                if (e.getAction() == MotionEvent.ACTION_DOWN && e.getX() > (width / 2) + 3 && e.getY() > 547) {
                    leftButton = false;
                    rightButton = true;
                }
                if (e.getAction() == MotionEvent.ACTION_UP) {
                    leftButton = false;
                    rightButton = false;
                    goRight = false;
                    goLeft = false;
                }
                return true;
            default:
                return super.onTouchEvent(e);
        }
    }

}

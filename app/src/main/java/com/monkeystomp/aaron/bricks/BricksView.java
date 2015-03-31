/**
 * A basic brick breaker game to learn many basic aspects of android game programming.
 */

package com.monkeystomp.aaron.bricks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.KeyEvent;
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

    // Object variable for SurfaceHolder class.
    // Used to lock canvas and unlockCanvasAndPost
    private SurfaceHolder mSurfaceHolder;

    // Used to allow the game loop.
    // When set false, game thread will reach end of run method and die.
    private boolean running = false;

    // Width and height of the surface.
    private int width, height;

    // Width and height of the paddle.
    private static final int PADDLE_WIDTH = 75;
    private static final int PADDLE_HEIGHT = 7;

    // Bounds for the paddle x location.
    private static final int PADDLE_LEFT_BOUND = 3;
    private static final int PADDLE_RIGHT_BOUND = 403;

    // Speed of animation.
    private static final int PADDLE_SPEED_SEC = 170;

    // States if the keys are pressed down.
    private boolean goLeft = false;
    private boolean goRight = false;

    // Coordinates of the paddle.
    private double paddleX;
    private int paddleY;

    // Color of the paddle.
    private int paddleColor = 0xffffff;

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
        level = new Level(width, height);
        paddleX = (width / 2) - 37;
        paddleY = height - 170;
        pixels = new int[width * height];
        setMyBackgroundColor(0x000000);
        random = new Random();
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        thread = new Thread(this, "Surface-View-Thread");
        setFocusable(true);
        setFocusableInTouchMode(true);
    }

    private void setMyBackgroundColor(int color) {
        int len = pixels.length;
        for (int i = 0; i < len; i++) {
            pixels[i] = color;
        }
    }

    private void drawPaddle() {
        for (int y = 0; y < PADDLE_HEIGHT; y++) {
            for (int x = 0; x < PADDLE_WIDTH; x++) {
                int xa = (int)paddleX + x;
                pixels[(xa) + (paddleY - 3 + y) * width] = 0xffff0000;
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
        long timer = System.currentTimeMillis();
        int frames = 0;
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
                Log.v("________________run", "  |  FPS: " + frames + " `PaddleX " + paddleX);
                frames = 0;
            }
        }
        Log.v("____________run", "Thread has reached end of run method. Thread is dying");
    }

    /**
     * The game's main update method.
     */
    private void update() {
        long now = System.currentTimeMillis();
        if (now < lastTime) return;
        else {
            if (goLeft && paddleX > PADDLE_LEFT_BOUND){
                double elapsed = (now - lastTime) / 1000.0;
                paddleX -= PADDLE_SPEED_SEC * elapsed;
            }
            else if (goRight && paddleX < PADDLE_RIGHT_BOUND){
                double elapsed = (now - lastTime) / 1000.0;
                paddleX += PADDLE_SPEED_SEC * elapsed;
            }
            if (paddleX < PADDLE_LEFT_BOUND) paddleX = PADDLE_LEFT_BOUND;
            else if (paddleX > PADDLE_RIGHT_BOUND) paddleX = PADDLE_RIGHT_BOUND;
        }
        lastTime = now;
    }

    /**
     * The game's main render method.
     * @param c - A Canvas returned from the SurfaceHolder.
     */
    private void render(Canvas c) {
        if (bitmap == null) {
            Log.v("_____________render", "Bitmap is empty, creating new one");
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        }
        setMyBackgroundColor(0xff000000);
        // draw the paddle
        drawPaddle();
        level.render(this);

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        c.drawBitmap(bitmap, 0, 0, null);
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
            default:
                return onKeyUp(keyCode, msg);
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

}

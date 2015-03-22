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

    private short anim = 0;

    private Random random;

    private Bitmap bitmap;

    // Array of pixel data to be given to the bitmap
    private int[] pixels;

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

        pixels = new int[width * height];
        setMyBackgroundColor(0xffff0000);

        random = new Random();

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        thread = new Thread(this, "Surface-View-Thread");

        setFocusable(true);
    }

    private void setMyBackgroundColor(int color) {
        int len = pixels.length;
        for (int i = 0; i < len; i++) {
            pixels[i] = color;
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
        long lastTime = System.nanoTime();
        long timer = System.currentTimeMillis();
        double ns = 1000000000.0 / 60.0;
        double delta = 0.0;
        int frames = 0;
        int updates = 0;
        Canvas c;
        while(running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            while (delta >= 1) {
                update();
                updates++;
                delta--;
            }
            frames++;
            c = mSurfaceHolder.lockCanvas();
            render(c);
            mSurfaceHolder.unlockCanvasAndPost(c);
            if (System.currentTimeMillis() - timer >= 1000) {
                timer = System.currentTimeMillis();
                Log.v("________________run", "UPS: " + updates + "  |  FPS: " + frames + " Timer: " + timer);
                updates = 0;
                frames = 0;
            }
        }
        Log.v("____________run", "Thread has reached end of run method. Thread is dying");
    }

    /**
     * The game's main update method.
     */
    private void update() {
        if (anim > 1000) anim = 0;
        else anim++;
        if (anim >= 120) {
            Log.v("__________update", "Randomizing background color");
            setMyBackgroundColor(random.nextInt(Math.abs(0xffffff)));
            anim = 0;
        }
    }

    /**
     * The game's main render method.
     * @param c - A Canvas returned from the SurfaceHolder.
     */
    private void render(Canvas c) {
        if (bitmap == null) {
            Log.v("_____________render", "Bitmap is empty, creating new one");
            bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);
        }

        /*for (int i = 0; i < pixels.length; i++) {
            pixels[i] = 0xff517EE0;
        }*/

        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        c.drawBitmap(bitmap, 0, 0, null);
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

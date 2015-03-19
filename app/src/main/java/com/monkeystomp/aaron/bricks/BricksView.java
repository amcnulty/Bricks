package com.monkeystomp.aaron.bricks;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created by Aaron on 3/18/2015.
 */
class BricksView extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    // Thread used to make the game loop and decide what is to be drawn to the Canvas
    private Thread thread;

    // Object variable for SurfaceHolder class.
    // Used to lock canvas and unlockCanvasAndPost
    private SurfaceHolder mSurfaceHolder;

    private boolean running = false;

    /**
     * Constructor for setting up the SurfaceView with the SurfaceHolder.
     * Game thread gets instantiated here.
     * @param context
     * @param attributeSet
     */
    public BricksView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

        thread = new Thread(this, "Surface-View-Thread");

        setFocusable(true);
    }

    /**
     *
     * @param b true if you want the gameLoop to continue false if you want it to stop
     */
    private void setRunning(Boolean b) {
        this.running = b;
    }

    /**
     * This is where the game loop will exist
     */
    @Override
    public void run() {
        while(running) {
            try {
                thread.sleep(1000);
            }
            catch (InterruptedException e) {}
            Log.v("Game Loop", "Running");
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
        thread.start();
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
        try {
            thread.join();
        }
        catch (InterruptedException e) {
            Log.e("SurfaceDestroyed", "Interrupted Exception");
        }
    }

}

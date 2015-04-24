package com.monkeystomp.aaron.bricks;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Aaron on 3/30/2015.
 */
public class Level {

    // Dimensions of the screen.
    public int width, height;

    // The score on this level.
    private int score = 0;

    // The application context.
    Context context;

    // Used to load in the level's sounds.
    SoundPool soundPool;
    MediaPlayer mediaPlayer;

    // Id's of the different sounds.
    int wallBounceId;
    int brickBreakId;

    // Handle for BricksView
    BricksView view;

    // The game paddle.
    Paddle paddle;

    private ArrayList<Block> blocks = new ArrayList<>();

    public Level(int width, int height, Context context, BricksView view) {
        this.context = context;
        this.width = width;
        this.height = height;
        this.view = view;
        paddle = new Paddle(width, height, this);
        addBlocks();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder().build();
            soundPool = new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(audioAttributes).build();
        }
        else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);
        }
        startMusic();
        brickBreakId = soundPool.load(context, R.raw.brickbreak, 1);
        wallBounceId = soundPool.load(context, R.raw.wallbounce, 1);
    }

    public void startMusic() {
        mediaPlayer = MediaPlayer.create(context, R.raw.classical_a_minor);
        mediaPlayer.setLooping(true);
        mediaPlayer.start();
    }

    public void stopMusic() {
        mediaPlayer.stop();
        mediaPlayer.release();
    }

    public void pauseMusic() {
        mediaPlayer.pause();
    }

    public void resumeMusic() {
        mediaPlayer.start();
    }

    private void addBlocks() {
        // Nine blocks at the top
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 80, Block.PLAIN_BLOCK, this));
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 80 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, Block.GOLD_BLOCK, this));
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 80 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2, Block.PLAIN_BLOCK, this));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 80, Block.PLAIN_BLOCK, this));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 80 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, Block.GOLD_BLOCK, this));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 80 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2, Block.PLAIN_BLOCK, this));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 80, Block.PLAIN_BLOCK, this));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 80 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, Block.GOLD_BLOCK, this));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 80 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2, Block.PLAIN_BLOCK, this));

        // 8 blocks below
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 280, Block.PLAIN_BLOCK, this));
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 280 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, Block.PLAIN_BLOCK, this));
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 280 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2, Block.PLAIN_BLOCK, this));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 280, Block.GOLD_BLOCK, this));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 280 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, Block.SILVER_BLOCK, this));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 280, Block.PLAIN_BLOCK, this));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 280 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, Block.PLAIN_BLOCK, this));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 280 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2, Block.PLAIN_BLOCK, this));
    }

    public boolean blockHere(double x, double y) {
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i).blockHere(x, y)) {
                soundPool.play(brickBreakId, 1, 1, 1, 0, 1);
                return true;
            }
        }
        return false;
    }

    public boolean wallHere(double x, double y) {
        if (x <= 40) {
            soundPool.play(wallBounceId, .5f, .5f, 1, 0, .75f);
            return true;
        }
        else if (x >= width - 40) {
            soundPool.play(wallBounceId, .5f, .5f, 1, 0, .75f);
            return true;
        }
        else if (y <= 20) {
            soundPool.play(wallBounceId, .5f, .5f, 1, 0, .75f);
            return true;
        }
        return false;
    }

    public boolean paddleHere(double x, double y) {
        if (paddle.paddleHere(x, y)) {
            soundPool.play(wallBounceId, .5f, .5f, 1, 0, .75f);
            return true;
        }
        return false;
    }

    public void increaseScore(int blockPointValue) {
        score += blockPointValue;
    }

    public void update(long lastTime, BricksView view) {
        paddle.update(lastTime, view);
        view.updateScore(score);
    }

    public void render(BricksView view) {
        for(int i = 0; i < blocks.size(); i++) {
            if (!blocks.get(i).isBroken()) view.drawBlock(blocks.get(i).getX(), blocks.get(i).getY(), blocks.get(i).blockColor);
        }
        paddle.render(view);
    }

}

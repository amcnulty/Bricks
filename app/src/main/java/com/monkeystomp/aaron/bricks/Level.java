package com.monkeystomp.aaron.bricks;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

/**
 * Created by Aaron on 3/30/2015.
 */
public class Level {

    // Dimensions of the screen.
    public int width, height;

    // The score on this level.
    private int score = 0;
    private int scoreMultiplier = 1;

    // The number of balls remaining on the level.
    public int ballsRemaining;

    // Variables to control the multiplier.
    private boolean showMultiplier = false;
    private long multiplierTimer;

    // True if all blocks have been broken.
    private boolean levelCleared = false;

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

    // Random class
    Random random;

    // The game paddle.
    Paddle paddle;

    private ArrayList<Block> blocks = new ArrayList<>();
    private ArrayList<long[]> multipliers = new ArrayList<>();

    public Level(int width, int height, Context context, BricksView view) {
        this.context = context;
        this.width = width;
        this.height = height;
        this.view = view;
        random = new Random();
        ballsRemaining = 3;
        paddle = new Paddle(width, height, this);
        addBlocks();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes audioAttributes = new AudioAttributes.Builder().build();
            soundPool = new SoundPool.Builder().setMaxStreams(10).setAudioAttributes(audioAttributes).build();
        }
        else {
            soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 1);
        }
        mediaPlayer = MediaPlayer.create(context, R.raw.classical_a_minor);
        startMusic();
        brickBreakId = soundPool.load(context, R.raw.brickbreak, 1);
        wallBounceId = soundPool.load(context, R.raw.wallbounce, 1);
    }

    public void startMusic() {
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

    public void muteMusic() {
        mediaPlayer.setVolume(0, 0);
    }

    public void unMuteMusic() {
        mediaPlayer.setVolume(1, 1);
    }

    // Currently not working!
    public void changeTrack() {
        mediaPlayer.reset();
        try {
            mediaPlayer.setDataSource(context, Uri.parse("android.resource://com.monkeystomp.aaron/" + R.raw.classical_beethoven_pathetique));
            mediaPlayer.prepareAsync();
            startMusic();
        }
        catch (IOException e) {
            Log.v(null, "File Not Found!");
        }
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
        score += blockPointValue * scoreMultiplier;
    }

    public void increaseMultiplier(int multiplier) {
        if (multiplier > 1) {
            showMultiplier = true;
            multipliers.add(new long[]{multiplier, System.currentTimeMillis()});
            multiplierTimer = System.currentTimeMillis();
            scoreMultiplier *= multiplier;
        }
    }

    public void setScoreMultiplier() {
        for (int i = 0; i < multipliers.size(); i++) {
            if (System.currentTimeMillis() > multipliers.get(i)[1] + 8000) {
                scoreMultiplier /= multipliers.get(i)[0];
                multipliers.remove(i);
            }
        }
    }

    private void toggleMultiplier(int number) {
        switch (number) {
            case 1:
                if (System.currentTimeMillis() % 500 > 250) {
                    view.drawMultiplier(scoreMultiplier);
                }
                else view.eraseMultiplier();
                break;
            case 2:
                if (System.currentTimeMillis() % 250 > 125) {
                    view.drawMultiplier(scoreMultiplier);
                }
                else view.eraseMultiplier();
                break;
        }
    }

    public void lostBall() {
        showMultiplier = false;
        scoreMultiplier = 1;
        multipliers.clear();
        ballsRemaining--;
        if (ballsRemaining == 0) {
            view.gameState = view.GAME_LOST;
            view.startEndgameMessage();
        }
    }

    public void resetLevel() {
        ballsRemaining = 3;
        paddle.x = (width / 2);
        paddle.y = height - 270;
        score = 0;
        scoreMultiplier = 1;
        multipliers.clear();
        showMultiplier = false;
        stopMusic();
        startMusic();
        for (int i = 0; i < blocks.size(); i++) {
            blocks.get(i).broken = false;
            if (blocks.get(i).blockType == Block.BANANA_BLOCK) {
                Log.v("_____________resetLevel", "THIS IS A BANANA BLOCK CHANGING TO A PLAIN BLOCK. INDEX #: " + i);
                blocks.get(i).setBlockType(Block.PLAIN_BLOCK);
            }
            if (blocks.get(i).blockType == Block.PLAIN_BLOCK) {
                Log.v("_____________resetLevel", "THIS IS A PLAIN BLOCK. INDEX #: " + i);
                if (random.nextInt(9) == 0) {
                    Log.v("_____________resetLevel", "THIS IS A PLAIN BLOCK TURNING INTO A BANANA BLOCK. INDEX #: " + i);
                    blocks.get(i).setBlockType(Block.BANANA_BLOCK);
                }
            }
        }
    }

    public void checkForVictory() {
        for (int i = 0; i < blocks.size(); i++) {
            if (!blocks.get(i).broken) break;
            if (i == blocks.size() - 1) {
                levelCleared = true;
            }
        }
    }

    public void update(long lastTime, BricksView view) {
        paddle.update(lastTime, view);
        view.updateScore(score);
        setScoreMultiplier();
        if (System.currentTimeMillis() > multiplierTimer + 8000 && showMultiplier) {
            showMultiplier = false;
            scoreMultiplier = 1;
            view.eraseMultiplier();
        }
        if (levelCleared) {
            view.gameState = view.LEVEL_CLEARED;
            view.startEndgameMessage();
            levelCleared = false;
        }
    }

    public void render(BricksView view) {
        for(int i = 0; i < blocks.size(); i++) {
            if (!blocks.get(i).isBroken()) view.drawBlock(blocks.get(i).getX(), blocks.get(i).getY(), blocks.get(i).blockColor);
        }
        paddle.render(view);
        if (System.currentTimeMillis() > multiplierTimer + 7000 && showMultiplier) {
            toggleMultiplier(2);
        }
        else if (System.currentTimeMillis() > multiplierTimer + 6000 && showMultiplier) {
            toggleMultiplier(1);
        }
        else if (showMultiplier) {
            view.drawMultiplier(scoreMultiplier);
        }
    }

}

package com.monkeystomp.aaron.bricks;

/**
 * Created by Aaron on 3/30/2015.
 */
public class Block {

    // Coordinate of the block
    private int x, y;

    public static final int BLOCK_WIDTH = 50;
    public static final int HALF_BLOCK_WIDTH = BLOCK_WIDTH / 2;
    public static final int BLOCK_HEIGHT = 25;

    public static final int BLOCK_INSET = 10;

    // Various block types.
    public static final int PLAIN_BLOCK = 1;
    public static final int GOLD_BLOCK = 2;
    public static final int SILVER_BLOCK = 3;

    // Corresponding block colors.
    public static final int PLAIN_BLOCK_COLOR = 0xff888888;
    public static final int GOLD_BLOCK_COLOR = 0xffD4AF37;
    public static final int SILVER_BLOCK_COLOR = 0xffE6E8FA;

    // Corresponding block point values.
    public static final int PLAIN_BLOCK_POINT_VALUE = 100;
    public static final int GOLD_BLOCK_POINT_VALUE = 500;
    public static final int SILVER_BLOCK_POINT_VALUE = 250;

    // True if broken false if not.
    public boolean broken = false;

    // Color of this block.
    public int blockColor;

    // Point value of this block.
    public int blockPointValue;

    // Multiplier value of this block.
    public int multiplier;

    // Handle to the Level it is associated with to send score information.
    Level level;

    public Block(int x, int y, int blockType, Level level) {
        this.x = x;
        this.y = y;
        this.level = level;
        switch(blockType) {
            case PLAIN_BLOCK:
                blockColor = PLAIN_BLOCK_COLOR;
                blockPointValue = PLAIN_BLOCK_POINT_VALUE;
                multiplier = 1;
                break;
            case GOLD_BLOCK:
                blockColor = GOLD_BLOCK_COLOR;
                blockPointValue = GOLD_BLOCK_POINT_VALUE;
                multiplier = 3;
                break;
            case SILVER_BLOCK:
                blockColor = SILVER_BLOCK_COLOR;
                blockPointValue = SILVER_BLOCK_POINT_VALUE;
                multiplier = 2;
                break;
        }
    }

    public boolean blockHere(double x, double y) {
        if (x >= this.x && x <= this.x + BLOCK_WIDTH && y >= this.y && y <= this.y + BLOCK_HEIGHT && !broken) {
            broken = true;
            level.increaseScore(blockPointValue);
            level.startMultiplier(multiplier);
            level.checkForVictory();
            return true;
        }
        return false;
    }

    public boolean isBroken() {
        return broken;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

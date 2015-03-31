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

    public int blockColor;

    /**
     *
     * @param x x location of the block. (left side of block)
     * @param y y location of the block. (top of the block)
     */

    public Block(int x, int y) {
        this.x = x;
        this.y = y;
        blockColor = 0xff888888;
    }

    public Block(int x, int y, int blockColor) {
        this.x = x;
        this.y = y;
        this.blockColor = blockColor;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}

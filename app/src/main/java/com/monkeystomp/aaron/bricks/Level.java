package com.monkeystomp.aaron.bricks;

import java.util.ArrayList;

/**
 * Created by Aaron on 3/30/2015.
 */
public class Level {

    // Dimensions of the screen.
    private int width, height;

    // Gold block color.
    private static final int GOLD_BLOCK = 0xffD4AF37;

    private ArrayList<Block> blocks = new ArrayList<>();

    public Level(int width, int height) {
        this.width = width;
        this.height = height;
        addBlocks();
    }

    private void addBlocks() {
        // Nine blocks at the top
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 80));
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 80 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, GOLD_BLOCK));
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 80 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 80));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 80 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, GOLD_BLOCK));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 80 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 80));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 80 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, GOLD_BLOCK));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 80 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2));

        // 8 blocks below
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 280));
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 280 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET));
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 280 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 280, GOLD_BLOCK));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 280 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, GOLD_BLOCK));
        //blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 180 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 280));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 280 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 280 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2));
    }

    public void update() {

    }

    public void render(BricksView view) {
        for(int i = 0; i < blocks.size(); i++) {
            view.drawBlock(blocks.get(i).getX(), blocks.get(i).getY(), blocks.get(i).blockColor);
        }
    }

}

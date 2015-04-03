package com.monkeystomp.aaron.bricks;

import java.util.ArrayList;

/**
 * Created by Aaron on 3/30/2015.
 */
public class Level {

    // Dimensions of the screen.
    private int width, height;

    private ArrayList<Block> blocks = new ArrayList<>();

    public Level(int width, int height) {
        this.width = width;
        this.height = height;
        addBlocks();
    }

    private void addBlocks() {
        // Nine blocks at the top
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 80, Block.PLAIN_BLOCK));
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 80 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, Block.GOLD_BLOCK));
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 80 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2, Block.PLAIN_BLOCK));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 80, Block.PLAIN_BLOCK));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 80 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, Block.GOLD_BLOCK));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 80 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2, Block.PLAIN_BLOCK));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 80, Block.PLAIN_BLOCK));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 80 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, Block.GOLD_BLOCK));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 80 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2, Block.PLAIN_BLOCK));

        // 8 blocks below
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 280, Block.PLAIN_BLOCK));
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 280 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, Block.PLAIN_BLOCK));
        blocks.add(new Block((width / 3) - Block.HALF_BLOCK_WIDTH, 280 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2, Block.PLAIN_BLOCK));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 280, Block.GOLD_BLOCK));
        blocks.add(new Block((width / 2) - Block.HALF_BLOCK_WIDTH, 280 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, Block.SILVER_BLOCK));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 280, Block.PLAIN_BLOCK));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 280 + Block.BLOCK_HEIGHT + Block.BLOCK_INSET, Block.PLAIN_BLOCK));
        blocks.add(new Block(((2 * width) / 3) - Block.HALF_BLOCK_WIDTH, 280 + (Block.BLOCK_HEIGHT + Block.BLOCK_INSET) * 2, Block.PLAIN_BLOCK));
    }

    public boolean blockHere(double x, double y) {
        boolean collision = false;
        for (int i = 0; i < blocks.size(); i++) {
            if (blocks.get(i).blockHere(x, y)) collision = true;
        }
        return collision;
    }

    public void update() {

    }

    public void render(BricksView view) {
        for(int i = 0; i < blocks.size(); i++) {
            view.drawBlock(blocks.get(i).getX(), blocks.get(i).getY(), blocks.get(i).blockColor);
        }
    }

}

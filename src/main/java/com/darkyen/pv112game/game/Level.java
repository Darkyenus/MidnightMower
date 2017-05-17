package com.darkyen.pv112game.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.darkyen.pv112game.gl.Environment;
import com.darkyen.pv112game.gl.Model;

/**
 *
 */
public final class Level {

    public final static byte GRASS_00 = 1;
    public final static byte GRASS_10 = 1<<1;
    public final static byte GRASS_01 = 1<<2;
    public final static byte GRASS_11 = 1<<3;

    public final int width, height;
    public final boolean[][] tileTraversable;
    public final byte[][] tileGrass;
    public int remainingGrass = 0;

    public float playerX, playerY;
    public float playerAngle;

    public Level(int width, int height) {
        this.width = width;
        this.height = height;
        this.tileTraversable = new boolean[width][height];
        this.tileGrass = new byte[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0 || y == 0 || x + 1 == width || y + 1 == height || (x == width/2 && y == height /2)) {
                    tileTraversable[x][y] = false;
                } else {
                    tileTraversable[x][y] = true;
                    playerX = x + 0.5f;
                    playerY = y + 0.5f;
                    playerAngle = 33f;
                }
                byte grass = tileGrass[x][y] = (byte) MathUtils.random.nextInt(1<<4);
                for (int i = 0; i < 4; i++) {
                    if ((grass & (1 << i)) != 0) {
                        remainingGrass++;
                    }
                }
            }
        }
    }

    public boolean traversable(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return false;
        return tileTraversable[x][y];
    }
}

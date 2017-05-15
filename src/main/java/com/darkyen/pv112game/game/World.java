package com.darkyen.pv112game.game;

import com.badlogic.gdx.math.Vector3;
import com.darkyen.pv112game.gl.Environment;
import com.darkyen.pv112game.gl.Model;

/**
 *
 */
public final class World {

    public final int width, height;
    public final boolean[][] tileTraversable;
    public final TileType[][] tileTypes;

    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.tileTraversable = new boolean[width][height];
        this.tileTypes = new TileType[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (x == 0 || y == 0 || x + 1 == width || y + 1 == height) {
                    tileTraversable[x][y] = false;
                } else {
                    tileTraversable[x][y] = true;
                }
                tileTypes[x][y] = (x + y) % 2 == 1 ? TileType.GRASS : TileType.STONE;
            }
        }
    }

    public void draw(Environment environment) {
        final int TILE_SCALE = 3;
        final float LOW_Z = 0;
        final float HIGH_Z = 0;
        final Vector3 position = new Vector3();

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                position.x = x * TILE_SCALE;
                position.y = tileTraversable[x][y] ? LOW_Z : HIGH_Z;
                position.z = y * TILE_SCALE;

                environment.draw(Models.PlateGrass01, position);
            }
        }
    }

    public enum TileType {
        GRASS,
        STONE
    }
}

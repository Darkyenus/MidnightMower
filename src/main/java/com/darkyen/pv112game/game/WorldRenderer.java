package com.darkyen.pv112game.game;

import com.badlogic.gdx.math.Vector3;
import com.darkyen.pv112game.gl.Environment;
import com.darkyen.pv112game.gl.Model;

/**
 *
 */
public final class WorldRenderer {

    private static Model[] GRASS = {
            Models.Plant101,
            Models.Plant201,
            Models.Plant301,
            Models.Grass01,
            Models.Grass01,
            Models.Grass01,
            Models.FlowerRed01,
            Models.FlowerTallRed01,
            Models.FlowerYellow01,
            Models.FlowerYellow02,
    };

    private static Model pickGrass(int x, int y, byte tile) {
        return GRASS[((31 ^ tile) * x + y) % GRASS.length];
    }

    private static int rotation(int x, int y, byte tile) {
        return ((31 ^ tile) * x + y) * 15;
    }

    public static void render(Level level, Environment environment) {
        final float TILE_SCALE = 3f;
        final Vector3 position = new Vector3();

        for (int x = 0; x < level.width; x++) {
            for (int y = 0; y < level.height; y++) {
                final boolean traversable = level.tileTraversable[x][y];

                if (traversable) {
                    position.x = x * TILE_SCALE;
                    position.y = 0f;
                    position.z = y * TILE_SCALE;
                    environment.draw(Models.PlateGrass01, position);

                    final byte grass = level.tileGrass[x][y];
                    if ((grass & Level.GRASS_00) != 0) {
                        position.x = x * TILE_SCALE + 0.75f;
                        position.y = 0.28f;
                        position.z = y * TILE_SCALE - 0.9f;
                        environment.draw(pickGrass(x, y, Level.GRASS_00), position, rotation(x, y, Level.GRASS_00));
                    }

                    if ((grass & Level.GRASS_10) != 0) {
                        position.x = x * TILE_SCALE + 0.75f + 0.75f;
                        position.y = 0.28f;
                        position.z = y * TILE_SCALE - 0.9f;
                        environment.draw(pickGrass(x, y, Level.GRASS_10), position, rotation(x, y, Level.GRASS_10));
                    }

                    if ((grass & Level.GRASS_01) != 0) {
                        position.x = x * TILE_SCALE + 0.75f;
                        position.y = 0.28f;
                        position.z = y * TILE_SCALE - 0.9f - 0.75f;
                        environment.draw(pickGrass(x, y, Level.GRASS_01), position, rotation(x, y, Level.GRASS_01));
                    }

                    if ((grass & Level.GRASS_11) != 0) {
                        position.x = x * TILE_SCALE + 0.75f + 0.75f;
                        position.y = 0.28f;
                        position.z = y * TILE_SCALE - 0.9f - 0.75f;
                        environment.draw(pickGrass(x, y, Level.GRASS_11), position, rotation(x, y, Level.GRASS_11));
                    }

                } else {
                    // Long live position hacks.
                    // Those assets are shit.

                    if (level.traversable(x+1, y)) {
                        position.x = x * TILE_SCALE + 2.5f;
                        position.y = -1.95f;
                        position.z = y * TILE_SCALE;
                        environment.draw(Models.GreyCliffBottom01, position);
                        position.y = 0.01f;
                        environment.draw(Models.GreyCliffTop01, position);
                    }
                    if (level.traversable(x-1, y)) {
                        position.x = x * TILE_SCALE + 0.5f;
                        position.y = -1.95f;
                        position.z = y * TILE_SCALE - 3f;
                        environment.draw(Models.GreyCliffBottom01, position, 180f);
                        position.y = 0.01f;
                        environment.draw(Models.GreyCliffTop01, position, 180f);
                    }
                    if (level.traversable(x, y+1)) {
                        position.x = x * TILE_SCALE;
                        position.y = -1.95f;
                        position.z = y * TILE_SCALE - 0.5f;
                        environment.draw(Models.GreyCliffBottom01, position, 270f);
                        position.y = 0.01f;
                        environment.draw(Models.GreyCliffTop01, position, 270f);
                    }
                    if (level.traversable(x, y-1)) {
                        position.x = x * TILE_SCALE + 3f;
                        position.y = -1.95f;
                        position.z = y * TILE_SCALE - 2.5f;
                        environment.draw(Models.GreyCliffBottom01, position, 90f);
                        position.y = 0.01f;
                        environment.draw(Models.GreyCliffTop01, position, 90f);
                    }

                    // Corners
                    if (level.traversable(x+1, y+1) && !level.traversable(x+1, y) && !level.traversable(x, y+1)) {
                        position.x = x * TILE_SCALE+1.9f;
                        position.y = -1.95f;
                        position.z = y * TILE_SCALE;
                        environment.draw(Models.GreyCliffBottomCorner01, position);
                        position.x = x * TILE_SCALE+2.1f;
                        position.y = 0.01f;
                        environment.draw(Models.GreyCliffTopCorner01, position);
                    }

                    if (level.traversable(x-1, y+1) && !level.traversable(x-1, y) && !level.traversable(x, y+1)) {
                        position.x = x * TILE_SCALE;
                        position.y = -1.95f;
                        position.z = y * TILE_SCALE-1.15f;
                        environment.draw(Models.GreyCliffBottomCorner01, position, 270f);
                        position.y = 0.01f;
                        position.z = y * TILE_SCALE-0.92f;
                        environment.draw(Models.GreyCliffTopCorner01, position, 270f);
                    }

                    if (level.traversable(x+1, y-1) && !level.traversable(x+1, y) && !level.traversable(x, y-1)) {
                        position.x = x * TILE_SCALE+2.9999f;
                        position.y = -1.95f;
                        position.z = y * TILE_SCALE-1.87f;
                        environment.draw(Models.GreyCliffBottomCorner01, position, 90f);
                        position.z = y * TILE_SCALE-2.08f;
                        position.y = 0.01f;
                        environment.draw(Models.GreyCliffTopCorner01, position, 90f);
                    }

                    if (level.traversable(x-1, y-1) && !level.traversable(x-1, y) && !level.traversable(x, y-1)) {
                        position.x = x * TILE_SCALE+1.15f;
                        position.y = -1.95f;
                        position.z = y * TILE_SCALE-3f;
                        environment.draw(Models.GreyCliffBottomCorner01, position, 180f);
                        position.x = x * TILE_SCALE+0.9f;
                        position.y = 0.01f;
                        environment.draw(Models.GreyCliffTopCorner01, position, 180f);
                    }
                }
            }
        }

        {
            position.set(level.playerX * TILE_SCALE, 0f, level.playerY * TILE_SCALE);
            environment.draw(Models.LawnMower, position, level.playerAngle + System.currentTimeMillis() % 10000);
        }
    }

}

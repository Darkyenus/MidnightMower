package com.darkyen.pv112game.game;

import com.badlogic.gdx.math.Vector3;
import com.darkyen.pv112game.gl.Environment;
import com.darkyen.pv112game.gl.Model;

/**
 *
 */
public final class WorldRenderer {

    private static Model[] GRASS = {
            Models.TallGrass01,
            Models.TallGrass02,
    };

    private static Model pickGrass(int x, int y, byte tile) {
        return GRASS[((31 ^ tile) * x + y) % GRASS.length];
    }

    private static int rotation(int x, int y, byte tile) {
        return ((31 ^ tile) * x + y) * 15;// + (int)((System.currentTimeMillis() % 36000) / 10);
    }

    private static float scale(int x, int y, byte tile) {
        return ((((31 ^ tile) * x + y) % 100) / 100f) * 0.4f + 0.8f;
    }


    public static void render(Level level, Environment environment) {
        final Vector3 position = new Vector3();

        for (int x = -1; x <= level.width; x++) {
            for (int y = -1; y <= level.height; y++) {
                final boolean traversable = level.traversable(x, y);;

                if (traversable) {
                    position.x = x + 0.5f;
                    position.y = 0f;
                    position.z = y + 0.5f;
                    environment.draw(Models.GrassTile, position);

                    final byte grass = level.tileGrass[x][y];
                    if ((grass & Level.GRASS_00) != 0) {
                        position.x = x + 0.25f;
                        position.y = 0f;
                        position.z = y + 0.25f;
                        environment.draw(pickGrass(x, y, Level.GRASS_00), position, rotation(x, y, Level.GRASS_00), scale(x, y, Level.GRASS_00));
                    }

                    if ((grass & Level.GRASS_10) != 0) {
                        position.x = x + 0.75f;
                        position.y = 0f;
                        position.z = y + 0.25f;
                        environment.draw(pickGrass(x, y, Level.GRASS_10), position, rotation(x, y, Level.GRASS_10), scale(x, y, Level.GRASS_10));
                    }

                    if ((grass & Level.GRASS_01) != 0) {
                        position.x = x + 0.25f;
                        position.y = 0f;
                        position.z = y + 0.75f;
                        environment.draw(pickGrass(x, y, Level.GRASS_01), position, rotation(x, y, Level.GRASS_01), scale(x, y, Level.GRASS_01));
                    }

                    if ((grass & Level.GRASS_11) != 0) {
                        position.x = x + 0.75f;
                        position.y = 0f;
                        position.z = y + 0.75f;
                        environment.draw(pickGrass(x, y, Level.GRASS_11), position, rotation(x, y, Level.GRASS_11), scale(x, y, Level.GRASS_11));
                    }

                } else {
                    position.x = x + 0.5f;
                    position.z = y + 0.5f;

                    if (level.traversable(x+1, y)) {
                        position.y = -1f;
                        environment.draw(Models.Cliff, position, 90f);
                        position.y = 0f;
                        environment.draw(Models.Overhang, position, 90f);
                    }
                    if (level.traversable(x-1, y)) {
                        position.y = -1f;
                        environment.draw(Models.Cliff, position, 270f);
                        position.y = 0f;
                        environment.draw(Models.Overhang, position, 270f);
                    }
                    if (level.traversable(x, y+1)) {
                        position.y = -1f;
                        environment.draw(Models.Cliff, position);
                        position.y = 0f;
                        environment.draw(Models.Overhang, position);
                    }
                    if (level.traversable(x, y-1)) {
                        position.y = -1f;
                        environment.draw(Models.Cliff, position, 180f);
                        position.y = 0f;
                        environment.draw(Models.Overhang, position, 180f);
                    }

                    // Corners
                    if (level.traversable(x+1, y+1) && !level.traversable(x+1, y) && !level.traversable(x, y+1)) {
                        position.y = -1f;
                        environment.draw(Models.CliffCorner, position, 90f);
                        position.y = 0f;
                        environment.draw(Models.OverhangCorner, position, 90f);
                    }

                    if (level.traversable(x-1, y+1) && !level.traversable(x-1, y) && !level.traversable(x, y+1)) {
                        position.y = -1f;
                        environment.draw(Models.CliffCorner, position);
                        position.y = 0f;
                        environment.draw(Models.OverhangCorner, position);
                    }

                    if (level.traversable(x+1, y-1) && !level.traversable(x+1, y) && !level.traversable(x, y-1)) {
                        position.y = -1f;
                        environment.draw(Models.CliffCorner, position, 180f);
                        position.y = 0f;
                        environment.draw(Models.OverhangCorner, position, 180f);
                    }

                    if (level.traversable(x-1, y-1) && !level.traversable(x-1, y) && !level.traversable(x, y-1)) {
                        position.y = -1f;
                        environment.draw(Models.CliffCorner, position, 270f);
                        position.y = 0f;
                        environment.draw(Models.OverhangCorner, position, 270f);
                    }
                }
            }
        }

        {
            position.set(level.playerPos.x, 0f, level.playerPos.y);
            environment.draw(Models.LawnMower, position, level.playerAngle);
        }
    }

}

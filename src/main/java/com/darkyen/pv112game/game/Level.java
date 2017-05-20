package com.darkyen.pv112game.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

/**
 *
 */
public final class Level {

    public final static byte GRASS_00 = 1;
    public final static byte GRASS_10 = 1<<1;
    public final static byte GRASS_01 = 1<<2;
    public final static byte GRASS_11 = 1<<3;

    public final int order;
    public final int width, height;
    public final boolean[][] tileTraversable;
    public final byte[][] tileGrass;
    public int remainingGrass = 0;

    public final Vector2 playerPos = new Vector2();
    public int playerTileX, playerTileY;
    public float playerAngle;
    public float playerSpeed = 0.7f;
    public float playerRotationSpeed = 45f;

    public Level(int order, int width, int height, long seed) {
        this.order = order;
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
                    playerPos.x = x;
                    playerPos.y = y;
                    playerTileX = x;
                    playerTileY = y;
                    playerAngle = 33f;

                    byte grass = tileGrass[x][y] = (byte) (MathUtils.random.nextInt(1<<4) | MathUtils.random.nextInt(1<<4));
                    for (int i = 0; i < 4; i++) {
                        if ((grass & (1 << i)) != 0) {
                            remainingGrass++;
                        }
                    }
                }
            }
        }
    }

    // Code lifted from my DDA voxel/pixel tracer

    private float deltaToCross(float currentPos, int currentTile, int raySign) {
        if (raySign == 0) {
            return Float.POSITIVE_INFINITY;
        } else if (raySign > 0) {
            return currentTile + 1f - currentPos;
        } else {
            return currentPos - currentTile;
        }
    }

    public CollisionData playerMove(Vector2 by) {
        final int rayDirectionX = (int) Math.signum(by.x);
        final int rayDirectionY = (int) Math.signum(by.y);

        float t = 0f;
        final float distance = by.len();

        final float absRayX = Math.abs(by.x) / distance;
        final float absRayY = Math.abs(by.y) / distance;

        int tileX = playerTileX;
        int tileY = playerTileY;
        float remainingToCrossX = deltaToCross(playerPos.x, tileX, rayDirectionX);
        float remainingToCrossY = deltaToCross(playerPos.y, tileY, rayDirectionY);

        boolean lastCrossedX = false;
        boolean collided = false;

        while (t < distance) {
            float tX = remainingToCrossX / absRayX;
            float tY = remainingToCrossY / absRayY;

            final float stepT;
            final int nextTileX, nextTileY;

            if (tX < tY) {
                // Crossing X first
                stepT = tX;
                nextTileX = tileX + rayDirectionX;
                nextTileY = tileY;

                remainingToCrossX = 1f;
                remainingToCrossY -= stepT * absRayY;
                lastCrossedX = true;
            } else {
                // Crossing Y first
                stepT = tY;
                nextTileX = tileX;
                nextTileY = tileY + rayDirectionY;

                remainingToCrossX -= stepT * absRayX;
                remainingToCrossY = 1f;
                lastCrossedX = false;
            }

            t += stepT;

            if (t > distance) {
                t = distance;
                break;
            }

            if (!traversable(nextTileX, nextTileY)) {
                collided = true;
                break;
            }

            tileX = nextTileX;
            tileY = nextTileY;
        }

        final CollisionData collisionData = CollisionData.INSTANCE;
        collisionData.collision = collided ? (lastCrossedX ? CollisionData.COLLIDED_CROSSING_X : CollisionData.COLLIDED_CROSSING_Y) : CollisionData.NO_COLLISION;
        collisionData.distanceTravelled = t;

        if (t != 0f) {
            playerTileX = tileX;
            playerTileY = tileY;
            playerPos.mulAdd(by, t/distance);
        }

        collisionData.grassCut = false;
        if (traversable(playerTileX, playerTileY)) {
            final float tilePosX = playerPos.x - playerTileX;
            final float tilePosY = playerPos.y - playerTileY;
            final byte grassBit;
            if (tilePosX < 0.5f) {
                if (tilePosY < 0.5f) {
                    grassBit = GRASS_00;
                } else {
                    grassBit = GRASS_01;
                }
            } else {
                if (tilePosY < 0.5f) {
                    grassBit = GRASS_10;
                } else {
                    grassBit = GRASS_11;
                }
            }

            if ((tileGrass[playerTileX][playerTileY] & grassBit) != 0) {
                collisionData.grassCut = true;
                tileGrass[playerTileX][playerTileY] &= ~grassBit;
                remainingGrass--;
            }
        }

        return collisionData;
    }

    public boolean traversable(int x, int y) {
        if (x < 0 || y < 0 || x >= width || y >= height) return false;
        return tileTraversable[x][y];
    }

    public static final class CollisionData {
        private static final CollisionData INSTANCE = new CollisionData();

        public byte collision;
        public float distanceTravelled;
        public boolean grassCut;

        public static final byte NO_COLLISION = 0;
        public static final byte COLLIDED_CROSSING_X = 1;
        public static final byte COLLIDED_CROSSING_Y = 2;
    }
}

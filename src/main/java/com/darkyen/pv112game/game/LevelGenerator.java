package com.darkyen.pv112game.game;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.LongArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;

/**
 *
 */
public final class LevelGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(LevelGenerator.class);
    
    private static final Random random = new Random();

    public static LevelData generateLevel(int filledTiles, long seed) {
        final float fillRatio = 0.3f;// 30% filed
        final float tiles = filledTiles / fillRatio + 1f;
        final int width = (int) Math.sqrt(tiles);
        final int height = (int) Math.ceil(tiles / width);

        final Random random = LevelGenerator.random;
        random.setSeed(seed);

        final int[] points = genPoints((width + height) * 5, width, height, random);
        final int[][] voronoiBitmap = createVoronoiBitmap(points, width, height);
        final boolean[][] fillMap = createFillMap(voronoiBitmap, width, height, width / 2, height / 2, filledTiles);

        final LongArray playerPositions = new LongArray();
        long xSum = 0;
        long ySum = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (fillMap[x][y]) {
                    xSum += x;
                    ySum += y;
                    playerPositions.add(toKey(x, y));
                }
            }
        }
        final long playerPosition = playerPositions.random();
        final int playerX = keyX(playerPosition);
        final int playerY = keyY(playerPosition);


        final float averageX = (float) ((double)xSum / playerPositions.size);
        final float averageY = (float) ((double)ySum / playerPositions.size);
        final float toAverageX = averageX - playerX;
        final float toAverageY = averageY - playerY;

        //noinspection SuspiciousNameCombination
        int playerAngle = (int) Math.round(Math.atan2(toAverageX, toAverageY) * MathUtils.radiansToDegrees);
        if (playerAngle < 0) playerAngle += 360;

        return new LevelData(fillMap, width, height, playerX, playerY, playerAngle);
    }

    private static long toKey(int x, int y) {
        return ((long)x << 32) | ((long)y & 0xFFFF_FFFFL);
    }

    private static int keyX(long key) {
        return (int) (key >>> 32);
    }

    private static int keyY(long key) {
        return (int) (key & 0xFFFF_FFFFL);
    }

    private static boolean isConsiderable(boolean[][] fillMap, int x, int y) {
        return x >= 0 && y >= 0 && x < fillMap.length && y < fillMap[0].length && !fillMap[x][y];
    }

    private static boolean[][] createFillMap(int[][] indexMap, int width, int height, int startX, int startY, int tiles) {
        final boolean[][] map = new boolean[width][height];// Full of false
        assert width * height >= tiles;
        int remainingTiles = tiles;

        int currentIndex = -1;
        final LongArray coordStack = new LongArray(true, 100);
        final LongArray possibleNextIndexStack = new LongArray(false, 100);
        possibleNextIndexStack.add(toKey(startX, startY));

        tileLoop:
        while (remainingTiles > 0) {
            if (coordStack.size > 0) {
                // Add next from coord stack
                while (coordStack.size > 0) {
                    final long key = coordStack.removeIndex(0);
                    final int x = keyX(key);
                    final int y = keyY(key);
                    if (map[x][y]) {
                        // Already filled
                        continue;
                    }
                    // Not filled yet, fill now!
                    map[x][y] = true;
                    remainingTiles--;

                    // Populate neighbors
                    if (isConsiderable(map, x-1, y)) {
                        if (indexMap[x-1][y] == currentIndex) {
                            coordStack.add(toKey(x-1, y));
                        } else {
                            possibleNextIndexStack.add(toKey(x-1, y));
                        }
                    }

                    if (isConsiderable(map, x+1, y)) {
                        if (indexMap[x+1][y] == currentIndex) {
                            coordStack.add(toKey(x+1, y));
                        } else {
                            possibleNextIndexStack.add(toKey(x+1, y));
                        }
                    }

                    if (isConsiderable(map, x, y-1)) {
                        if (indexMap[x][y-1] == currentIndex) {
                            coordStack.add(toKey(x, y-1));
                        } else {
                            possibleNextIndexStack.add(toKey(x, y-1));
                        }
                    }

                    if (isConsiderable(map, x, y+1)) {
                        if (indexMap[x][y+1] == currentIndex) {
                            coordStack.add(toKey(x, y+1));
                        } else {
                            possibleNextIndexStack.add(toKey(x, y+1));
                        }
                    }

                    break;
                }
            } else {
                // Coord stack is empty
                possibleNextIndexStack.shuffle();

                while (possibleNextIndexStack.size > 0) {
                    final long nextKey = possibleNextIndexStack.pop();
                    // Check if this position is not filled yet
                    final int nextX = keyX(nextKey);
                    final int nextY = keyY(nextKey);
                    if (!map[nextX][nextY]) {
                        // Valid!
                        currentIndex = indexMap[nextX][nextY];
                        coordStack.add(nextKey);
                        continue tileLoop;
                    }
                }

                LOG.error("Can't continue map fill, {} tiles remaining", remainingTiles);
                break;
            }
        }

        return map;
    }

    private static int[][] createVoronoiBitmap(int[] points, int width, int height) {
        final int[][] map = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                map[x][y] = findClosestPointIndex(points, x, y);
            }
        }

        return map;
    }
    
    private static int findClosestPointIndex(int[] points, int x, int y) {
        int closest = -1;
        int closestDistance2 = Integer.MAX_VALUE;

        for (int i = 0; i < points.length; i += 2) {
            final int dX = points[i] - x;
            final int dY = points[i+1] - y;
            int dist2 = dX * dX + dY * dY;
            if (dist2 < closestDistance2) {
                closest = i/2;
                closestDistance2 = dist2;
            }
        }

        return closest;
    }
    
    private static int[] genPoints(int numPoints, int width, int height, Random random) {
        final int[] points = new int[numPoints * 2];
        for (int i = 0; i < numPoints * 2; i += 2) {
            points[i] = random.nextInt(width);
            points[i+1] = random.nextInt(height);
        }
        return points;
    }

    public static final class LevelData {
        public final boolean[][] filled;
        public final int width, height;
        public final int playerX, playerY;
        public final int playerAngle;

        public LevelData(boolean[][] filled, int width, int height, int playerX, int playerY, int playerAngle) {
            this.filled = filled;
            this.width = width;
            this.height = height;
            this.playerX = playerX;
            this.playerY = playerY;
            this.playerAngle = playerAngle;
        }
    }

    public static void main(String[] args){
        // Test voronoi bitmap
        /*
        final int size = 50;
        final int[][] voronoiBitmap = createVoronoiBitmap(genPoints(100, size, size, random), size, size);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                System.out.print(voronoiBitmap[x][y]);
            }
            System.out.println();
        }

        System.out.println();

        final boolean[][] fillMap = createFillMap(voronoiBitmap, size, size, size / 2, size / 2, size * 3);
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                System.out.print(fillMap[x][y] ? 'X' : '.');
            }
            System.out.println();
        }
        */

        long seed = 1234;

        for (int i = 1; i < 10; i++) {
            final LevelData levelData = generateLevel(i * 10, seed);
            System.out.println("\n\nTiles: "+(i*10)+" "+levelData.playerAngle);
            for (int y = 0; y < levelData.height; y++) {
                for (int x = 0; x < levelData.width; x++) {
                    if (x == levelData.playerX && y == levelData.playerY) {
                        System.out.print('P');
                    } else {
                        System.out.print(levelData.filled[x][y] ? 'X' : '.');
                    }
                }
                System.out.println();
            }
        }
    }
}

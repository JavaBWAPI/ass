package org.bk.ass.path;

/**
 * The query interface of the path finding algorithm.
 */
public interface Map {

  /**
   * Returns true, if the given position is walkable <em>and</em> within the map.
   */
  boolean isWalkable(int x, int y);

  int getWidth();

  int getHeight();

  static Map fromBooleanArray(boolean[][] map) {
    return new Map() {
      @Override
      public boolean isWalkable(int x, int y) {
        return y >= 0 && y < map[0].length && x >= 0 && x < map.length && map[x][y];
      }

      @Override
      public int getWidth() {
        return map.length;
      }

      @Override
      public int getHeight() {
        return map[0].length;
      }
    };
  }
}

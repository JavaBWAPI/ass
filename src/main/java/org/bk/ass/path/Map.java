package org.bk.ass.path;

import org.bk.ass.grid.Grid;

/** The query interface of the path finding algorithm. */
public interface Map extends Grid<Boolean> {
  static Map fromBooleanArray(boolean[][] map) {
    return new Map() {
      @Override
      public Boolean get(int x, int y) {
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

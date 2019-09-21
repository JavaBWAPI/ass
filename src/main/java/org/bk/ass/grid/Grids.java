package org.bk.ass.grid;

import bwapi.Game;
import org.openbw.bwapi4j.BWMap;

public final class Grids {
  private Grids() {
    // Utility class
  }

  public static Grid<Boolean> fromBooleanArray(boolean[][] map) {
    return new Grid<Boolean>() {
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

  public static Grid<Boolean> fromTileWalkability(Game game) {
    return new Grid<Boolean>() {
      @Override
      public int getWidth() {
        return game.mapWidth();
      }

      @Override
      public int getHeight() {
        return game.mapHeight();
      }

      @Override
      public Boolean get(int x, int y) {
        return game.isWalkable(x * 4, y * 4)
            && game.isWalkable(x * 4 + 1, y * 4)
            && game.isWalkable(x * 4, y * 4 + 1)
            && game.isWalkable(x * 4 + 1, y * 4 + 1);
      }
    };
  }

  public static Grid<Boolean> fromTileWalkability(BWMap map) {
    return new Grid<Boolean>() {
      @Override
      public int getWidth() {
        return map.mapWidth();
      }

      @Override
      public int getHeight() {
        return map.mapHeight();
      }

      @Override
      public Boolean get(int x, int y) {
        return map.isWalkable(x * 4, y * 4)
            && map.isWalkable(x * 4 + 1, y * 4)
            && map.isWalkable(x * 4, y * 4 + 1)
            && map.isWalkable(x * 4 + 1, y * 4 + 1);
      }
    };
  }
}

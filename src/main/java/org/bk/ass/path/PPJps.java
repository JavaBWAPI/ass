package org.bk.ass.path;

import org.bk.ass.grid.Grid;

import java.util.Collections;

/**
 * Jump point search. Initialize with a {@link PPMap} instance and call {@link #findPath(Position,
 * Position)}. This implementation uses preprocessed maps to improve runtime performance (~40%).
 * (Note: This is not JPS+, only verticals and horizontals are precomputed)
 */
public class PPJps {

  private final PPMap map;

  public PPJps(PPMap map) {
    this.map = map;
  }

  public PPJps(Grid<Boolean> map) {
    this(PPMap.fromMap(map));
  }

  /**
   * Searches for a path between start and end. Allows concurrent pathing requests if the map
   * implementation allows concurrent queries.
   *
   * @return a valid path or a path with infinite length if none could be found. Never returns null.
   */
  public Result findPath(Position start, Position end) {
    if (start.equals(end)) {
      return new Result(0, Collections.singletonList(start));
    }
    return new PathFinder(end, map).searchFrom(start);
  }

  private static class PathFinder extends AbstractPathFinder {

    private final PPMap map;

    PathFinder(Position target, PPMap map) {
      super(target, map);
      this.map = map;
    }

    @Override
    protected Position jumpHorizontal(int px, int py, int dx) {
      assert dx != 0;

      short x;
      if (dx < 0) {
        x = map.left(px, py);
        if (py == target.y
            && px >= target.x
            && (x < 0 && -x - 1 <= target.x || x >= 0 && x <= target.x)) {
          x = (short) target.x;
        }
      } else {
        x = map.right(px, py);
        if (py == target.y && px <= target.x && (x < 0 && -x - 1 >= target.x || x >= target.x)) {
          x = (short) target.x;
        }
      }
      if (x >= 0) {
        return new Position(x, py);
      }
      return null;
    }

    @Override
    protected Position jumpVertical(int px, int py, int dy) {
      assert dy != 0;
      short y;

      if (dy < 0) {
        y = map.up(px, py);
        if (px == target.x
            && py >= target.y
            && (y < 0 && -y - 1 <= target.y || y >= 0 && y <= target.y)) {
          y = (short) target.y;
        }
      } else {
        y = map.down(px, py);
        if (px == target.x && py <= target.y && (y < 0 && -y - 1 >= target.y || y >= target.y)) {
          y = (short) target.y;
        }
      }
      if (y >= 0) {
        return new Position(px, y);
      }
      return null;
    }
  }
}

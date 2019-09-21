package org.bk.ass.path;

import org.bk.ass.grid.Grid;

import java.util.Collections;

/**
 * Jump point search. Initialize with a {@link Grid} instance and call {@link #findPath(Position,
 * Position)}.
 */
public class Jps {

  private final Grid<Boolean> map;

  public Jps(Grid<Boolean> map) {
    this.map = map;
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

    private final Grid<Boolean> map;

    protected PathFinder(Position target, Grid<Boolean> map) {
      super(target, map);
      this.map = map;
    }

    @Override
    protected Position jumpHorizontal(int px, int py, int dx) {
      assert dx != 0;
      int x = px + dx;

      int a = (map.get(x, py - 1) ? 0 : 1) | (map.get(x, py + 1) ? 0 : 2);
      while (map.get(x, py)) {
        int b = (map.get(x + dx, py - 1) ? 1 : 0) | (map.get(x + dx, py + 1) ? 2 : 0);
        if (x == target.x && py == target.y || (a & b) != 0) {
          return new Position(x, py);
        }
        x += dx;
        a = ~b;
      }
      return null;
    }

    @Override
    protected Position jumpVertical(int px, int py, int dy) {
      assert dy != 0;
      int y = py + dy;

      int a = (map.get(px - 1, y) ? 0 : 1) | (map.get(px + 1, y) ? 0 : 2);
      while (map.get(px, y)) {
        int b = (map.get(px - 1, y + dy) ? 1 : 0) | (map.get(px + 1, y + dy) ? 2 : 0);
        if (px == target.x && y == target.y || (a & b) != 0) {
          return new Position(px, y);
        }
        y += dy;
        a = ~b;
      }
      return null;
    }
  }
}

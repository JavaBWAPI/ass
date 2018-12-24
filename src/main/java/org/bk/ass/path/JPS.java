package org.bk.ass.path;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Jump point search. Initialize with a {@link Map} instance and call {@link #findPath(Position,
 * Position)}.
 */
public class JPS {

  private final Map map;

  public JPS(Map map) {
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
    return new PathFinder(end).searchFrom(start);
  }

  private class PathFinder {
    private final PriorityQueue<Node> openSet = new PriorityQueue<>();
    private final Set<Position> closed = new HashSet<>();
    private final Position target;
    private int nextX;
    private int nextY;

    private PathFinder(Position target) {
      this.target = target;
    }

    public Result searchFrom(Position start) {
      openSet.add(new Node(start));
      while (!openSet.isEmpty()) {
        Node best = openSet.poll();
        if (best.position.equals(target)) {
          List<Position> path = new ArrayList<>();
          Node n = best;
          while (n != null) {
            path.add(n.position);
            n = n.parent;
          }
          Collections.reverse(path);
          return new Result(best.cost, path);
        }
        Position p = best.position;
        if (!closed.contains(p)) {
          closed.add(p);

          if (best.parent == null) {
            if (jumpHorizontal(p.x, p.y, -1)) {
              addToOpenSet(best);
            }
            if (jumpHorizontal(p.x, p.y, 1)) {
              addToOpenSet(best);
            }

            if (jumpVertical(p.x, p.y, -1)) {
              addToOpenSet(best);
            }
            if (jumpVertical(p.x, p.y, 1)) {
              addToOpenSet(best);
            }

            if (jumpDiag(p.x, p.y, 1, 1)) {
              addToOpenSet(best);
            }
            if (jumpDiag(p.x, p.y, -1, 1)) {
              addToOpenSet(best);
            }
            if (jumpDiag(p.x, p.y, -1, -1)) {
              addToOpenSet(best);
            }
            if (jumpDiag(p.x, p.y, 1, -1)) {
              addToOpenSet(best);
            }
          } else {
            int dx = p.x - best.parent.position.x;
            int dy = p.y - best.parent.position.y;

            if (dx < 0) {
              dx = -1;
            } else if (dx > 0) {
              dx = 1;
            }
            if (dy < 0) {
              dy = -1;
            } else if (dy > 0) {
              dy = 1;
            }

            if (dx != 0 && dy != 0) {
              if (jumpHorizontal(p.x, p.y, dx)) {
                addToOpenSet(best);
              }
              if (jumpVertical(p.x, p.y, dy)) {
                addToOpenSet(best);
              }
              if (jumpDiag(p.x, p.y, dx, dy)) {
                addToOpenSet(best);
              }
              if (!map.isWalkable(p.x - dx, p.y) && jumpDiag(p.x, p.y, -dx, dy)) {
                addToOpenSet(best);
              }
              if (!map.isWalkable(p.x, p.y - dy) && jumpDiag(p.x, p.y, dx, -dy)) {
                addToOpenSet(best);
              }
            } else if (dx != 0) {
              if (jumpHorizontal(p.x, p.y, dx)) {
                addToOpenSet(best);
              }
              if (!map.isWalkable(p.x, p.y - 1) && jumpDiag(p.x, p.y, dx, -1)) {
                addToOpenSet(best);
              }
              if (!map.isWalkable(p.x, p.y + 1) && jumpDiag(p.x, p.y, dx, 1)) {
                addToOpenSet(best);
              }
            } else {
              if (jumpVertical(p.x, p.y, dy)) {
                addToOpenSet(best);
              }
              if (!map.isWalkable(p.x - 1, p.y) && jumpDiag(p.x, p.y, -1, dy)) {
                addToOpenSet(best);
              }
              if (!map.isWalkable(p.x + 1, p.y) && jumpDiag(p.x, p.y, 1, dy)) {
                addToOpenSet(best);
              }
            }
          }
        }
      }

      return new Result(Float.POSITIVE_INFINITY, Collections.emptyList());
    }

    private void addToOpenSet(Node parent) {
      openSet.add(new Node(parent, new Position(nextX, nextY)));
    }

    private boolean jumpDiag(int px, int py, int dx, int dy) {
      assert dx != 0;
      assert dy != 0;
      int x = px + dx;
      int y = py + dy;
      int a = (map.isWalkable(x - dx, y) ? 0 : 1) | (map.isWalkable(x, y - dy) ? 0 : 2);
      while (map.isWalkable(x, y)) {
        int b = (map.isWalkable(x - dx, y + dy) ? 1 : 0) | (map.isWalkable(x + dx, y - dy) ? 2 : 0);
        if (x == target.x && y == target.y
            || (a & b) != 0
            || jumpHorizontal(x, y, dx)
            || jumpVertical(x, y, dy)) {
          nextX = x;
          nextY = y;
          return true;
        }
        x += dx;
        y += dy;
        a = ~b;
      }
      return false;
    }

    private boolean jumpHorizontal(int px, int py, int dx) {
      assert dx != 0;
      int x = px + dx;

      int a = (map.isWalkable(x, py - 1) ? 0 : 1) | (map.isWalkable(x, py + 1) ? 0 : 2);
      while (map.isWalkable(x, py)) {
        int b = (map.isWalkable(x + dx, py - 1) ? 1 : 0) | (map.isWalkable(x + dx, py + 1) ? 2 : 0);
        if (x == target.x && py == target.y || (a & b) != 0) {
          nextX = x;
          nextY = py;
          return true;
        }
        x += dx;
        a = ~b;
      }
      return false;
    }

    private boolean jumpVertical(int px, int py, int dy) {
      assert dy != 0;
      int y = py + dy;

      int a = (map.isWalkable(px - 1, y) ? 0 : 1) | (map.isWalkable(px + 1, y) ? 0 : 2);
      while (map.isWalkable(px, y)) {
        int b = (map.isWalkable(px - 1, y + dy) ? 1 : 0) | (map.isWalkable(px + 1, y + dy) ? 2 : 0);
        if (px == target.x && y == target.y || (a & b) != 0) {
          nextX = px;
          nextY = y;
          return true;
        }
        y += dy;
        a = ~b;
      }
      return false;
    }

    private class Node implements Comparable<Node> {

      final Node parent;
      final Position position;
      final float cost;
      final float h;

      Node(Position start) {
        parent = null;
        position = start;
        cost = 0;
        h =
            (float)
                Math.sqrt(
                    (position.x - target.x) * (position.x - target.x)
                        + (position.y - target.y) * (position.y - target.y));
      }

      Node(Node parent, Position position) {
        this.parent = parent;
        this.position = position;
        cost =
            parent.cost
                + (float)
                Math.sqrt(
                    (position.x - parent.position.x) * (position.x - parent.position.x)
                        + (position.y - parent.position.y) * (position.y - parent.position.y));
        h =
            (float)
                Math.sqrt(
                    (position.x - target.x) * (position.x - target.x)
                        + (position.y - target.y) * (position.y - target.y));
      }

      @Override
      public int compareTo(Node o) {
        return Float.compare(cost + h, o.cost + o.h);
      }

      @Override
      public String toString() {
        return position + " : " + (cost + h);
      }
    }
  }

  public static class Position {

    public final int x;
    public final int y;

    Position(int x, int y) {
      this.x = x;
      this.y = y;
    }

    @Override
    public String toString() {
      return "(" + x + ", " + y + ")";
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (o == null || getClass() != o.getClass()) {
        return false;
      }
      Position position = (Position) o;
      return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
      return x * 100279 ^ y;
    }
  }

  /**
   * The query interface of the path finding algorithm.
   */
  public interface Map {

    /**
     * Returns true, if the given position is walkable <em>and</em> within the map.
     */
    boolean isWalkable(int x, int y);

    static Map fromBooleanArray(boolean[][] map) {
      return (x, y) -> x >= 0 && x < map[0].length && y >= 0 && y < map.length && map[y][x];
    }
  }

  /** Pathfinding result. */
  public static class Result {

    /** The length of the path or {@link Float#POSITIVE_INFINITY} if none was found. */
    public final float length;
    /**
     * The path without intermediary steps. Each pair of positions describes a line to be followed.
     * The minimum angle between those segments is 45° and is a multiple of 45°.
     */
    public final List<Position> path;

    public Result(float length, List<Position> path) {
      this.length = length;
      this.path = path;
    }
  }
}

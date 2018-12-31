package org.bk.ass.path;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

abstract class AbstractPathFinder {

  private final PriorityQueue<Node> openSet = new PriorityQueue<>();
  private final Set<Position> closed = new HashSet<>();
  protected final Position target;
  private final Map map;

  protected AbstractPathFinder(Position target, Map map) {
    this.target = target;
    this.map = map;
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
        return new Result(best.cost / 10f, path);
      }
      Position p = best.position;
      if (!closed.contains(p)) {
        closed.add(p);

        if (best.parent == null) {
          addToOpenSet(best, jumpHorizontal(p.x, p.y, -1));
          addToOpenSet(best, jumpHorizontal(p.x, p.y, 1));

          addToOpenSet(best, jumpVertical(p.x, p.y, -1));
          addToOpenSet(best, jumpVertical(p.x, p.y, 1));

          addToOpenSet(best, jumpDiag(p.x, p.y, 1, 1));
          addToOpenSet(best, jumpDiag(p.x, p.y, -1, 1));
          addToOpenSet(best, jumpDiag(p.x, p.y, -1, -1));
          addToOpenSet(best, jumpDiag(p.x, p.y, 1, -1));
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
            addToOpenSet(best, jumpHorizontal(p.x, p.y, dx));
            addToOpenSet(best, jumpVertical(p.x, p.y, dy));
            addToOpenSet(best, jumpDiag(p.x, p.y, dx, dy));
            if (!map.isWalkable(p.x - dx, p.y)) {
              addToOpenSet(best, jumpDiag(p.x, p.y, -dx, dy));
            }
            if (!map.isWalkable(p.x, p.y - dy)) {
              addToOpenSet(best, jumpDiag(p.x, p.y, dx, -dy));
            }
          } else if (dx != 0) {
            addToOpenSet(best, jumpHorizontal(p.x, p.y, dx));
            if (!map.isWalkable(p.x, p.y - 1)) {
              addToOpenSet(best, jumpDiag(p.x, p.y, dx, -1));
            }
            if (!map.isWalkable(p.x, p.y + 1)) {
              addToOpenSet(best, jumpDiag(p.x, p.y, dx, 1));
            }
          } else {
            addToOpenSet(best, jumpVertical(p.x, p.y, dy));
            if (!map.isWalkable(p.x - 1, p.y)) {
              addToOpenSet(best, jumpDiag(p.x, p.y, -1, dy));
            }
            if (!map.isWalkable(p.x + 1, p.y)) {
              addToOpenSet(best, jumpDiag(p.x, p.y, 1, dy));
            }
          }
        }
      }
    }

    return new Result(Float.POSITIVE_INFINITY, Collections.emptyList());
  }

  protected abstract Position jumpVertical(int x, int y, int dy);

  protected abstract Position jumpHorizontal(int x, int y, int dx);

  private void addToOpenSet(Node parent, Position pos) {
    if (pos != null) {
      openSet.add(new Node(parent, pos));
    }
  }

  private Position jumpDiag(int px, int py, int dx, int dy) {
    assert dx != 0;
    assert dy != 0;
    int x = px + dx;
    int y = py + dy;
    int a = (map.isWalkable(x - dx, y) ? 0 : 1) | (map.isWalkable(x, y - dy) ? 0 : 2);
    while (map.isWalkable(x, y)) {
      int b = (map.isWalkable(x - dx, y + dy) ? 1 : 0) | (map.isWalkable(x + dx, y - dy) ? 2 : 0);
      if (x == target.x && y == target.y
          || (a & b) != 0
          || jumpHorizontal(x, y, dx) != null
          || jumpVertical(x, y, dy) != null) {
        return new Position(x, y);
      }
      x += dx;
      y += dy;
      a = ~b;
    }
    return null;
  }

  private class Node implements Comparable<Node> {

    final Node parent;
    final Position position;
    final int cost;
    final int h;

    Node(Position start) {
      parent = null;
      position = start;
      cost = 0;
      h = estCost(position.x - target.x, position.y - target.y);
    }

    private int estCost(int dx, int dy) {
      int distX = abs(dx);
      int distY = abs(dy);
      return 10 * max(distX, distY) + 4 * min(distX, distY);
    }

    Node(Node parent, Position position) {
      this.parent = parent;
      this.position = position;
      cost = parent.cost + estCost(position.x - parent.position.x, position.y - parent.position.y);
      h = estCost(position.x - target.x, position.y - target.y);
    }

    @Override
    public int compareTo(Node o) {
      return Integer.compare(cost + h, o.cost + o.h);
    }

    @Override
    public String toString() {
      return position + " : " + (cost + h);
    }
  }
}

package org.bk.ass.path;

import java.util.*;

import static java.lang.Math.*;

abstract class AbstractPathFinder {

  private final PriorityQueue<Node> openQueue = new PriorityQueue<>(100);
  private final Set<Position> closed = new HashSet<>(400);
  final Position target;
  private final Map map;

  AbstractPathFinder(Position target, Map map) {
    this.target = target;
    this.map = map;
  }

  Result searchFrom(Position start) {
    openQueue.add(new Node(start));
    Node best;
    while ((best = openQueue.poll()) != null) {
      if (best.position.equals(target)) {
        List<Position> path = new ArrayList<>();
        Node n = best;
        while (n != null) {
          path.add(n.position);
          n = n.parent;
        }
        Collections.reverse(path);
        return new Result(best.g / 10f, path);
      }
      Position p = best.position;
      boolean proceed = closed.add(p);
      if (proceed) {
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
          int dx = Integer.signum(p.x - best.parent.position.x);
          int dy = Integer.signum(p.y - best.parent.position.y);

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
      openQueue.add(new Node(parent, pos));
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
    final int g;
    final int f;

    Node(Position start) {
      parent = null;
      position = start;
      g = 0;
      f = estCost(position.x - target.x, position.y - target.y);
    }

    private int estCost(int dx, int dy) {
      int distX = abs(dx);
      int distY = abs(dy);
      return 10 * max(distX, distY) + 4 * min(distX, distY);
    }

    Node(Node parent, Position position) {
      this.parent = parent;
      this.position = position;
      g = parent.g + estCost(position.x - parent.position.x, position.y - parent.position.y);
      f = g + estCost(position.x - target.x, position.y - target.y);
    }

    @Override
    public int compareTo(Node o) {
      return Integer.compare(f, o.f);
    }

    @Override
    public String toString() {
      return position + " : " + f;
    }
  }
}

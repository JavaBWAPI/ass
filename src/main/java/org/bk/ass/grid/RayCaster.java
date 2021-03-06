package org.bk.ass.grid;

import static java.lang.Math.abs;

import org.bk.ass.grid.SearchPredicate.Result;

/**
 * Allows tracing rays through a grid.
 *
 * @param <T>
 */
public class RayCaster<T> {

  private final SearchPredicate<Hit<T>> acceptLast =
      x -> {
        if (x == null || Boolean.FALSE.equals(x.value)) {
          return Result.STOP;
        }
        return Result.ACCEPT_CONTINUE;
      };

  public final SearchPredicate<Hit<T>> findFirst = x -> {
    if (x == null || Boolean.FALSE.equals(x.value)) {
      return Result.CONTINUE;
    }
    return Result.ACCEPT;
  };
  private final Grid<T> grid;

  public RayCaster(Grid<T> grid) {
    this.grid = grid;
  }

  /** Trace a line through the grid and return the first hit, or null if none. */
  public Hit<T> trace(int ax, int ay, int bx, int by) {
    return trace(ax, ay, bx, by, acceptLast);
  }

  /**
   * Trace a line through the grid and return the first hit which is accepted by the predicate. If
   * none is accepted or found, returns null.<br>
   * The predicate can be used to capture multiple hits.
   */
  public Hit<T> trace(int ax, int ay, int bx, int by, SearchPredicate<Hit<T>> stopPredicate) {
    int deltaX = abs(bx - ax);
    int deltaY = -abs(by - ay);
    int x = ax;
    int y = ay;
    int err = deltaX + deltaY;
    int sx = ax < bx ? 1 : -1;
    int sy = ay < by ? 1 : -1;
    Hit<T> best = null;

    while (true) {
      T value = grid.get(x, y);
      Hit<T> hit = new Hit<>(x, y, value);
      Result result = stopPredicate.accept(hit);
      if (result == Result.STOP) {
        return best;
      }
      if (result == Result.ACCEPT) {
        return hit;
      }
      if (result == Result.ACCEPT_CONTINUE) {
        best = hit;
      }
      if (x == bx && y == by) {
        return best;
      }
      int e2 = 2 * err;
      if (e2 >= deltaY) {
        err += deltaY;
        x += sx;
      }
      if (e2 <= deltaX) {
        err += deltaX;
        y += sy;
      }
    }
  }

  public static class Hit<T> {

    public final int x;
    public final int y;
    public final T value;

    public Hit(int x, int y, T value) {
      this.x = x;
      this.y = y;
      this.value = value;
    }
  }
}

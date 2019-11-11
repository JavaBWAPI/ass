package org.bk.ass.grid;

import static java.lang.Math.abs;

import java.util.function.Predicate;

/**
 * Allows tracing rays through a grid.
 *
 * @param <T>
 */
public class RayCaster<T> {

  private static final Predicate ALWAYS_STOP = x -> true;
  private final Grid<T> grid;

  public RayCaster(Grid<T> grid) {
    this.grid = grid;
  }

  /** Trace a line through the grid and return the first hit, or null if none. */
  public Hit<T> trace(int ax, int ay, int bx, int by) {
    return trace(ax, ay, bx, by, ALWAYS_STOP);
  }

  /**
   * Trace a line through the grid and return the first hit which is accepted by the predicate. If
   * none is accepted or found, returns null.<br>
   * The predicate can be used to capture multiple hits.
   */
  public Hit<T> trace(int ax, int ay, int bx, int by, Predicate<Hit<T>> stopPredicate) {
    int deltaX = abs(bx - ax);
    int deltaY = -abs(by - ay);
    int x = ax;
    int y = ay;
    int err = 2 * (deltaX + deltaY);
    int sx = ax < bx ? 1 : -1;
    int sy = ay < by ? 1 : -1;

    while (true) {
      T value = grid.get(x, y);
      if (value != null && !value.equals(Boolean.FALSE)) {
        Hit<T> hit = new Hit<>(x, y, value);
        if (stopPredicate.test(hit)) {
          return hit;
        }
      }
      if (x == bx && y == by) {
        return null;
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

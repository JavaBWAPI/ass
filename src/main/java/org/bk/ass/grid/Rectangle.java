package org.bk.ass.grid;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Iterator;
import java.util.stream.IntStream;
import org.bk.ass.path.Position;

public final class Rectangle implements Iterable<Position> {

  private final int ax;
  private final int ay;
  private final int bx;
  private final int by;

  public Rectangle(Position a, Position b) {
    this.ax = min(a.x, b.x);
    this.ay = min(a.y, b.y);
    this.bx = max(a.x, b.x);
    this.by = max(a.y, b.y);
  }

  public Position getTopLeft() {
    return new Position(ax, ay);
  }

  public Position getBottomRight() {
    return new Position(bx, by);
  }


  public boolean intersects(Rectangle other) {
    return bx > other.ax && ax < other.bx &&
        by > other.ay && ay < other.by;
  }

  public boolean contains(Rectangle other) {
    return ax <= other.ax && ay <= other.ay &&
        bx >= other.bx && by >= other.by;
  }

  public boolean contains(Position position) {
    return ax <= position.x && ay <= position.y &&
        bx > position.x && by > position.y;
  }

  @Override
  public Iterator<Position> iterator() {
    return IntStream.range(ay, by)
        .mapToObj(y ->
            IntStream.range(ax, bx).mapToObj(x -> new Position(x, y))
        ).flatMap(it -> it)
        .iterator();
  }
}

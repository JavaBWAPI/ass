package org.bk.ass.grid;

import java.util.Iterator;
import java.util.stream.IntStream;
import org.bk.ass.path.Position;

public final class Rectangle implements Iterable<Position> {

  private final Position start;
  private final Position end;

  public Rectangle(Position start, Position end) {
    this.start = start;
    this.end = end;
  }

  public boolean intersects(Rectangle other) {
    return end.x > other.start.x && start.x < other.end.x &&
        end.y > other.start.y && start.y < other.end.y;
  }

  public boolean contains(Rectangle other) {
    return start.x <= other.start.x && start.y <= other.start.y &&
        end.x >= other.end.x && end.y >= other.end.y;
  }

  public boolean contains(Position position) {
    return start.x <= position.x && start.y <= position.y &&
        end.x > position.x && end.y > position.y;
  }

  @Override
  public Iterator<Position> iterator() {
    return IntStream.range(start.y, end.y)
        .mapToObj(y ->
            IntStream.range(start.x, end.x).mapToObj(x -> new Position(x, y))
        ).flatMap(it -> it)
        .iterator();
  }
}

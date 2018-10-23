package org.bk.ass.query;

import java.util.Objects;

/**
 * Wraps position and a unique identifier to discriminate entities from each other.
 */
public class PositionAndId implements Comparable<PositionAndId> {

  private final int id;
  final int x;
  final int y;

  public PositionAndId(int id, int x, int y) {
    this.x = x;
    this.y = y;
    this.id = id;
  }

  @Override
  public String toString() {
    return "id: " + id + " (" + x + ", " + y + ")";
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PositionAndId that = (PositionAndId) o;
    return id == that.id && x == that.x && y == that.y;
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, x, y);
  }

  @Override
  public int compareTo(PositionAndId o) {
    if (x < o.x) {
      return -1;
    }
    if (x > o.x) {
      return 1;
    }
    if (y < o.y) {
      return -1;
    }
    if (y > o.y) {
      return 1;
    }
    return Integer.compare(id, o.id);
  }
}

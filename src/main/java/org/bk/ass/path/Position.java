package org.bk.ass.path;

import bwapi.WalkPosition;

public final class Position {

  public final int x;
  public final int y;

  public Position(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public static Position of(WalkPosition p) {
    return new Position(p.x, p.y);
  }

  public static Position of(org.openbw.bwapi4j.WalkPosition p) {
    return new Position(p.getX(), p.getY());
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

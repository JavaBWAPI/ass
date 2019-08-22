package org.bk.ass.query;

/** Provide the distance between to positions. */
@FunctionalInterface
public interface DistanceProvider {

  int distance(int ax, int ay, int bx, int by);
}

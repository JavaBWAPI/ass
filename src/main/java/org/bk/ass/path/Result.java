package org.bk.ass.path;

import java.util.List;

/**
 * Pathfinding result.
 */
public class Result {

  /**
   * The length of the path or {@link Float#POSITIVE_INFINITY} if none was found.
   */
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

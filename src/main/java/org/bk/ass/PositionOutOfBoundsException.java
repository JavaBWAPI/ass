package org.bk.ass;

/** Thrown if a position is not within the required bounds. */
public class PositionOutOfBoundsException extends RuntimeException {

  public PositionOutOfBoundsException(String message) {
    super(message);
  }
}

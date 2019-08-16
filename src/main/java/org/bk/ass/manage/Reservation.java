package org.bk.ass.manage;

/**
 * Should be implemented by clients to be able to use {@link Lock}.
 *
 * @param <T> the type of resource
 */
public interface Reservation<T> {
  /** Returns true if the given item could be reserved, false otherwise. */
  boolean tryReserve(T item);

  default boolean canBeReservedLater(T item, int futureFrames) {
    return false;
  }

  /** Releases the given item. */
  void release(T item);
}

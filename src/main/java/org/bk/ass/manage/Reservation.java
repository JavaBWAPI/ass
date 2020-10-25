package org.bk.ass.manage;

/**
 * Should be implemented by clients to be able to use {@link Lock}.
 *
 * @param <T> the type of resource
 */
public interface Reservation<T> {

  /**
   * Returns true, if the given item is available immediately. Returns false, if the item was
   * reserved but is not yet available. (Ie. resources might be "blocked" for later use, but not yet
   * spendable.)
   */
  boolean reserve(Object source, T item);

  /**
   * Returns true if the given item will be available in futureFrames frames. The given item will
   * already be reserved when this is called! If used for {@link GMS}, you should re-add the given
   * {@link GMS} before checking for sufficient resources.
   */
  default boolean itemReservableInFuture(Object source, T item, int futureFrames) {
    return false;
  }

  /**
   * Releases the given item.
   */
  void release(Object source, T item);
}

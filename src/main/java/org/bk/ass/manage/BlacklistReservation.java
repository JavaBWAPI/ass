package org.bk.ass.manage;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple {@link Reservation} that allows only one item of the given type to be reserved at a time.
 *
 * @param <T>
 */
public class BlacklistReservation<T> implements Reservation<T> {

  private final Set<T> reservedItems = new HashSet<>();

  public void reset() {
    this.reservedItems.clear();
  }

  @Override
  public boolean reserve(Object source, T item) {
    return reservedItems.add(item);
  }

  @Override
  public void release(Object source, T item) {
    reservedItems.remove(item);
  }

  public boolean isAvailable(T item) {
    return !reservedItems.contains(item);
  }
}

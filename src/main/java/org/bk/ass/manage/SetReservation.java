package org.bk.ass.manage;

import java.util.HashSet;
import java.util.Set;

/**
 * Simple {@link Reservation} that allows only one item of the given type to be reserved at a time.
 *
 * @param <T>
 */
public class SetReservation<T> implements Reservation<T> {

  private final Set<T> reservedItems = new HashSet<>();

  public void setReservedItems(Set<T> reservedItems) {
    this.reservedItems.clear();
    this.reservedItems.addAll(reservedItems);
  }

  @Override
  public boolean reserve(Lock<T> lock, T item) {
    return reservedItems.add(item);
  }

  @Override
  public void release(Lock<T> lock, T item) {
    reservedItems.remove(item);
  }
}

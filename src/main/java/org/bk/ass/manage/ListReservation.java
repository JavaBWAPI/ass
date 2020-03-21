package org.bk.ass.manage;

import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper for {@link Reservation} to allow for "atomic" list of item reservations.
 */
public class ListReservation<T> implements Reservation<List<T>> {

  private final Reservation<T> delegate;

  public ListReservation(Reservation<T> delegate) {
    this.delegate = delegate;
  }

  @Override
  public boolean reserve(Object source, List<T> itemList) {
    ArrayList<T> reservedItems = new ArrayList<>();
    for (T item : itemList) {
      boolean reserved = delegate.reserve(source, item);
      if (!reserved) {
        reservedItems.forEach(it -> delegate.release(source, it));
        return false;
      }
      reservedItems.add(item);
    }
    return true;
  }

  @Override
  public void release(Object source, List<T> itemList) {
    itemList.forEach(it -> delegate.release(source, it));
  }
}


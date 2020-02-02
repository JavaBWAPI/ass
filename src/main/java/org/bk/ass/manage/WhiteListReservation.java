package org.bk.ass.manage;

import java.util.Collection;

public class WhiteListReservation<T, S extends Collection<T>> implements Reservation<T> {

  public S availableItems;

  public boolean isAvailable(T item) {
    return availableItems.contains(item);
  }

  @Override
  public boolean reserve(Object source, T item) {
    return availableItems.remove(item);
  }

  @Override
  public void release(Object source, T item) {
    availableItems.add(item);
  }
}

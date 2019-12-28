package org.bk.ass.manage;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class ListLock<T> extends Lock<List<T>> {

  public ListLock(Reservation<List<T>> reservation, Supplier<List<T>> selector) {
    super(reservation, selector);
  }

  public boolean releaseItem(T item) {
    return releaseItem(Collections.singletonList(item));
  }

  public boolean releaseItem(List<T> partial) {
    int items = item.size();
    item.removeAll(partial);
    if (item.size() + partial.size() != items)
      throw new IllegalArgumentException(
          "Some of the items released are not part of the original item list!");
    reservation.release(partial);
    return checkLockSatisfied();
  }
}

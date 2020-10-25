package org.bk.ass.manage;

import java.util.Collections;
import java.util.List;

/**
 * @param <T>
 */
public class ListLock<T> extends Lock<List<T>> {

  public ListLock(Reservation<List<T>> reservation) {
    super(reservation);
  }

  public boolean releaseItem(T item) {
    return releasePartially(Collections.singletonList(item));
  }

  @Override
  public List<T> getItem() {
    return Collections.unmodifiableList(super.getItem());
  }

  /**
   * Releases only some of the items in the list.
   */
  public boolean releasePartially(List<T> partial) {
    int items = item.size();
    item.removeAll(partial);
    if (item.size() + partial.size() != items) {
      throw new IllegalArgumentException(
          "Some of the items released are not part of the original item list!");
    }
    reservation.release(this, partial);
    return tryLock();
  }
}

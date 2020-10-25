package org.bk.ass.collection;

import java.util.Comparator;
import java.util.List;
import java.util.ListIterator;

public final class Collections {

  private Collections() {
// Utility method
  }

  public static <T> T removeMax(List<T> list, Comparator<T> comparator) {
    int maxIndex = -1;
    T max = null;
    for (ListIterator<T> it = list.listIterator(); it.hasNext(); ) {
      T current = it.next();
      if (max == null || comparator.compare(current, max) > 0) {
        max = current;
        maxIndex = it.previousIndex();
      }
    }
    return list.remove(maxIndex);
  }
}

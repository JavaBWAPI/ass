package org.bk.ass.manage;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Holds a lock on a resource. Ie. a unit or {@link GMS}.
 *
 * @param <T> the type of resource
 */
public class Lock<T> {
  private boolean satisfied;
  private boolean satisfiedLater;
  private boolean changed;
  private T item;
  private Predicate<T> criteria = unused -> true;
  private final Reservation<T> reservation;
  private final Supplier<T> selector;
  private int futureFrame;

  public Lock(Reservation<T> reservation, Supplier<T> selector) {
    this.reservation = reservation;
    this.selector = selector;
  }

  public void setFutureFrame(int futureFrame) {
    this.futureFrame = futureFrame;
  }

  public T getItem() {
    return item;
  }

  public boolean isSatisfied() {
    return satisfied;
  }

  public boolean isSatisfiedLater() {
    return satisfiedLater;
  }

  public boolean isChanged() {
    return changed;
  }

  public void reacquire() {
    reset();
    acquire();
  }

  public void release() {
    if (item != null) reservation.release(item);
    item = null;
    changed = true;
  }

  public void reset() {
    item = null;
    changed = false;
    satisfied = false;
  }

  public void acquire() {
    changed = false;
    if (item != null && criteria.test(item) && reservation.tryReserve(item)) {
      satisfied = true;
      satisfiedLater = true;
      return;
    }
    item = selector.get();
    satisfied = item != null && criteria.test(item);
    if (satisfied) {
      changed = true;
      satisfiedLater = true;
      boolean reserved = reservation.tryReserve(item);
      if (!reserved) throw new IllegalStateException("Could not reserve item that was selected!");
    } else satisfiedLater = reservation.canBeReservedLater(item, futureFrame);
  }
}

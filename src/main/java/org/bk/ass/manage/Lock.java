package org.bk.ass.manage;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Holds a lock on a resource. Ie. a unit or {@link GMS}.
 *
 * @param <T> the type of resource
 */
public class Lock<T> {

  static final int DEFAULT_HYSTERESIS = 48;
  private boolean satisfied;
  private boolean satisfiedLater;
  T item;
  private Predicate<T> criteria = unused -> true;
  final Reservation<T> reservation;
  private final Supplier<T> selector;
  private int futureFrames;
  private int hysteresisFrames = DEFAULT_HYSTERESIS;

  public Lock(Reservation<T> reservation, Supplier<T> selector) {
    this.reservation = reservation;
    this.selector = selector;
  }

  public void setFutureFrames(int futureFrames) {
    this.futureFrames = futureFrames;
  }

  public void setHysteresisFrames(int hysteresisFrames) {
    this.hysteresisFrames = hysteresisFrames;
  }

  public void setCriteria(Predicate<T> criteria) {
    this.criteria = criteria;
  }

  public T getItem() {
    if (!satisfied) throw new IllegalStateException("Item is not locked!");
    return item;
  }

  public boolean isSatisfied() {
    return satisfied;
  }

  public boolean isSatisfiedLater() {
    return satisfiedLater;
  }

  public void reacquire() {
    release();
    acquire();
  }

  public void release() {
    if (item != null) reservation.release(item);
    reset();
  }

  public void reset() {
    item = null;
    satisfied = false;
    satisfiedLater = false;
  }

  public boolean acquire() {
    if (item == null || !criteria.test(item)) item = null;
    if (item == null) item = selector.get();
    return checkLockSatisfied();
  }

  boolean checkLockSatisfied() {
    satisfied = item != null && criteria.test(item) && reservation.reserve(item);
    if (satisfied) {
      satisfiedLater = true;
      return true;
    } else {
      satisfiedLater =
          item != null
              && reservation.itemAvailableInFuture(
                  item, futureFrames + (satisfiedLater ? hysteresisFrames : 0));
      return false;
    }
  }
}

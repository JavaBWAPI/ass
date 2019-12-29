package org.bk.ass.manage;

import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Holds a lock on a resource. Ie. a unit or {@link GMS}. For a lock supporting multiple items, use
 * {@link ListLock}.
 *
 * <p>How to use the lock:
 *
 * <ul>
 *   <li>Per-Frame mode: Your {@link Reservation} should reset every frame. Call {@link #acquire()}
 *       to get the current or new item. Only call {@link #release()} if you might want to lock some
 *       item somewhere else in the same frame. To ensure a new item is retrieved every time, use
 *       {@link #reacquire()}.
 *   <li>Continuous mode: Your {@link Reservation} is "persistent" and will not forget reserved
 *       items. If a reserved item is "lost", be sure to call {@link #reset()}. Call {@link
 *       #release()} to allow another lock to {@link #acquire()} an item. Never call {@link
 *       #reacquire()}, it will not release any previous item.
 * </ul>
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

  /**
   * When trying to figure out if the lock can be satisfied in the future, uses the given amount of
   * frames. Be sure to update the value or reset it if no longer used!
   */
  public void setFutureFrames(int futureFrames) {
    this.futureFrames = futureFrames;
  }

  /**
   * Once a lock is <em>satisfiedLater</em>, uses the given amount of frames in addition to the
   * future frames for the next check if the lock can be satisfied in the future. The main purpose
   * it to prevent {@link #isSatisfiedLater()} from jumping from true to false due to small
   * fluctuations. The default value is 48 frames.
   */
  public void setHysteresisFrames(int hysteresisFrames) {
    this.hysteresisFrames = hysteresisFrames;
  }

  /**
   * Add a criteria any locked item must meet. This is true for already selected items and for newly
   * selected items. If the selected item does not satisfy the criteria, it will be dropped.
   */
  public void setCriteria(Predicate<T> criteria) {
    this.criteria = criteria;
  }

  /**
   * Returns the currently locked item.
   *
   * @throws IllegalStateException if the current item is not locked.
   */
  public T getItem() {
    if (!satisfied) throw new IllegalStateException("Item is not locked!");
    return item;
  }

  /** Returns true if there is a locked item available now. */
  public boolean isSatisfied() {
    return satisfied;
  }

  /**
   * Returns true if an item was reserved but is not yet available. If {@link #futureFrames} is 0,
   * this should match {@link #isSatisfied()}.
   */
  public boolean isSatisfiedLater() {
    return satisfiedLater;
  }

  /**
   * Drops any current item and tries to acquire a new one. The current item will not be released!
   */
  public void reacquire() {
    reset();
    acquire();
  }

  /** Releases the currently reserved item. */
  public void release() {
    if (item != null) reservation.release(this, item);

    reset();
  }

  /** Resets this lock. Any previously reserved item will not be released when using this call. */
  public void reset() {
    item = null;
    satisfied = false;
    satisfiedLater = false;
  }

  /**
   * Tries to reserve and lock an item. If the last item reserved was not released, tries to reserve
   * and lock it again. Returns true if an item was reserved and is available immediately.
   */
  public boolean acquire() {
    if (item == null || !criteria.test(item)) item = null;
    if (item == null) item = selector.get();
    return checkLockSatisfied();
  }

  boolean checkLockSatisfied() {
    satisfied = item != null && criteria.test(item) && reservation.reserve(this, item);
    if (satisfied) {
      satisfiedLater = true;
      return true;
    } else {
      satisfiedLater =
          item != null
              && reservation.itemAvailableInFuture(
                  this, item, futureFrames + (satisfiedLater ? hysteresisFrames : 0));
      return false;
    }
  }
}

package org.bk.ass.manage;

import java.util.function.Predicate;

/**
 * Holds a lock on a resource. Ie. a unit or {@link GMS}. For a lock supporting multiple items, use
 * {@link ListLock}.
 *
 * <p>How to use the lock:
 *
 * <ul>
 *   <li>Per-Frame mode: Your {@link Reservation} should reset every frame. Call {@link #tryLock()} ()}
 *       to get the current or new item. Only call {@link #release()} if you might want to lock some
 *       item somewhere else in the same frame.
 *   <li>Continuous mode: Your {@link Reservation} is "persistent" and will not forget reserved
 *       items. If a reserved item is "lost", be sure to call {@link #reset()}. Call {@link
 *       #release()} to allow another lock to {@link #tryLock()} an item.
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
  private int futureFrames;
  private int hysteresisFrames = DEFAULT_HYSTERESIS;

  public Lock(Reservation<T> reservation) {
    this.reservation = reservation;
  }

  /**
   * When trying to figure out if the lock can be satisfied in the future, uses the given amount of
   * frames. Be sure to update the value or reset it if no longer used!
   */
  public void setFutureFrames(int futureFrames) {
    this.futureFrames = futureFrames;
    satisfiedLater = false;
  }

  /**
   * Once a lock is <em>satisfiedLater</em>, uses the given amount of frames in addition to the
   * future frames for the next check if the lock can be satisfied in the future. The main purpose
   * it to prevent {@link #isSatisfiedLater()} from jumping from true to false due to small
   * fluctuations. The default value is 48 frames.
   */
  public void setHysteresisFrames(int hysteresisFrames) {
    this.hysteresisFrames = hysteresisFrames;
    satisfiedLater = false;
  }

  /**
   * Add a criteria any locked item must meet. This is true for already selected items and for newly
   * selected items. If the selected item does not satisfy the criteria, it will be dropped.
   */
  public void setCriteria(Predicate<T> criteria) {
    this.criteria = criteria;
    satisfied = false;
    satisfiedLater = false;
  }

  /**
   * Returns the currently reserved item. To verify that it is locked, call {@link #isSatisfied()}
   * before!
   */
  public T getItem() {
    return item;
  }

  /**
   * Set the item to be locked. Setting an item does not mean it is locked. To try to lock it, call
   * {@link #tryLock()}.
   */
  public void setItem(T item) {
    this.item = item;
    satisfied = false;
    satisfiedLater = false;
  }

  /**
   * Returns true if there is a locked item available now.
   */
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
   * Releases the currently reserved item.
   */
  public void release() {
    if (item != null) {
      reservation.release(this, item);
    }

    reset();
  }

  /**
   * Resets this lock. Any previously reserved item will not be released when using this call.
   */
  public void reset() {
    satisfied = false;
    satisfiedLater = false;
    futureFrames = 0;
  }

  /**
   * Tries to reserve and lock an item. If the last item reserved was not released, tries to reserve
   * and lock it again. Returns true if an item was reserved and is available immediately (same as
   * if {@link #isSatisfied() was called}.
   */
  public boolean tryLock() {
    satisfied = item != null && criteria.test(item) && reservation.reserve(this, item);
    if (satisfied) {
      satisfiedLater = true;
      return true;
    } else {
      satisfiedLater =
          item != null
              && reservation.itemReservableInFuture(
              this, item, futureFrames + (satisfiedLater ? hysteresisFrames : 0));
      return false;
    }
  }
}

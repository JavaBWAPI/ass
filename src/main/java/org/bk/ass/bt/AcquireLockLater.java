package org.bk.ass.bt;

import java.util.Objects;
import java.util.function.Supplier;
import org.bk.ass.manage.Lock;

/**
 * Succeeds if the lock will be satisfied later (see {@link Lock#isSatisfiedLater()}, fails
 * otherwise.
 *
 * @param <L>
 */
public class AcquireLockLater<T, L extends Lock<T>> extends TreeNode {

  private final L lock;
  private final Supplier<T> selector;

  public AcquireLockLater(L lock, Supplier<T> selector) {
    Objects.requireNonNull(lock, "lock must be set");
    Objects.requireNonNull(selector, "selector must be set");
    this.selector = selector;
    this.lock = lock;
  }

  @Override
  public void exec() {
    if (!lock.isSatisfiedLater()) {
      lock.setItem(selector.get());
    }
    if (lock.tryLock() || lock.isSatisfiedLater()) {
      success();
    } else {
      failed();
    }
  }

  @Override
  public void reset() {
    super.reset();
    lock.reset();
  }
}

package org.bk.ass.bt;

import java.util.Objects;
import java.util.function.Supplier;
import org.bk.ass.manage.Lock;

/**
 * Leaf node that will succeed if the supplied lock can be acquired. Fails otherwise.
 *
 * @param <T> the lock kind
 */
public class AcquireLock<T> extends TreeNode {

  private final Lock<T> lock;
  private final Supplier<T> selector;

  public AcquireLock(Lock<T> lock, Supplier<T> selector) {
    this.lock = Objects.requireNonNull(lock);
    this.selector = Objects.requireNonNull(selector);
  }

  @Override
  public void exec() {
    if (!lock.isSatisfied()) {
      lock.setItem(selector.get());
    }
    if (lock.tryLock()) {
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

package org.bk.ass.bt;

import org.bk.ass.manage.Lock;

/**
 * Leaf node that will succeed if the supplied lock can be acquired. Fails otherwise.
 *
 * @param <T> the lock kind
 */
public class AcquireLock<T> extends TreeNode {

  private final Lock<T> lock;

  public AcquireLock(Lock<T> lock) {
    this.lock = lock;
  }

  @Override
  public void exec() {
    if (lock.acquire()) {
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

package org.bk.ass.bt;

import org.bk.ass.manage.Lock;

/**
 * Leaf node that will reset the supplied lock. Will always succeed.
 *
 * @param <T> the lock kind
 */
public class ResetLock<T> extends TreeNode {

  private final Lock<T> lock;

  public ResetLock(Lock<T> lock) {
    this.lock = lock;
  }

  @Override
  public void exec() {
    lock.reset();
    success();
  }

  @Override
  public void reset() {
    super.reset();
    lock.reset();
  }
}

package org.bk.ass.bt;

import org.bk.ass.manage.Lock;

/**
 * Leaf node that will release the supplied lock. Will always succeed.
 *
 * @param <T> the lock kind
 */
public class ReleaseLock<T> extends TreeNode {

  private final Lock<T> lock;

  public ReleaseLock(Lock<T> lock) {
    this.lock = lock;
  }

  @Override
  public void exec() {
    lock.release();
    success();
  }

  @Override
  public void reset() {
    super.reset();
    lock.reset();
  }
}

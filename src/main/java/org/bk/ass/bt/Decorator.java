package org.bk.ass.bt;

import java.util.Objects;

public abstract class Decorator extends TreeNode {
  private final TreeNode delegate;

  public Decorator(TreeNode delegate) {
    Objects.requireNonNull(delegate, "delegate must not be set");
    this.delegate = delegate;
  }

  @Override
  public void init() {
    delegate.init();
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public void exec() {
    delegate.exec();
    updateStatusFromDelegate(delegate.getStatus());
  }

  protected void updateStatusFromDelegate(NodeStatus status) {
    this.status = status;
  }

  @Override
  public double getUtility() {
    return delegate.getUtility();
  }

  @Override
  public void reset() {
    super.reset();
    delegate.reset();
  }
}

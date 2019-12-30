package org.bk.ass.bt;

public abstract class TreeNode {
  protected NodeStatus status;

  public void init() {
  }

  public void close() {
  }

  public abstract void exec();

  public double getUtility() {
    return 0;
  }

  public final NodeStatus getStatus() {
    return status;
  }

  public final void success() {
    status = NodeStatus.SUCCESS;
  }

  public final void running() {
    status = NodeStatus.RUNNING;
  }

  public final void failed() {
    status = NodeStatus.FAILURE;
  }

  public void reset() {
    status = NodeStatus.INITIAL;
  }
}

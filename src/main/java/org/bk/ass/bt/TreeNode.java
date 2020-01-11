package org.bk.ass.bt;

public abstract class TreeNode {

  String name = getClass().getSimpleName();
  NodeStatus status;

  public void init() {
    status = NodeStatus.INITIAL;
  }

  public void close() {}

  public void exec(ExecutionContext executionContext) {
    exec();
  }

  public abstract void exec();

  /**
   * Used by {@link CompoundNode}s to determine order of execution. Generally, nodes with {@link
   * Selector} like behavior will run children in decreasing order of utility. Nodes with {@link
   * Sequence} like behavior will not change the order of children unless explicitly stated.
   */
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

  public void abort() {
    status = NodeStatus.ABORTED;
  }

  public TreeNode withName(String name) {
    this.name = name;
    return this;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name + "{" + "status=" + status + '}';
  }
}

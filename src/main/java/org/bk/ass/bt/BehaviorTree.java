package org.bk.ass.bt;

import static java.util.Objects.requireNonNull;

public abstract class BehaviorTree extends TreeNode {

  private TreeNode root;

  protected abstract TreeNode getRoot();

  @Override
  public void init() {
    super.init();
    this.root =
        requireNonNull(getRoot(), "getRoot() should return a valid tree node, but returned null");
    root.init();
  }

  @Override
  public void exec(ExecutionContext executionContext) {
    checkInitWasCalled();
    root.exec(executionContext);
    status = root.getStatus();
  }

  @Override
  public final void exec() {
    exec(ExecutionContext.NOOP);
  }

  private TreeNode checkInitWasCalled() {
    return requireNonNull(root, "init() must be called before using the behavior tree");
  }

  @Override
  public void close() {
    checkInitWasCalled();
    root.close();
  }

  @Override
  public double getUtility() {
    checkInitWasCalled();
    return root.getUtility();
  }

  @Override
  public void reset() {
    checkInitWasCalled();
    root.reset();
  }
}

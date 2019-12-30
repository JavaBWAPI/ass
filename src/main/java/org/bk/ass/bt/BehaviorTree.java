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
  public final void exec() {
    checkInitWasCalled();
    root.exec();
    status = root.getStatus();
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

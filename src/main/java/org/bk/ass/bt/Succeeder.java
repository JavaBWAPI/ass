package org.bk.ass.bt;

/**
 * Never fails, even if the delegate fails.
 */
public class Succeeder extends Decorator {

  public Succeeder(TreeNode delegate) {
    super(delegate);
  }

  @Override
  protected void updateStatusFromDelegate(NodeStatus status) {
    if (status != NodeStatus.RUNNING) {
      success();
    } else {
      running();
    }
  }
}

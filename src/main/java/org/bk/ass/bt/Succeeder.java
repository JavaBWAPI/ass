package org.bk.ass.bt;

/**
 * Never fails, but
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

package org.bk.ass.bt;

public class Succeeder extends Decorator {

  public Succeeder(TreeNode delegate) {
    super(delegate);
  }

  @Override
  protected void updateStatusFromDelegate(NodeStatus status) {
    success();
  }
}

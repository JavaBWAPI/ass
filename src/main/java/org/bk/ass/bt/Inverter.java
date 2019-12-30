package org.bk.ass.bt;

public class Inverter extends Decorator {

  public Inverter(TreeNode delegate) {
    super(delegate);
  }

  @Override
  protected void updateStatusFromDelegate(NodeStatus status) {
    if (status == NodeStatus.SUCCESS) failed();
    else if (status == NodeStatus.FAILURE) success();
    else running();
  }
}

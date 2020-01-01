package org.bk.ass.bt;

public class Memo extends Decorator {

  public Memo(TreeNode delegate) {
    super(delegate);
  }

  @Override
  public void exec(ExecutionContext context) {
    if (status == NodeStatus.SUCCESS || status == NodeStatus.FAILURE) return;
    super.exec(context);
  }
}

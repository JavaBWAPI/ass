package org.bk.ass.bt;

public class Sequence extends CompoundNode {

  protected Sequence(TreeNode... children) {
    super(children);
  }

  @Override
  public void exec() {
    for (TreeNode child : children) {
      child.exec();
      if (child.getStatus() != NodeStatus.SUCCESS) {
        status = child.getStatus();
        return;
      }
    }
    success();
  }
}

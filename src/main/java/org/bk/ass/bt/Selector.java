package org.bk.ass.bt;

public class Selector extends CompoundNode {

  public Selector(TreeNode... children) {
    super(children);
  }

  @Override
  public void exec() {
    children.sort(UTILITY_COMPARATOR);
    for (TreeNode child : children) {
      child.exec();
      if (child.getStatus() != NodeStatus.FAILURE) {
        status = child.getStatus();
        return;
      }
    }
    failed();
  }
}

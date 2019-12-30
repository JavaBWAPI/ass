package org.bk.ass.bt;

public class Parallel extends CompoundNode {

  private Policy policy;

  public enum Policy {
    SEQUENCE,
    SELECTOR;
  }

  protected Parallel(Policy policy, TreeNode... children) {
    super(children);
    this.policy = policy;
  }

  @Override
  public void exec() {
    if (policy == Policy.SELECTOR) {
      children.sort(UTILITY_COMPARATOR);
      status = NodeStatus.FAILURE;
      execChildren(NodeStatus.SUCCESS);
    } else if (policy == Policy.SEQUENCE) {
      status = NodeStatus.SUCCESS;
      execChildren(NodeStatus.FAILURE);
    }
  }

  private void execChildren(NodeStatus statusToStop) {
    for (TreeNode child : children) {
      child.exec();
      if (child.getStatus() == statusToStop) {
        status = child.getStatus();
        break;
      } else if (child.getStatus() == NodeStatus.RUNNING) status = NodeStatus.RUNNING;
    }
  }
}

package org.bk.ass.bt;

import org.bk.ass.StopWatch;

public class Parallel extends CompoundNode {

  private Policy policy;

  public enum Policy {
    SEQUENCE,
    SELECTOR
  }

  public Parallel(Policy policy, TreeNode... children) {
    super(children);
    this.policy = policy;
  }

  @Override
  public void exec(ExecutionContext context) {
    if (policy == Policy.SELECTOR) {
      children.sort(UTILITY_COMPARATOR);
      status = NodeStatus.FAILURE;
      execChildren(context, NodeStatus.SUCCESS);
    } else if (policy == Policy.SEQUENCE) {
      status = NodeStatus.SUCCESS;
      execChildren(context, NodeStatus.FAILURE);
    }
  }

  private void execChildren(ExecutionContext context, NodeStatus statusToStop) {
    StopWatch stopWatch = new StopWatch();
    for (TreeNode child : children) {
      execChild(child, context);
      if (child.getStatus() == statusToStop) {
        status = child.getStatus();
        abortRunningChildren();
        break;
      } else if (child.getStatus() == NodeStatus.RUNNING) status = NodeStatus.RUNNING;
    }
    stopWatch.registerWith(context, this);
  }
}

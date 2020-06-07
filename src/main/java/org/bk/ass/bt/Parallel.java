package org.bk.ass.bt;

import org.bk.ass.StopWatch;

/**
 * Using a {@link Policy}, a behavior similar to {@link Sequence} or {@link Selector}. But Parallel
 * will not wait for a {@link NodeStatus#RUNNING} node to complete and just {@link
 * #exec(ExecutionContext)} the next child node.
 */
public class Parallel extends CompoundNode {

  private final Policy policy;

  public enum Policy {
    /**
     * Run all children in order, stop if one has {@link NodeStatus#FAILURE}
     */
    SEQUENCE,
    /**
     * Run all children in order, stop if one has {@link NodeStatus#SUCCESS}
     */
    SELECTOR
  }

  /**
   * Initializes with the default policy {@link Policy#SEQUENCE}.
   */
  public Parallel(TreeNode... children) {
    super(children);
    policy = Policy.SEQUENCE;
  }

  public Parallel(Policy policy, TreeNode... children) {
    super(children);
    this.policy = policy;
  }

  @Override
  public void exec(ExecutionContext context) {
    children.sort(UTILITY_COMPARATOR);
    if (policy == Policy.SELECTOR) {
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

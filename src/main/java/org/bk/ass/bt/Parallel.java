package org.bk.ass.bt;

import java.util.Objects;
import org.bk.ass.StopWatch;

/**
 * Using a {@link Policy}, a behavior similar to {@link Sequence} or {@link Selector}. But Parallel
 * will not wait for a {@link NodeStatus#RUNNING} node to complete and just {@link
 * #exec(ExecutionContext)} the next child node.
 */
public class Parallel extends CompoundNode {

  private final Policy policy;
  private NodeStatus statusToSet;

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
  public Parallel(String name, TreeNode... children) {
    super(name, children);
    policy = Policy.SEQUENCE;
  }

  /**
   * Initializes with the default policy {@link Policy#SEQUENCE}.
   */
  public Parallel(TreeNode... children) {
    super(children);
    policy = Policy.SEQUENCE;
  }

  public Parallel(String name, Policy policy, TreeNode... children) {
    super(name, children);
    Objects.requireNonNull(policy);
    this.policy = policy;
  }

  public Parallel(Policy policy, TreeNode... children) {
    super(children);
    Objects.requireNonNull(policy);
    this.policy = policy;
  }

  @Override
  public void startExecPhase() {
    super.startExecPhase();
    if (policy == Policy.SELECTOR) {
      statusToSet = NodeStatus.FAILURE;
    } else if (policy == Policy.SEQUENCE) {
      statusToSet = NodeStatus.SUCCESS;
    }
  }

  @Override
  protected void exec(ExecutionContext context) {
    if (policy == Policy.SELECTOR) {
      execChildren(context, NodeStatus.SUCCESS);
    } else if (policy == Policy.SEQUENCE) {
      execChildren(context, NodeStatus.FAILURE);
    }
  }

  private void execChildren(ExecutionContext context, NodeStatus statusToStop) {
    StopWatch stopWatch = new StopWatch();
    if (!remainingChildren.isEmpty()) {
      TreeNode currentChild = nextMaxUtilityChild();
      execChild(currentChild, context);
      NodeStatus nodeStatus = currentChild.status;
      if (nodeStatus != NodeStatus.INCOMPLETE) {
        remainingChildren.remove(currentChild);
        if (nodeStatus == statusToStop) {
          statusToSet = nodeStatus;
          abortRunningChildren();
          remainingChildren.clear();
        } else if (nodeStatus == NodeStatus.RUNNING) {
          statusToSet = nodeStatus;
        }
      }
    }
    if (remainingChildren.isEmpty() && status == NodeStatus.INCOMPLETE) {
      status = statusToSet;
    }
    stopWatch.registerWith(context, this);
  }
}

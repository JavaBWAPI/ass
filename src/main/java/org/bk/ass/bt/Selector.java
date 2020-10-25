package org.bk.ass.bt;

import org.bk.ass.StopWatch;

/**
 * Reactive Selector node: Every time {@link #exec(ExecutionContext)} is called all nodes are
 * executed in order. Even if they are in a non-running state. {@link NodeStatus#SUCCESS} will be
 * returned, as soon as any child node succeeds.
 *
 * <p>In case it succeeds, all children in state {@link NodeStatus#RUNNING} will get aborted.
 */
public class Selector extends CompoundNode {

  public Selector(String name, TreeNode... children) {
    super(name, children);
  }

  public Selector(TreeNode... children) {
    super(children);
  }

  @Override
  protected void exec(ExecutionContext context) {
    StopWatch stopWatch = new StopWatch();
    if (!remainingChildren.isEmpty()) {
      TreeNode toExec = nextMaxUtilityChild();
      execChild(toExec, context);
      NodeStatus childStatus = toExec.status;
      if (childStatus != NodeStatus.INCOMPLETE) {
        remainingChildren.remove(toExec);
        if (childStatus != NodeStatus.FAILURE) {
          this.status = childStatus;
          if (childStatus == NodeStatus.SUCCESS) {
            abortRunningChildren();
          }
          remainingChildren.clear();
        } else if (remainingChildren.isEmpty()) {
          failed();
        }
      }
    } else {
      failed();
    }
    stopWatch.registerWith(context, this);
  }
}

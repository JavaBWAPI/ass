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

  public Selector(TreeNode... children) {
    super(children);
  }

  @Override
  public void exec(ExecutionContext context) {
    StopWatch stopWatch = new StopWatch();
    children.sort(UTILITY_COMPARATOR);
    for (TreeNode child : children) {
      execChild(child, context);
      if (child.status != NodeStatus.FAILURE) {
        status = child.getStatus();
        if (child.status == NodeStatus.SUCCESS) abortRunningChildren();
        stopWatch.registerWith(context, this);
        return;
      }
    }
    failed();
    stopWatch.registerWith(context, this);
  }
}

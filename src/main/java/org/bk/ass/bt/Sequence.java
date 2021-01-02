package org.bk.ass.bt;

import org.bk.ass.StopWatch;

/**
 * Reactive Sequence node: Every time {@link #exec(ExecutionContext)} is called all nodes are
 * executed in order. Even if they are in a non-running state. {@link NodeStatus#SUCCESS} will only
 * be returned, if all child nodes return that value.
 *
 * <p>If any child fails, the Sequence will also fail. In that case, all children in state {@link
 * NodeStatus#RUNNING} will get aborted.
 */
public class Sequence extends CompoundNode {

  public Sequence(TreeNode... children) {
    super(children);
  }

  public Sequence(String name, TreeNode... children) {
    super(children);
    withName(name);
  }

  @Override
  public void exec(ExecutionContext context) {
    StopWatch stopWatch = new StopWatch();
    for (TreeNode child : children) {
      execChild(child, context);
      if (child.status != NodeStatus.SUCCESS) {
        status = child.getStatus();
        if (child.status == NodeStatus.FAILURE) {
          abortRunningChildren();
        }
        stopWatch.registerWith(context, this);
        return;
      }
    }
    success();
    stopWatch.registerWith(context, this);
  }
}

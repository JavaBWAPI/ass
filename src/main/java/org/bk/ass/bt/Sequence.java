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

  public Sequence(String name, TreeNode... children) {
    super(name, children);
  }

  public Sequence(TreeNode... children) {
    super(children);
  }

  @Override
  protected void exec(ExecutionContext context) {
    StopWatch stopWatch = new StopWatch();
    if (!remainingChildren.isEmpty()) {
      TreeNode toExec = remainingChildren.get(0);
      execChild(toExec, context);
      NodeStatus childStatus = toExec.status;
      if (childStatus != NodeStatus.INCOMPLETE) {
        remainingChildren.remove(0);
        if (childStatus != NodeStatus.SUCCESS) {
          status = childStatus;
          if (childStatus == NodeStatus.FAILURE) {
            abortRunningChildren();
          }
          remainingChildren.clear();
        } else if (remainingChildren.isEmpty()) {
          success();
        }
      }
    } else {
      success();
    }
    stopWatch.registerWith(context, this);
  }
}

package org.bk.ass.bt;

import org.bk.ass.StopWatch;

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

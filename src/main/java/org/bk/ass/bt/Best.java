package org.bk.ass.bt;

/**
 * Will execute the child with maximum utility first like {@link Selector}. But will only ever
 * execute one child, regardless of it failing or not.
 */
public class Best extends CompoundNode {

  public Best(TreeNode... children) {
    super(children);
  }

  @Override
  protected void exec(ExecutionContext executionContext) {
    if (!remainingChildren.isEmpty()) {
      TreeNode toExec = nextMaxUtilityChild();
      execChild(toExec, executionContext);
      if (toExec.status != NodeStatus.INCOMPLETE) {
        status = toExec.status;
        abortRunningChildren();
        remainingChildren.clear();
      }
    } else {
      success();
    }
  }
}

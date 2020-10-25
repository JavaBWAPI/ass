package org.bk.ass.bt;

/**
 *
 */
public final class Executor {

  private Executor() {
    // Utility class
  }

  public static void execute(TreeNode node) {
    execute(ExecutionContext.NOOP, node);
  }

  public static void execute(ExecutionContext context, TreeNode node) {
    node.startExecPhase();
    do {
      node.exec(context);
    } while (node.status == NodeStatus.INCOMPLETE);
  }
}

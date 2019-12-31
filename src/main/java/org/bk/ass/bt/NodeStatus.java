package org.bk.ass.bt;

public enum NodeStatus {
  INITIAL,
  RUNNING,
  SUCCESS,
  FAILURE;

  public TreeNode after(Runnable block) {
    return new LambdaNode(
        () -> {
          block.run();
          return NodeStatus.this;
        });
  }
}

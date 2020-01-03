package org.bk.ass.bt;

public enum NodeStatus {
  INITIAL,
  RUNNING,
  SUCCESS,
  FAILURE,
  /** Child was in state RUNNING but will not be called again due to a parent having completed. */
  ABORTED {
    @Override
    public TreeNode after(Runnable block) {
      throw new UnsupportedOperationException("Abort should not be used this way.");
    }
  };

  public TreeNode after(Runnable block) {
    return new LambdaNode(
        () -> {
          block.run();
          return NodeStatus.this;
        });
  }
}

package org.bk.ass.bt;

/**
 * Status of a node.
 */
public enum NodeStatus {
  /**
   * Initial status of a node or a resetted node.
   */
  INITIAL,
  /**
   * Node did not fail, but also has not completed its task yet.
   */
  RUNNING,
  /**
   * Node is done, but can still be ticked and change its status.
   */
  SUCCESS,
  /**
   * Node has failed, but can still be ticked and change its status.
   */
  FAILURE,
  /**
   * Child was in state RUNNING but will not be ticked again due to a parent having completed.
   */
  ABORTED {
    @Override
    public TreeNode after(Runnable block) {
      throw new UnsupportedOperationException("Abort should not be used this way.");
    }
  },
  /**
   * Node needs more ticks to determine the result.
   */
  INCOMPLETE;


  /**
   * Executes the given code block but will return this NodeStatus' value.
   */
  public TreeNode after(Runnable block) {
    return new LambdaNode(
        () -> {
          block.run();
          return NodeStatus.this;
        });
  }
}

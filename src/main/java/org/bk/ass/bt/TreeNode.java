package org.bk.ass.bt;

import java.util.Objects;

/**
 * Base class of all of ASS' behavior trees.
 */
public abstract class TreeNode {

  final String name;
  NodeStatus status;

  protected TreeNode() {
    name = getClass().getSimpleName();
  }

  protected TreeNode(String name) {
    Objects.requireNonNull(name);
    this.name = name;
  }

  /**
   * Should be called before using this node.
   */
  public void init() {
    status = NodeStatus.INITIAL;
  }

  /**
   * Should be called if this node will not be used again and might perform some operations at the
   * end. If used for clean-ups, be aware that some nodes might get {@link #abort()}ed instead and
   * will not receive a close call.
   */
  public void close() {
  }

  /**
   *
   */
  protected void startExecPhase() {
  }

  protected void verifyExecution() {
    if (status == NodeStatus.INITIAL) {
      throw new IllegalStateException("Node " + this.name + " is still in initial state!");
    }
  }

  /**
   * Executes this node, with an {@link ExecutionContext} for meta data.
   */
  protected void exec(ExecutionContext executionContext) {
    exec();
  }

  /**
   * Executes this node without any meta data context.
   */
  protected abstract void exec();

  /**
   * Used by {@link CompoundNode}s to determine order of execution. Generally, nodes with {@link
   * Selector} like behavior will run children in decreasing order of utility. Nodes with {@link
   * Sequence} like behavior will not change the order of children unless explicitly stated. This
   * method might be called quite frequently, so make sure it runs fast.
   *
   * @return 0 by default.
   */
  public double getUtility() {
    return 0;
  }

  /**
   * Returns the status of the last execution.
   */
  public final NodeStatus getStatus() {
    return status;
  }

  /**
   * Mark node as successful.
   */
  protected final void success() {
    status = NodeStatus.SUCCESS;
  }

  /**
   * Mark node as running.
   */
  protected final void running() {
    status = NodeStatus.RUNNING;
  }

  /**
   * Mark node as failed.
   */
  protected final void failed() {
    status = NodeStatus.FAILURE;
  }

  /**
   * Aborts any operation of this node and marks the status as aborted. Subclasses should call
   * <pre>super.abort()</pre> on override.
   */
  public void abort() {
    status = NodeStatus.ABORTED;
  }

  /**
   * Resets the node and its status. If overridden, should always call <pre>super.reset()</pre> to
   * ensure status is correct.
   */
  public void reset() {
    init();
  }

  /**
   * The previously set name.
   */
  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return name + "{" + "status=" + status + '}';
  }
}

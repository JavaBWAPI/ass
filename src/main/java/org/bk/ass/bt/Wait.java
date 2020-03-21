package org.bk.ass.bt;

/**
 * Node which always is in status {@link NodeStatus#RUNNING}. Usually used in conjunction with a
 * {@link Sequence} and a {@link Condition} to make sure it's not waiting endlessly.
 */
public class Wait extends TreeNode {

  public static final Wait INSTANCE = new Wait();

  protected Wait() {

  }

  @Override
  public void init() {
    running();
  }

  @Override
  public void exec() {
    // Nothing to do
  }
}

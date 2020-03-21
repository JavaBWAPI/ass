package org.bk.ass.bt;

import java.util.function.Supplier;

/**
 * Allows inline nodes using lambdas.
 */
public class LambdaNode extends TreeNode {

  private final Supplier<NodeStatus> delegate;

  public LambdaNode(Supplier<NodeStatus> delegate) {
    this.delegate = delegate;

  }

  @Override
  public void exec() {
    status = delegate.get();
  }
}

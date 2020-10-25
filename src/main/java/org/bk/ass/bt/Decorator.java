package org.bk.ass.bt;

import java.util.Objects;

/**
 * Base class for nodes that delegate execution and modify the delegate or the status.
 */
public abstract class Decorator extends TreeNode {

  private final TreeNode delegate;

  public Decorator(String name, TreeNode delegate) {
    super(name);
    Objects.requireNonNull(delegate, "delegate must not be set");
    this.delegate = delegate;
  }

  public Decorator(TreeNode delegate) {
    Objects.requireNonNull(delegate, "delegate must not be set");
    this.delegate = delegate;
  }

  @Override
  public void init() {
    super.init();
    delegate.init();
  }

  @Override
  public void close() {
    delegate.close();
  }

  @Override
  public void startExecPhase() {
    delegate.startExecPhase();
  }

  @Override
  protected void exec(ExecutionContext executionContext) {
    executionContext.push(delegate);
    delegate.exec(executionContext);
    delegate.verifyExecution();
    executionContext.pop();
    updateStatusFromDelegate(delegate.getStatus());
  }

  @Override
  protected void exec() {
    exec(ExecutionContext.NOOP);
  }

  protected void updateStatusFromDelegate(NodeStatus status) {
    this.status = status;
  }

  @Override
  public double getUtility() {
    return delegate.getUtility();
  }

  @Override
  public void reset() {
    super.reset();
    delegate.reset();
  }

  @Override
  public void abort() {
    super.abort();
    delegate.abort();
  }

  @Override
  public String toString() {
    return name + "{" + "delegate=" + delegate + ", status=" + status + '}';
  }
}

package org.bk.ass.bt;

/**
 * Most nodes of <pre>ASS</pre> are reactive (they will execute no matter what the last execution
 * result was). Using this class, once the delegate reaches a non-running status the result will be
 * remembered. Further invocations will not change the status and the delegate will not be ticked
 * again.
 */
public class Memo extends Decorator {

  public Memo(TreeNode delegate) {
    super(delegate);
  }

  public Memo(String name, TreeNode delegate) {
    this(delegate);
    withName(name);
  }

  @Override
  public void exec(ExecutionContext context) {
    if (status == NodeStatus.SUCCESS || status == NodeStatus.FAILURE) {
      return;
    }
    super.exec(context);
  }
}

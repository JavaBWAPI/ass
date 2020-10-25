package org.bk.ass.bt;

import static java.util.Objects.requireNonNull;

/**
 * A behavior tree. Main difference to other compound nodes is that the actual root tree node is
 * constructed on initialization.
 * <p>
 * This allows data oriented programming, with simplified access to some data:
 * <pre>
 *   {@code
 *   public class MyTree extends BehaviorTree {
 *     // Data shared by nodes of this tree
 *     private Data myData = new Data(...);
 *     protected TreeNode getRoot() {
 *       return new Sequence(new SomeCondition(myData), new Action(myData));
 *     }
 *   }
 *   }
 * </pre>
 */
public abstract class BehaviorTree extends TreeNode {

  private TreeNode root;

  public BehaviorTree() {
  }

  public BehaviorTree(String name) {
    super(name);
  }

  /**
   * Will be called <em>once</em> to create the actual root to be ticked.
   */
  protected abstract TreeNode getRoot();

  @Override
  public void init() {
    super.init();
    this.root =
        requireNonNull(getRoot(), "getRoot() should return a valid tree node, but returned null");
    root.init();
  }

  @Override
  protected void exec(ExecutionContext executionContext) {
    checkInitWasCalled();
    root.exec(executionContext);
    status = root.getStatus();
  }

  @Override
  protected final void exec() {
    exec(ExecutionContext.NOOP);
  }

  private void checkInitWasCalled() {
    requireNonNull(root, "init() must be called before using the behavior tree");
  }

  @Override
  public void close() {
    checkInitWasCalled();
    root.close();
  }

  @Override
  public void startExecPhase() {
    checkInitWasCalled();
    root.startExecPhase();
  }

  @Override
  protected void verifyExecution() {
    root.verifyExecution();
  }

  @Override
  public double getUtility() {
    checkInitWasCalled();
    return root.getUtility();
  }

  @Override
  public void reset() {
    checkInitWasCalled();
    root.reset();
  }

  @Override
  public void abort() {
    checkInitWasCalled();
    root.abort();
  }
}

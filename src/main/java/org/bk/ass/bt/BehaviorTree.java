package org.bk.ass.bt;

import static java.util.Objects.requireNonNull;

/**
 * A behavior tree. Main difference to other compound nodes is that the actual root tree node is
 * constructed on initialization.
 * <p/>
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
  public void exec(ExecutionContext executionContext) {
    checkInitWasCalled();
    root.exec(executionContext);
    status = root.getStatus();
  }

  @Override
  public final void exec() {
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

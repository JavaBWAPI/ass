package org.bk.ass.bt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Base class for non-leaf nodes. Usually not subclassed directly.
 */
public abstract class CompoundNode extends TreeNode {

  protected static final Comparator<TreeNode> UTILITY_COMPARATOR =
      Comparator.comparing(TreeNode::getUtility).reversed();
  protected final List<TreeNode> children;

  protected CompoundNode(TreeNode... children) {
    this.children = new ArrayList<>(Arrays.asList(children));
  }

  @Override
  public abstract void exec(ExecutionContext context);

  @Override
  public void exec() {
    exec(ExecutionContext.NOOP);
  }

  protected void execChild(TreeNode child, ExecutionContext context) {
    context.push(child);
    child.exec(context);
    context.pop();
  }

  protected void abortRunningChildren() {
    for (TreeNode it : children) {
      if (it.status == NodeStatus.RUNNING) {
        it.abort();
      }
    }
  }

  @Override
  public void init() {
    super.init();
    children.forEach(TreeNode::init);
  }

  @Override
  public void close() {
    super.close();
    children.forEach(TreeNode::close);
  }

  /**
   * Will return the highest utility of all children which are still running or not yet executed.
   */
  @Override
  public double getUtility() {
    return children.stream()
        .filter(it -> it.status == NodeStatus.INITIAL || it.status == NodeStatus.RUNNING)
        .mapToDouble(TreeNode::getUtility).max().orElseGet(super::getUtility);
  }

  @Override
  public void reset() {
    super.reset();
    children.forEach(TreeNode::reset);
  }

  @Override
  public void abort() {
    super.abort();
    abortRunningChildren();
  }

  @Override
  public String toString() {
    return name + "{" + "children=" + children + ", status=" + status + '}';
  }
}

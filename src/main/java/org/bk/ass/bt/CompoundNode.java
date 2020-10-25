package org.bk.ass.bt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Base class for non-leaf nodes. Usually not subclassed directly.
 */
public abstract class CompoundNode extends TreeNode {

  static final Comparator<TreeNode> UTILITY_COMPARATOR =
      Comparator.comparing(TreeNode::getUtility);
  private final List<TreeNode> children;
  protected List<TreeNode> remainingChildren;

  protected CompoundNode(String name, TreeNode... children) {
    super(name);
    Objects.requireNonNull(children, "children must be set");

    this.children = new ArrayList<>(Arrays.asList(children));
  }

  protected CompoundNode(TreeNode... children) {
    Objects.requireNonNull(children, "children must be set");

    this.children = new ArrayList<>(Arrays.asList(children));
  }

  @Override
  protected abstract void exec(ExecutionContext context);

  @Override
  protected void exec() {
    exec(ExecutionContext.NOOP);
  }

  protected void execChild(TreeNode child, ExecutionContext context) {
    context.push(child);
    child.exec(context);
    child.verifyExecution();
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

  @Override
  public void startExecPhase() {
    super.startExecPhase();
    status = NodeStatus.INCOMPLETE;
    children.forEach(TreeNode::startExecPhase);
    remainingChildren = new ArrayList<>(children);
  }

  protected TreeNode nextMaxUtilityChild() {
    return remainingChildren.stream().max(UTILITY_COMPARATOR)
        .orElseThrow(IllegalStateException::new);
  }

  /**
   * Will return the highest utility of all children which are still running or not yet executed.
   */
  @Override
  public double getUtility() {
    return remainingChildren.stream()
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

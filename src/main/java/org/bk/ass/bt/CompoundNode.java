package org.bk.ass.bt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

public abstract class CompoundNode extends TreeNode {
  protected static final Comparator<TreeNode> UTILITY_COMPARATOR = Comparator.comparing(TreeNode::getUtility).reversed();
  protected final List<TreeNode> children;

  protected CompoundNode(TreeNode... children) {
    this.children = new ArrayList<>(Arrays.asList(children));
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
  public double getUtility() {
    return children.stream()
        .mapToDouble(TreeNode::getUtility).max()
        .orElseGet(super::getUtility);
  }

  @Override
  public void reset() {
    super.reset();
    children.forEach(TreeNode::reset);
  }
}

package org.bk.ass.bt;

import java.util.Comparator;
import java.util.Optional;

public class Best extends CompoundNode {

  public Best(TreeNode... children) {
    super(children);
  }

  @Override
  public void exec() {
    Optional<TreeNode> bestNode =
        children.stream().max(Comparator.comparingDouble(TreeNode::getUtility));
    if (bestNode.isPresent()) {
      TreeNode delegate = bestNode.get();
      delegate.exec();
      status = delegate.getStatus();
    } else success();
  }
}

package org.bk.ass.bt;

import java.util.Comparator;
import java.util.Optional;

/**
 * Will execute the child with maximum utility first like {@link Selector}. But will only ever
 * execute one child, regardless of it failing or not.
 */
public class Best extends CompoundNode {

  public Best(TreeNode... children) {
    super(children);
  }

  @Override
  public void exec(ExecutionContext executionContext) {
    Optional<TreeNode> bestNode =
        children.stream().max(Comparator.comparingDouble(TreeNode::getUtility));
    if (bestNode.isPresent()) {
      TreeNode delegate = bestNode.get();
      children.stream()
          .filter(it -> it.status == NodeStatus.RUNNING && it != delegate)
          .forEach(TreeNode::abort);
      execChild(delegate, executionContext);
      status = delegate.getStatus();
    } else success();
  }
}

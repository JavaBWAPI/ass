package org.bk.ass.bt;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import org.bk.ass.bt.Parallel.Policy;

/**
 * This is like <code>map</code> for streams. Maps each item of a given list to a node. New nodes
 * will only be created for new items. Nodes for items that are no longer present will be aborted
 * and discarded.
 * <p>
 * The created child nodes will be executed in parallel in the order of the item list.
 *
 * @param <T> the type of the item used to create new nodes
 */
public class Distributor<T> extends TreeNode {

  private Policy policy;
  private final Supplier<Collection<T>> itemSupplier;
  private final Function<T, TreeNode> nodeFactory;
  private final Map<T, TreeNode> itemNodes = new HashMap<>();
  private List<TreeNode> remainingChildren;
  private NodeStatus statusToSet;

  public Distributor(
      Policy policy, Supplier<Collection<T>> itemSupplier, Function<T, TreeNode> nodeFactory) {
    this.policy = requireNonNull(policy);
    this.itemSupplier = requireNonNull(itemSupplier);
    this.nodeFactory = requireNonNull(nodeFactory);
  }

  @Override
  public void startExecPhase() {
    super.startExecPhase();
    status = NodeStatus.INCOMPLETE;
    itemNodes.values().forEach(TreeNode::startExecPhase);

    if (policy == Policy.SELECTOR) {
      statusToSet = NodeStatus.FAILURE;
    } else if (policy == Policy.SEQUENCE) {
      statusToSet = NodeStatus.SUCCESS;
    }
    remainingChildren = updateMappedNodes().stream().map(itemNodes::get)
        .collect(Collectors.toList());
  }

  @Override
  protected void exec() {
    exec(ExecutionContext.NOOP);
  }

  @Override
  protected void exec(ExecutionContext executionContext) {
    if (!remainingChildren.isEmpty()) {
      TreeNode toExec;
      if (policy == Policy.SELECTOR) {
        toExec =
            remainingChildren.stream()
                .max(CompoundNode.UTILITY_COMPARATOR)
                .orElseThrow(IllegalStateException::new);
      } else {
        toExec = remainingChildren.get(0);
      }
      execChild(toExec, executionContext);
      NodeStatus toExecStatus = toExec.status;
      if (toExecStatus != NodeStatus.INCOMPLETE) {
        remainingChildren.remove(toExec);
        if (toExecStatus == NodeStatus.SUCCESS && policy == Policy.SELECTOR
            || toExecStatus == NodeStatus.FAILURE && policy == Policy.SEQUENCE) {
          statusToSet = toExecStatus;
          abortRunningChildren();
          remainingChildren.clear();
        } else if (toExecStatus == NodeStatus.RUNNING) {
          statusToSet = NodeStatus.RUNNING;
        }
      }
    }
    if (remainingChildren.isEmpty() && status == NodeStatus.INCOMPLETE) {
      status = statusToSet;
    }
  }

  private void abortRunningChildren() {
    itemNodes.values().stream()
        .filter(it -> it.status == NodeStatus.RUNNING)
        .forEach(TreeNode::abort);
  }

  private void execChild(TreeNode child, ExecutionContext context) {
    context.push(child);
    child.exec(context);
    context.pop();
  }

  private Collection<T> updateMappedNodes() {
    Collection<T> updatedItemList = itemSupplier.get();
    Iterator<Entry<T, TreeNode>> it = itemNodes.entrySet().iterator();
    while (it.hasNext()) {
      Entry<T, TreeNode> next = it.next();
      if (!updatedItemList.contains(next.getKey())) {
        TreeNode nodeToRemove = next.getValue();
        if (nodeToRemove.status == NodeStatus.RUNNING) nodeToRemove.abort();
        it.remove();
      }
    }
    updatedItemList.forEach(
        item ->
            itemNodes.computeIfAbsent(
                item,
                itemForNode -> {
                  TreeNode node = nodeFactory.apply(itemForNode);
                  node.init();
                  node.startExecPhase();
                  return node;
                }));
    return updatedItemList;
  }

  @Override
  public void reset() {
    super.reset();
    itemNodes.values().forEach(TreeNode::reset);
  }

  @Override
  public void abort() {
    super.abort();
    itemNodes.values().stream()
        .filter(it -> it.status == NodeStatus.RUNNING)
        .forEach(TreeNode::abort);
  }

  @Override
  public void init() {
    super.init();
    itemNodes.values().forEach(TreeNode::init);
  }

  @Override
  public void close() {
    super.close();
    itemNodes.values().forEach(TreeNode::close);
  }
}

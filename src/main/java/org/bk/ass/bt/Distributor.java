package org.bk.ass.bt;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.bk.ass.StopWatch;
import org.bk.ass.bt.Parallel.Policy;

public class Distributor<T> extends TreeNode {

  private Policy policy;
  private final Supplier<Collection<T>> itemSupplier;
  private final Function<T, TreeNode> nodeFactory;
  private final Map<T, TreeNode> itemNodes = new HashMap<>();

  public Distributor(
      Policy policy, Supplier<Collection<T>> itemSupplier, Function<T, TreeNode> nodeFactory) {
    this.policy = requireNonNull(policy);
    this.itemSupplier = requireNonNull(itemSupplier);
    this.nodeFactory = requireNonNull(nodeFactory);
  }

  @Override
  public void exec() {
    exec(ExecutionContext.NOOP);
  }

  @Override
  public void exec(ExecutionContext executionContext) {
    Collection<T> mappedItems = updateMappedNodes();

    if (policy == Policy.SELECTOR) {
      Stream<TreeNode> children = mappedItems.stream().map(itemNodes::get);
      status = NodeStatus.FAILURE;
      execChildren(executionContext, children, NodeStatus.SUCCESS);
    } else if (policy == Policy.SEQUENCE) {
      Stream<TreeNode> children =
          itemNodes.values().stream().sorted(Comparator.comparing(TreeNode::getUtility).reversed());
      status = NodeStatus.SUCCESS;
      execChildren(executionContext, children, NodeStatus.FAILURE);
    }
  }

  private void execChildren(
      ExecutionContext context, Stream<TreeNode> children, NodeStatus stopStatus) {
    StopWatch stopWatch = new StopWatch();
    children
        .filter(
            child -> {
              execChild(child, context);
              if (child.status == stopStatus) {
                status = child.status;
                abortRunningChildren();
                return true;
              } else if (child.status == NodeStatus.RUNNING) status = NodeStatus.RUNNING;

              return false;
            })
        .findFirst(); // Workaround for missing takeWhile
    stopWatch.registerWith(context, this);
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

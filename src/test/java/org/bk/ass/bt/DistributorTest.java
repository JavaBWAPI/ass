package org.bk.ass.bt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import org.bk.ass.bt.Parallel.Policy;
import org.junit.jupiter.api.Test;

class DistributorTest {

  @Test
  void shouldCreateNodePerItem() {
    // GIVEN
    AtomicInteger nodeCreationCalls = new AtomicInteger();
    Distributor<String> sut =
        new Distributor<>(
            Policy.SEQUENCE,
            () -> Arrays.asList("a", "b"),
            s -> {
              nodeCreationCalls.incrementAndGet();
              return new LambdaNode(() -> NodeStatus.RUNNING);
            });

    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(nodeCreationCalls).hasValue(2);
  }

  @Test
  void shouldRunIfAllChildrenRun() {
    // GIVEN
    Distributor<String> sut =
        new Distributor<>(
            Policy.SEQUENCE,
            () -> Arrays.asList("a", "b"),
            s -> new LambdaNode(() -> NodeStatus.RUNNING));

    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }

  @Test
  void shouldFailIfAllChildrenFail() {
    // GIVEN
    Distributor<String> sut =
        new Distributor<>(
            Policy.SEQUENCE,
            () -> Arrays.asList("a", "b"),
            s -> new LambdaNode(() -> NodeStatus.FAILURE));

    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.FAILURE);
  }

  @Test
  void shouldFailIfAnyChildFails() {
    // GIVEN
    Distributor<String> sut =
        new Distributor<>(
            Policy.SEQUENCE,
            () -> Arrays.asList("a", "b"),
            s -> new LambdaNode(() -> "a".equals(s) ? NodeStatus.RUNNING : NodeStatus.FAILURE));

    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.FAILURE);
  }

  @Test
  void shouldNotFailIfAnyChildIsRunning() {
    // GIVEN
    Distributor<String> sut =
        new Distributor<>(
            Policy.SELECTOR,
            () -> Arrays.asList("a", "b"),
            s -> new LambdaNode(() -> "a".equals(s) ? NodeStatus.RUNNING : NodeStatus.FAILURE));

    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }

  @Test
  void shouldSucceedIfAnyChildSucceeds() {
    // GIVEN
    Distributor<String> sut =
        new Distributor<>(
            Policy.SELECTOR,
            () -> Arrays.asList("a", "b"),
            s -> new LambdaNode(() -> "a".equals(s) ? NodeStatus.FAILURE : NodeStatus.SUCCESS));

    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.SUCCESS);
  }

  @Test
  void shouldRemoveNodesForRemovedItems() {
    // GIVEN
    AtomicInteger nodeExecCalls = new AtomicInteger();
    ArrayList<String> items = new ArrayList<>();
    Distributor<String> sut =
        new Distributor<>(
            Policy.SELECTOR,
            () -> items,
            s -> new LambdaNode(() -> {
              nodeExecCalls.incrementAndGet();
              return NodeStatus.RUNNING;
            }));
    items.add("a");
    items.add("b");
    Executor.execute(sut);
    items.remove(1);

    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(nodeExecCalls).hasValue(2 + 1);
  }

  @Test
  void shouldAddNodesForAddedItems() {
    // GIVEN
    AtomicInteger nodeExecCalls = new AtomicInteger();
    ArrayList<String> items = new ArrayList<>();
    Distributor<String> sut =
        new Distributor<>(
            Policy.SELECTOR,
            () -> items,
            s -> new LambdaNode(() -> {
              nodeExecCalls.incrementAndGet();
              return NodeStatus.RUNNING;
            }));
    items.add("a");
    Executor.execute(sut);
    items.add("b");

    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(nodeExecCalls).hasValue(2 + 1);
  }

  @Test
  void shouldCallInitForNewNodes() {
    // GIVEN
    TreeNode treeNode = new TreeNode() {

      @Override
      public void exec() {
      }
    };
    Distributor<String> sut =
        new Distributor<>(
            Policy.SELECTOR,
            () -> Collections.singleton("a"),
            s -> treeNode);

    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(treeNode.status).isEqualTo(NodeStatus.INITIAL);
  }
}

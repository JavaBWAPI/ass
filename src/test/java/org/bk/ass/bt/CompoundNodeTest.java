package org.bk.ass.bt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CompoundNodeTest {

  @Test
  void shouldReturnUtilityOfChildInInitialState() {
    // GIVEN
    DummyCompound sut = new DummyCompound(
        new NodeWithUtility(1.0, NodeStatus.SUCCESS),
        new NodeWithUtility(0.5, NodeStatus.INITIAL)
    );
    sut.startExecPhase();

    // WHEN
    double utility = sut.getUtility();

    // THEN
    assertThat(utility).isEqualTo(0.5);
  }

  @Test
  void shouldReturnUtilityOfChildInRunningState() {
    // GIVEN
    DummyCompound sut = new DummyCompound(
        new NodeWithUtility(1.0, NodeStatus.SUCCESS),
        new NodeWithUtility(0.5, NodeStatus.RUNNING)
    );
    sut.startExecPhase();

    // WHEN
    double utility = sut.getUtility();

    // THEN
    assertThat(utility).isEqualTo(0.5);
  }

  @Test
  void shouldReturnHighestUtilityOfChildren() {
    // GIVEN
    DummyCompound sut = new DummyCompound(
        new NodeWithUtility(1.0, NodeStatus.INITIAL),
        new NodeWithUtility(0.5, NodeStatus.RUNNING)
    );
    sut.startExecPhase();

    // WHEN
    double utility = sut.getUtility();

    // THEN
    assertThat(utility).isEqualTo(1.0);
  }

  private static class DummyCompound extends CompoundNode {

    public DummyCompound(TreeNode... children) {
      super(children);
    }

    @Override
    public void exec(ExecutionContext context) {

    }
  }

  private static class NodeWithUtility extends TreeNode {

    private final double utility;

    NodeWithUtility(double utility, NodeStatus status) {
      this.utility = utility;
      this.status = status;
    }

    @Override
    public double getUtility() {
      return utility;
    }

    @Override
    public void exec() {

    }
  }
}
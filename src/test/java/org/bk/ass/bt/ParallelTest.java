package org.bk.ass.bt;

import static org.assertj.core.api.Assertions.assertThat;

import org.bk.ass.bt.Parallel.Policy;
import org.junit.jupiter.api.Test;

class ParallelTest {

  @Test
  void shouldSucceedWithSelectorPolicyAndChildWithSuccess() {
    // GIVEN
    Parallel sut =
        new Parallel(Policy.SELECTOR,
            new LambdaNode(() -> NodeStatus.RUNNING),
            new LambdaNode(() -> NodeStatus.SUCCESS));

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.SUCCESS);
  }
  @Test
  void shouldKeepRunningWithSelectorPolicyWithFailedChildButAlsoRunningChild() {
    // GIVEN
    Parallel sut =
        new Parallel(Policy.SELECTOR,
            new LambdaNode(() -> NodeStatus.FAILURE),
            new LambdaNode(() -> NodeStatus.RUNNING));

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }

  @Test
  void shouldFailWithSequencePolicyAndChildWithFailure() {
    // GIVEN
    Parallel sut =
        new Parallel(Policy.SEQUENCE,
            new LambdaNode(() -> NodeStatus.RUNNING),
            new LambdaNode(() -> NodeStatus.FAILURE));

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.FAILURE);
  }

  @Test
  void shouldKeepRunningWithSequencePolicyAndRunningChild() {
    // GIVEN
    Parallel sut =
        new Parallel(Policy.SEQUENCE,
            new LambdaNode(() -> NodeStatus.SUCCESS),
            new LambdaNode(() -> NodeStatus.RUNNING));

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }
}
package org.bk.ass.bt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SequenceTest {

  @Test
  void shouldSucceedIfEmpty() {
    // GIVEN
    Sequence sut = new Sequence();

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.SUCCESS);
  }

  @Test
  void shouldFailIfAnyChildFails() {
    // GIVEN
    Sequence sut =
        new Sequence(
            new LambdaNode(() -> NodeStatus.SUCCESS), new LambdaNode(() -> NodeStatus.FAILURE));

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.FAILURE);
  }

  @Test
  void shouldRunIfNoChildFailsAndAnyIsRunning() {
    // GIVEN
    Sequence sut =
        new Sequence(
            new LambdaNode(() -> NodeStatus.SUCCESS),
            new LambdaNode(() -> NodeStatus.RUNNING),
            new LambdaNode(() -> NodeStatus.FAILURE));

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }
}

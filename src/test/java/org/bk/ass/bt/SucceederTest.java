package org.bk.ass.bt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SucceederTest {
  @Test
  void shouldPassThroughSuccess() {
    // GIVEN
    Succeeder sut = new Succeeder(new LambdaNode(() -> NodeStatus.SUCCESS));

    // WHEN
    sut.exec();

    /// THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.SUCCESS);
  }

  @Test
  void shouldBeSuccessOnFailure() {
    // GIVEN
    Succeeder sut = new Succeeder(new LambdaNode(() -> NodeStatus.FAILURE));

    // WHEN
    sut.exec();

    /// THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.SUCCESS);
  }

  @Test
  void shouldBeSuccessOnRunning() {
    // GIVEN
    Succeeder sut = new Succeeder(new LambdaNode(() -> NodeStatus.RUNNING));

    // WHEN
    sut.exec();

    /// THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }

}
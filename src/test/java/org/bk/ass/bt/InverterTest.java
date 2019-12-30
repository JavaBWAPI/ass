package org.bk.ass.bt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InverterTest {

  @Test
  void shouldInvertSuccess() {
    // GIVEN
    Inverter sut = new Inverter(new LambdaNode(() -> NodeStatus.SUCCESS));

    // WHEN
    sut.exec();

    /// THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.FAILURE);
  }

  @Test
  void shouldInvertFailure() {
    // GIVEN
    Inverter sut = new Inverter(new LambdaNode(() -> NodeStatus.FAILURE));

    // WHEN
    sut.exec();

    /// THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.SUCCESS);
  }

  @Test
  void shouldNotInvertRunning() {
    // GIVEN
    Inverter sut = new Inverter(new LambdaNode(() -> NodeStatus.RUNNING));

    // WHEN
    sut.exec();

    /// THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }
}
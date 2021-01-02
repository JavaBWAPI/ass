package org.bk.ass.bt;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class SelectorTest {

  @Test
  void shouldFailIfEmpty() {
    // GIVEN
    Selector sut = new Selector();

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.FAILURE);
  }

  @Test
  void shouldFailIfAllChildFails() {
    // GIVEN
    Selector sut =
        new Selector(
            new LambdaNode(() -> NodeStatus.FAILURE), new LambdaNode(() -> NodeStatus.FAILURE));

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.FAILURE);
  }

  @Test
  void shouldSucceedIfChildSucceeds() {
    // GIVEN
    Selector sut =
        new Selector(
            new LambdaNode(() -> NodeStatus.FAILURE), new LambdaNode(() -> NodeStatus.SUCCESS));

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.SUCCESS);
  }

  @Test
  void shouldRunIfNoChildSucceedsAndAnyIsRunning() {
    // GIVEN
    Selector sut =
        new Selector(
            new LambdaNode(() -> NodeStatus.FAILURE),
            new LambdaNode(() -> NodeStatus.RUNNING),
            new LambdaNode(() -> NodeStatus.SUCCESS));

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }
}

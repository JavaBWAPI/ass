package org.bk.ass.bt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.bk.ass.bt.Repeat.Policy;
import org.junit.jupiter.api.Test;

class RepeatTest {
  @Test
  void shouldRepeatUntilLimit() {
    // GIVEN
    Repeat sut = new Repeat(Policy.SEQUENCE, 2, new LambdaNode(() -> NodeStatus.SUCCESS));
    sut.exec();

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.SUCCESS);
  }

  @Test
  void shouldRepeatUntilSuccess() {
    // GIVEN
    AtomicInteger calls = new AtomicInteger();
    Repeat sut = new Repeat(Policy.SELECTOR, new Condition(() -> calls.incrementAndGet() == 2));
    sut.exec();

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.SUCCESS);
  }

  @Test
  void shouldRepeatUntilFailure() {
    // GIVEN
    AtomicInteger calls = new AtomicInteger();
    Repeat sut = new Repeat(Policy.SELECTOR_INVERTED, new Condition(() -> calls.incrementAndGet() != 2));
    sut.exec();

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.SUCCESS);
  }

  @Test
  void shouldReportRunningIfChildIsRunning() {
    // GIVEN
    Repeat sut = new Repeat(Policy.SEQUENCE, new LambdaNode(() -> NodeStatus.RUNNING));

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }

  @Test
  void shouldReportRunningIfChildReportsSuccess() {
    // GIVEN
    Repeat sut = new Repeat(Policy.SEQUENCE, new LambdaNode(() -> NodeStatus.SUCCESS));

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }

  @Test
  void shouldReportRunningIfChildReportsFailure() {
    // GIVEN
    Repeat sut = new Repeat(Policy.SELECTOR, new LambdaNode(() -> NodeStatus.FAILURE));

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }

  @Test
  void shouldResetDelegateWhenRepeating() {
    // GIVEN
    AtomicInteger resetCalls = new AtomicInteger();
    Repeat sut = new Repeat(Policy.SEQUENCE, new TreeNode() {
      @Override
      public void exec() {
        success();
      }

      @Override
      public void reset() {
        super.reset();
        resetCalls.incrementAndGet();
      }
    });

    // WHEN
    sut.exec();

    // THEN
    assertThat(resetCalls).hasValue(1);
  }
}

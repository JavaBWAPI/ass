package org.bk.ass.bt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class MemoTest {

  @Test
  void shouldStopDelegationWhenSucceeded() {
    // GIVEN
    AtomicInteger calls = new AtomicInteger();
    Memo sut = new Memo(new Condition(() -> calls.getAndIncrement() == 0));
    sut.exec();

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.SUCCESS);
    assertThat(calls).hasValue(1);
  }

  @Test
  void shouldStopDelegationWhenFailed() {
    // GIVEN
    AtomicInteger calls = new AtomicInteger();
    Memo sut = new Memo(new Condition(() -> calls.getAndIncrement() == 1));
    sut.exec();

    // WHEN
    sut.exec();

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.FAILURE);
    assertThat(calls).hasValue(1);
  }
}
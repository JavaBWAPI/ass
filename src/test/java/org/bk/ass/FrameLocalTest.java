package org.bk.ass;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class FrameLocalTest {

  @Test
  public void shouldReturnSuppliedValue() {
    // GIVEN
    FrameLocal<Integer> frameLocal = new FrameLocal<>(() -> 1, () -> 2);

    // WHEN
    Integer value = frameLocal.get();

    // THEN
    assertThat(value).isEqualTo(2);
  }

  @Test
  public void shouldNotCallInitializerTwicePerFrame() {
    // GIVEN
    CountingSupplier<Integer> countingSupplier = new CountingSupplier<>(() -> 2);
    FrameLocal<Integer> frameLocal = new FrameLocal<>(() -> 1, countingSupplier);
    frameLocal.get();

    // WHEN
    frameLocal.get();

    // THEN
    assertThat(countingSupplier.calls).isEqualTo(1);
  }

  @Test
  public void shouldReturnSuppliedValueForMultipleInvocations() {
    // GIVEN
    FrameLocal<Integer> frameLocal = new FrameLocal<>(() -> 1, () -> 3);
    frameLocal.get();

    // WHEN
    Integer value = frameLocal.get();

    // THEN
    assertThat(value).isEqualTo(3);
  }

  @Test
  public void shouldRetrieveNewValueForNewFrame() {
    // GIVEN
    AtomicInteger frame = new AtomicInteger(1);
    AtomicInteger value = new AtomicInteger(1337);
    FrameLocal<Integer> frameLocal = new FrameLocal<>(frame::get, value::get);
    Integer firstValue = frameLocal.get();
    frame.set(2);
    value.set(815);

    // WHEN
    Integer secondValue = frameLocal.get();

    // THEN
    assertThat(firstValue).isEqualTo(1337);
    assertThat(secondValue).isEqualTo(815);
  }

  private static class CountingSupplier<T> implements Supplier<T> {

    private final Supplier<T> delegate;
    int calls;

    private CountingSupplier(Supplier<T> delegate) {
      this.delegate = delegate;
    }

    @Override
    public T get() {
      calls++;
      return delegate.get();
    }
  }
}
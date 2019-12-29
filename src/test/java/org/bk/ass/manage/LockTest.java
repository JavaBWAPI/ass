package org.bk.ass.manage;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class LockTest {

  private static final String STRING_ITEM = "Hello World";

  @Test
  void shouldReserveItem() {
    // GIVEN
    Reservation<String> reservation =
        new Reservation<String>() {
          @Override
          public boolean reserve(Lock<String> lock, String item) {
            return true;
          }

          @Override
          public void release(Lock<String> lock, String item) {}
        };
    Lock<String> sut = new Lock<>(reservation, () -> STRING_ITEM);

    // WHEN
    boolean acquire = sut.acquire();

    // THEN
    assertThat(acquire).isTrue();
    assertThat(sut.isSatisfied()).isTrue();
    assertThat(sut.isSatisfiedLater()).isTrue();
    assertThat(sut.getItem()).isEqualTo(STRING_ITEM);
  }

  @Test
  void shouldReleaseItem() {
    // GIVEN
    AtomicBoolean released = new AtomicBoolean();
    Reservation<String> reservation =
        new Reservation<String>() {
          @Override
          public boolean reserve(Lock<String> lock, String item) {
            return true;
          }

          @Override
          public void release(Lock<String> lock, String item) {
            released.set(true);
          }
        };
    Lock<String> sut = new Lock<>(reservation, () -> "Hello World");
    sut.acquire();

    // WHEN
    sut.release();

    // THEN
    assertThat(sut.isSatisfied()).isFalse();
    assertThat(sut.isSatisfiedLater()).isFalse();
    assertThat(released).isTrue();
  }

  @Test
  void shouldThrowExceptionIfItemWasNotLocked() {
    // GIVEN
    Reservation<String> reservation =
        new Reservation<String>() {
          @Override
          public boolean reserve(Lock<String> lock, String item) {
            return false;
          }

          @Override
          public void release(Lock<String> lock, String item) {}
        };
    Lock<String> sut = new Lock<>(reservation, () -> STRING_ITEM);

    // WHEN
    boolean satisfied = sut.acquire();

    // THEN
    assertThat(satisfied).isFalse();
    assertThat(sut.getItem()).isNotEmpty();
  }

  @Test
  void shouldResetItem() {
    // GIVEN
    Reservation<String> reservation =
        new Reservation<String>() {
          @Override
          public boolean reserve(Lock<String> lock, String item) {
            return true;
          }

          @Override
          public void release(Lock<String> lock, String item) {}
        };
    Lock<String> sut = new Lock<>(reservation, () -> "Hello World");
    sut.acquire();

    // WHEN
    sut.reset();

    // THEN
    assertThat(sut.isSatisfied()).isFalse();
    assertThat(sut.isSatisfiedLater()).isFalse();
  }

  @Test
  void shouldOnlyBeReservedOnce() {
    // GIVEN
    AtomicInteger reserveCalls = new AtomicInteger();
    Reservation<String> reservation =
        new Reservation<String>() {
          @Override
          public boolean reserve(Lock<String> lock, String item) {
            reserveCalls.incrementAndGet();
            return false;
          }

          @Override
          public void release(Lock<String> lock, String item) {}
        };
    Lock<String> sut = new Lock<>(reservation, () -> "Hello World");
    sut.acquire();
    reserveCalls.set(0);

    // WHEN
    sut.acquire();

    // THEN
    assertThat(reserveCalls).hasValue(1);
  }

  @Test
  public void shouldNotBeSatisfiedIfSelectorReturnsNull() {
    // GIVEN
    Reservation<String> reservation =
        new Reservation<String>() {
          @Override
          public boolean reserve(Lock<String> lock, String item) {
            return true;
          }

          @Override
          public boolean itemAvailableInFuture(Lock<String> lock, String item, int futureFrames) {
            return true;
          }

          @Override
          public void release(Lock<String> lock, String item) {}
        };
    Lock<String> sut = new Lock<>(reservation, () -> null);

    // WHEN
    boolean satisfied = sut.acquire();

    // THEN
    assertThat(satisfied).isFalse();
    assertThat(sut.isSatisfied()).isFalse();
    assertThat(sut.isSatisfiedLater()).isFalse();
  }

  @Test
  void shouldNotReleaseOnReacquire() {
    // GIVEN
    AtomicBoolean released = new AtomicBoolean();
    Reservation<String> reservation =
        new Reservation<String>() {
          @Override
          public boolean reserve(Lock<String> lock, String item) {
            return true;
          }

          @Override
          public void release(Lock<String> lock, String item) {
            released.set(true);
          }
        };
    Lock<String> sut = new Lock<>(reservation, () -> STRING_ITEM);
    sut.acquire();

    // WHEN
    sut.reacquire();

    // THEN
    assertThat(released).isFalse();
  }

  @Test
  public void shouldAddHysteresis() {
    // GIVEN
    AtomicInteger frames = new AtomicInteger();
    Reservation<String> reservation =
        new Reservation<String>() {
          @Override
          public boolean reserve(Lock<String> lock, String item) {
            return false;
          }

          @Override
          public boolean itemAvailableInFuture(Lock<String> lock, String item, int futureFrames) {
            frames.set(futureFrames);
            return true;
          }

          @Override
          public void release(Lock<String> lock, String item) {}
        };
    Lock<String> sut = new Lock<>(reservation, () -> STRING_ITEM);
    sut.acquire();

    // WHEN
    sut.acquire();

    // THEN
    assertThat(frames).hasValue(Lock.DEFAULT_HYSTERESIS);
  }

  @Test
  void shouldNotSelectIfPreviousItemIsOk() {
    AtomicInteger value = new AtomicInteger();
    Reservation<Integer> reservation =
        new Reservation<Integer>() {
          @Override
          public boolean reserve(Lock<Integer> lock, Integer item) {
            return false;
          }

          @Override
          public void release(Lock<Integer> lock, Integer item) {}
        };
    Lock<Integer> sut = new Lock<>(reservation, value::incrementAndGet);
    sut.acquire();

    // WHEN
    sut.acquire();

    // THEN
    assertThat(value).hasValue(1);
  }

  @Test
  void shouldReselectIfPreviousValueFailsCriteria() {
    AtomicInteger value = new AtomicInteger();
    Reservation<Integer> reservation =
        new Reservation<Integer>() {
          @Override
          public boolean reserve(Lock<Integer> lock, Integer item) {
            return true;
          }

          @Override
          public void release(Lock<Integer> lock, Integer item) {}
        };
    Lock<Integer> sut = new Lock<>(reservation, value::incrementAndGet);
    sut.setCriteria(val -> val == 2);
    sut.acquire();

    // WHEN
    boolean secondAcquire = sut.acquire();

    // THEN
    assertThat(value).hasValue(2);
    assertThat(secondAcquire).isTrue();
  }
}

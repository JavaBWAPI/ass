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
          public boolean reserve(Object lock, String item) {
            return true;
          }

          @Override
          public void release(Object lock, String item) {
          }
        };
    Lock<String> sut = new Lock<>(reservation);
    sut.setItem(STRING_ITEM);

    // WHEN
    boolean acquire = sut.tryLock();

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
          public boolean reserve(Object lock, String item) {
            return true;
          }

          @Override
          public void release(Object lock, String item) {
            released.set(true);
          }
        };
    Lock<String> sut = new Lock<>(reservation);
    sut.setItem("Hello World");
    sut.tryLock();

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
          public boolean reserve(Object lock, String item) {
            return false;
          }

          @Override
          public void release(Object lock, String item) {
          }
        };
    Lock<String> sut = new Lock<>(reservation);
    sut.setItem(STRING_ITEM);

    // WHEN
    boolean satisfied = sut.tryLock();

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
          public boolean reserve(Object lock, String item) {
            return true;
          }

          @Override
          public void release(Object lock, String item) {
          }
        };
    Lock<String> sut = new Lock<>(reservation);
    sut.setItem("Hello World");
    sut.tryLock();

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
          public boolean reserve(Object lock, String item) {
            reserveCalls.incrementAndGet();
            return false;
          }

          @Override
          public void release(Object lock, String item) {
          }
        };
    Lock<String> sut = new Lock<>(reservation);
    sut.setItem("Hello World");
    sut.tryLock();
    reserveCalls.set(0);

    // WHEN
    sut.tryLock();

    // THEN
    assertThat(reserveCalls).hasValue(1);
  }

  @Test
  public void shouldNotBeSatisfiedIfItemIsNull() {
    // GIVEN
    Reservation<String> reservation =
        new Reservation<String>() {
          @Override
          public boolean reserve(Object lock, String item) {
            return true;
          }

          @Override
          public boolean itemReservableInFuture(Object lock, String item, int futureFrames) {
            return true;
          }

          @Override
          public void release(Object lock, String item) {
          }
        };
    Lock<String> sut = new Lock<>(reservation);

    // WHEN
    boolean satisfied = sut.tryLock();

    // THEN
    assertThat(satisfied).isFalse();
    assertThat(sut.isSatisfied()).isFalse();
    assertThat(sut.isSatisfiedLater()).isFalse();
  }

  @Test
  public void shouldAddHysteresis() {
    // GIVEN
    AtomicInteger frames = new AtomicInteger();
    Reservation<String> reservation =
        new Reservation<String>() {
          @Override
          public boolean reserve(Object lock, String item) {
            return false;
          }

          @Override
          public boolean itemReservableInFuture(Object lock, String item, int futureFrames) {
            frames.set(futureFrames);
            return true;
          }

          @Override
          public void release(Object lock, String item) {
          }
        };
    Lock<String> sut = new Lock<>(reservation);
    sut.setItem(STRING_ITEM);
    sut.tryLock();

    // WHEN
    sut.tryLock();

    // THEN
    assertThat(frames).hasValue(Lock.DEFAULT_HYSTERESIS);
  }

  @Test
  void shouldNotSelectIfPreviousItemIsOk() {
    AtomicInteger value = new AtomicInteger();
    Reservation<Integer> reservation =
        new Reservation<Integer>() {
          @Override
          public boolean reserve(Object lock, Integer item) {
            return false;
          }

          @Override
          public void release(Object lock, Integer item) {
          }
        };
    Lock<Integer> sut = new Lock<>(reservation);
    sut.setItem(value.incrementAndGet());
    sut.tryLock();

    // WHEN
    sut.tryLock();

    // THEN
    assertThat(value).hasValue(1);
  }
}

package org.bk.ass.manage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ListLockTest {

  @Test
  void shouldPartiallyReleaseItems() {
    // GIVEN
    Reservation<List<String>> reservation =
        new Reservation<List<String>>() {
          @Override
          public boolean reserve(Object lock, List<String> item) {
            return true;
          }

          @Override
          public void release(Object lock, List<String> item) {
          }
        };
    ListLock<String> sut = new ListLock<>(reservation);
    sut.setItem(new ArrayList<>(Arrays.asList("a", "b")));
    sut.tryLock();

    // WHEN
    sut.releasePartially(Collections.singletonList("a"));

    // THEN
    assertThat(sut.getItem()).containsExactly("b");
  }

  @Test
  void shouldFailIfPartiallyReleasedItemsAreNotSelected() {
    // GIVEN
    Reservation<List<String>> reservation =
        new Reservation<List<String>>() {
          @Override
          public boolean reserve(Object lock, List<String> item) {
            return true;
          }

          @Override
          public void release(Object lock, List<String> item) {
          }
        };
    ListLock<String> sut = new ListLock<>(reservation);
    sut.setItem(Collections.emptyList());
    sut.tryLock();

    assertThatThrownBy(
        () -> {
          // WHEN
          sut.releasePartially(Collections.singletonList("a"));
        }) // THEN
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void shouldUpdateLockSatisfactionOnPartialRelease() {
    // GIVEN
    Reservation<List<String>> reservation =
        new Reservation<List<String>>() {
          @Override
          public boolean reserve(Object lock, List<String> item) {
            return true;
          }

          @Override
          public void release(Object lock, List<String> item) {
          }
        };
    ListLock<String> sut = new ListLock<>(reservation);
    sut.setCriteria(x -> x.contains("a"));
    sut.setItem(new ArrayList<>(Arrays.asList("a", "b")));
    sut.tryLock();

    // WHEN
    boolean stillSatisfied = sut.releasePartially(Collections.singletonList("a"));

    // THEN
    assertThat(stillSatisfied).isFalse();
    assertThat(sut.isSatisfied()).isFalse();
    assertThat(sut.isSatisfiedLater()).isFalse();
  }
}

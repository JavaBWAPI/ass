package org.bk.ass.grid;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class BooleanGridTest {

  @Test
  void shouldDiscardValueOnVersionUpdate() {
    // GIVEN
    BooleanGrid sut = new BooleanGrid(10, 10);
    sut.set(1, 1, true);

    // WHEN
    sut.updateVersion();

    // THEN
    assertThat(sut.get(1, 1)).isFalse();
  }

  @Test
  void shouldSetValue() {
    // GIVEN
    BooleanGrid sut = new BooleanGrid(10, 10);

    // WHEN
    sut.set(1, 1, true);

    // THEN
    assertThat(sut.get(1, 1)).isTrue();
  }
  
  @Test
  void shouldUnsetValue() {
    // GIVEN
    BooleanGrid sut = new BooleanGrid(10, 10);
    sut.set(1, 1, true);

    // WHEN
    sut.set(1, 1, false);

    // THEN
    assertThat(sut.get(1, 1)).isFalse();
  }
}
package org.bk.ass.manage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GMSTest {
  @Test
  void shouldAlwaysMeasureZeroGMSAsLower() {
    // GIVEN
    GMS sut = new GMS(-1, -1, -1);

    // WHEN
    boolean greaterOrEqual = sut.canAfford(GMS.ZERO);

    // THEN
    assertThat(greaterOrEqual).isTrue();
  }

  @Test
  void shouldNotBeGreaterOrEqualForMissingMinerals() {
    // GIVEN
    GMS sut = new GMS(1, 1, 1);

    // WHEN
    boolean greaterOrEqual = sut.canAfford(new GMS(0, 2, 0));

    // THEN
    assertThat(greaterOrEqual).isFalse();
  }

  @Test
  void shouldNotBeGreaterOrEqualForMissingGas() {
    // GIVEN
    GMS sut = new GMS(1, 1, 1);

    // WHEN
    boolean greaterOrEqual = sut.canAfford(new GMS(2, 0, 0));

    // THEN
    assertThat(greaterOrEqual).isFalse();
  }

  @Test
  void shouldNotBeGreaterOrEqualForMissingSupplies() {
    // GIVEN
    GMS sut = new GMS(1, 1, 1);

    // WHEN
    boolean greaterOrEqual = sut.canAfford(new GMS(0, 0, 2));

    // THEN
    assertThat(greaterOrEqual).isFalse();
  }
}
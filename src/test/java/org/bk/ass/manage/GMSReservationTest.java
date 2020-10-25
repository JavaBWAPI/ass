package org.bk.ass.manage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class GMSReservationTest {

  @Test
  void shouldReserveSuccessfullyIfAffordable() {
    // GIVEN
    GMSReservation sut = new GMSReservation();
    sut.setGms(new GMS(1, -1, 0));

    // WHEN
    boolean reserved = sut.reserve(null, new GMS(1, 0, 0));

    // THEN
    assertThat(reserved).isTrue();
    assertThat(sut.getGms()).isEqualTo(new GMS(0, -1, 0));
  }

  @Test
  void shouldBlockResourcesIfNotAffordable() {
    // GIVEN
    GMSReservation sut = new GMSReservation();
    sut.setGms(new GMS(-1, -1, 0));

    // WHEN
    boolean reserved = sut.reserve(null, new GMS(1, 1, 0));

    // THEN
    assertThat(reserved).isFalse();
    assertThat(sut.getGms()).isEqualTo(new GMS(-2, -2, 0));
  }

  @Test
  void shouldBeAvailableInFuture() {
    // GIVEN
    GMSReservation sut = new GMSReservation(frames -> new GMS(1, 1, 1).multiply(frames));
    GMS initialGMS = new GMS(-10, -10, -10);
    sut.setGms(initialGMS);

    // WHEN
    boolean reserved = sut.itemReservableInFuture(null, new GMS(1, 1, 1), 11);

    // THEN
    assertThat(reserved).isTrue();
    assertThat(sut.getGms()).isEqualTo(initialGMS);
  }
}
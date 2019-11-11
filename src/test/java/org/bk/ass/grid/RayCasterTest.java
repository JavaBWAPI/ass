package org.bk.ass.grid;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import org.bk.ass.grid.RayCaster.Hit;
import org.bk.ass.path.Position;
import org.junit.jupiter.api.Test;

class RayCasterTest {

  private static final boolean[][] BOTTOM_RIGHT =
      new boolean[][] {
        {false, false, false},
        {false, false, false},
        {false, false, true},
      };

  private static final boolean[][] TOP_LEFT =
      new boolean[][] {
        {true, false, false},
        {false, false, false},
        {false, false, false},
      };

  private static final boolean[][] MID_MID =
      new boolean[][] {
        {false, false, false},
        {false, true, false},
        {false, false, false},
      };

  private static final boolean[][] TL_TO_MID =
      new boolean[][] {
        {true, false, false},
        {false, true, false},
        {false, false, false},
      };

  @Test
  void shouldHitDiagonal() {
    // GIVEN
    RayCaster<Boolean> sut = new RayCaster<>(Grids.fromBooleanArray(BOTTOM_RIGHT));

    // WHEN
    Hit hit = sut.trace(0, 0, 2, 2);

    // THEN
    assertThat(hit).extracting(h -> new Position(h.x, h.y)).isEqualTo(new Position(2, 2));
  }

  @Test
  void shouldHitDiagonalReverse() {
    // GIVEN
    RayCaster<Boolean> sut = new RayCaster<>(Grids.fromBooleanArray(TOP_LEFT));

    // WHEN
    Hit hit = sut.trace(2, 2, 0, 0);

    // THEN
    assertThat(hit).extracting(h -> new Position(h.x, h.y)).isEqualTo(new Position(0, 0));
  }

  @Test
  void shouldMissVerticalIfNoHit() {
    // GIVEN
    RayCaster<Boolean> sut = new RayCaster<>(Grids.fromBooleanArray(MID_MID));

    // WHEN
    Hit hit = sut.trace(0, 0, 0, 2);

    // THEN
    assertThat(hit).isNull();
  }

  @Test
  void shouldFindHorizontalIfHit() {
    // GIVEN
    RayCaster<Boolean> sut = new RayCaster<>(Grids.fromBooleanArray(MID_MID));

    // WHEN
    Hit hit = sut.trace(0, 1, 2, 1);

    // THEN
    assertThat(hit).extracting(h -> new Position(h.x, h.y)).isEqualTo(new Position(1, 1));
  }

  @Test
  void shouldFindAllDiagHits() {
    // GIVEN
    RayCaster<Boolean> sut = new RayCaster<>(Grids.fromBooleanArray(TL_TO_MID));
    List<Hit> capturedHits = new ArrayList<>();

    // WHEN
    Hit hit = sut.trace(0, 0, 2, 2, x -> !capturedHits.add(x));

    // THEN
    assertThat(hit).isNull();
    assertThat(capturedHits)
        .extracting(h -> new Position(h.x, h.y))
        .containsExactly(new Position(0, 0), new Position(1, 1));
  }
}

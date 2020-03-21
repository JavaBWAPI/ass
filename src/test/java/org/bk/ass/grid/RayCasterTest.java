package org.bk.ass.grid;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayList;
import java.util.List;
import org.bk.ass.grid.RayCaster.Hit;
import org.bk.ass.grid.SearchPredicate.Result;
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

  private static final boolean[][] TR_TO_BL =
      new boolean[][] {
        {false, false, true},
        {false, true, false},
        {true, false, false},
      };

  private static final boolean[][] TR_AND_BR =
      new boolean[][] {
        {false, false, false},
        {false, false, false},
        {true, false, true},
      };

  @Test
  void shouldHitDiagonal() {
    // GIVEN
    RayCaster<Boolean> sut = new RayCaster<>(Grids.fromBooleanArray(BOTTOM_RIGHT));

    // WHEN
    Hit hit = sut.trace(0, 0, 2, 2, sut.findFirst);

    // THEN
    assertThat(hit).extracting(h -> new Position(h.x, h.y)).isEqualTo(new Position(2, 2));
  }

  @Test
  void shouldHitDiagonalReverse() {
    // GIVEN
    RayCaster<Boolean> sut = new RayCaster<>(Grids.fromBooleanArray(TOP_LEFT));

    // WHEN
    Hit hit = sut.trace(2, 2, 0, 0, sut.findFirst);

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
    Hit hit = sut.trace(0, 1, 2, 1, sut.findFirst);

    // THEN
    assertThat(hit).extracting(h -> new Position(h.x, h.y)).isEqualTo(new Position(1, 1));
  }

  @Test
  void shouldFindAllDiagHits() {
    // GIVEN
    RayCaster<Boolean> sut = new RayCaster<>(Grids.fromBooleanArray(TL_TO_MID));
    List<Hit> capturedHits = new ArrayList<>();

    // WHEN
    Hit hit =
        sut.trace(
            0,
            0,
            2,
            2,
            x -> {
              if (x.value) {
                capturedHits.add(x);
                return Result.CONTINUE;
              }
              return Result.STOP;
            });

    // THEN
    assertThat(hit).isNull();
    assertThat(capturedHits)
        .extracting(h -> new Position(h.x, h.y))
        .containsExactly(new Position(0, 0), new Position(1, 1));
  }

  @Test
  void shouldAcceptAndContinue() {
    // GIVEN
    RayCaster<Boolean> sut = new RayCaster<>(Grids.fromBooleanArray(TR_TO_BL));

    // WHEN
    Hit hit =
        sut.trace(
            2,
            0,
            0,
            2,
            i -> {
              if (i.x == 2) {
                return Result.ACCEPT_CONTINUE;
              }
              if (i.x == 1) {
                return Result.STOP;
              }
              fail("Should not have continued");
              return null;
            });

    // THEN
    assertThat(hit).extracting(h -> new Position(h.x, h.y)).isEqualTo(new Position(2, 0));
  }

  @Test
  void shouldReturnLastAcceptContinue() {
    // GIVEN
    RayCaster<Boolean> sut = new RayCaster<>(Grids.fromBooleanArray(TR_TO_BL));

    // WHEN
    Hit hit =
        sut.trace(
            2,
            0,
            0,
            2,
            i -> {
              if (i.x == 2) {
                return Result.ACCEPT_CONTINUE;
              }
              if (i.x == 1) {
                return Result.ACCEPT_CONTINUE;
              }
              return Result.STOP;
            });

    // THEN
    assertThat(hit).extracting(h -> new Position(h.x, h.y)).isEqualTo(new Position(1, 1));
  }

  @Test
  void shouldReturnAccepted() {
    // GIVEN
    RayCaster<Boolean> sut = new RayCaster<>(Grids.fromBooleanArray(TR_TO_BL));

    // WHEN
    Hit hit =
        sut.trace(
            2,
            0,
            0,
            2,
            i -> {
              if (i.x == 2) {
                return Result.CONTINUE;
              }
              if (i.x == 1) {
                return Result.ACCEPT;
              }
              fail("Should not have continued");
              return null;
            });

    // THEN
    assertThat(hit).extracting(h -> new Position(h.x, h.y)).isEqualTo(new Position(1, 1));
  }

  @Test
  void shouldOnlyReturnFirstHit() {
    // GIVEN
    RayCaster<Boolean> sut = new RayCaster<>(Grids.fromBooleanArray(TR_AND_BR));

    // WHEN
    Hit hit = sut.trace(2, 0, 2, 2);

    // THEN
    assertThat(hit).extracting(h -> new Position(h.x, h.y)).isEqualTo(new Position(2, 0));
  }
}

package org.bk.ass.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Function;
import org.junit.jupiter.api.Test;

class UnitFinderTest {

  @Test
  public void shouldNotDieIfEmpty() {
    // GIVEN
    UnitFinder<Object> finder =
        new UnitFinder<>(
            Collections.emptyList(),
            o -> {
              throw new IllegalStateException("On what object are you calling me?");
            });

    // WHEN
    finder.closestTo(0, 0);
    finder.inArea(0, 0, 10, 10);
    finder.inRadius(30, 30, 100);

    // THEN
    // Should not throw any exception
  }

  @Test
  public void shouldFindClosest() {
    // GIVEN
    UnitFinder<PositionAndId> finder =
        new UnitFinder<>(
            Arrays.asList(new PositionAndId(-1, -30, 0), new PositionAndId(-1, 0, -31)),
            Function.identity());

    // WHEN
    PositionAndId closest = finder.closestTo(0, 0).get();

    // THEN
    assertThat(closest).isEqualByComparingTo(new PositionAndId(-1, -30, 0));
  }

  @Test
  public void shouldFindAllInArea() {
    // GIVEN
    UnitFinder<PositionAndId> finder =
        new UnitFinder<>(
            Arrays.asList(
                new PositionAndId(-1, 0, 0),
                new PositionAndId(-1, 0, 10),
                new PositionAndId(-1, 10, 0),
                new PositionAndId(-1, 10, 10),
                new PositionAndId(-1, 0, 11),
                new PositionAndId(-1, 11, 0),
                new PositionAndId(-1, 11, 11)),
            Function.identity());

    // WHEN
    Collection<PositionAndId> result = finder.inArea(0, 0, 10, 10);

    // THEN
    assertThat(result).hasSize(4);
  }

  @Test
  public void shouldFindAllInRadius() {
    // GIVEN
    UnitFinder<PositionAndId> finder =
        new UnitFinder<>(
            Arrays.asList(
                new PositionAndId(-1, 0, 0),
                new PositionAndId(-1, 0, 10),
                new PositionAndId(-1, 10, 0),
                new PositionAndId(-1, 10, 10),
                new PositionAndId(-1, -10, 0),
                new PositionAndId(-1, 0, -10)),
            Function.identity());

    // WHEN
    Collection<PositionAndId> result = finder.inRadius(0, 0, 10);

    // THEN
    assertThat(result).hasSize(5);
  }
}

package org.bk.ass.query;

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;

class UnitFinderTest {

  @Test
  void shouldNotDieIfEmpty() {
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
  void shouldFindClosest() {
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
  void shouldFindClosestWithCriteria() {
    // GIVEN
    UnitFinder<Unit> finder =
            new UnitFinder<>(
                    Arrays.asList(
                            new Unit(-1, new PositionAndId(-1, -30, 0)),
                            new Unit(0, new PositionAndId(0, 0, -30)),
                            new Unit(1, new PositionAndId(1, 30, 0))),
                    u -> u.positionAndId);

    // WHEN
    Unit closest = finder.closestTo(0, 0, u -> u.id > 0).get();

    // THEN
    assertThat(closest).isEqualTo(new Unit(1, new PositionAndId(1, 30, 0)));
  }

  @Test
  void shouldFindDifferentItemIfCriteriaFails() {
    // GIVEN
    UnitFinder<Unit> finder =
            new UnitFinder<>(
                    Arrays.asList(
                            new Unit(-1, new PositionAndId(-1, -30, 0)),
                            new Unit(0, new PositionAndId(0, 0, -30)),
                            new Unit(1, new PositionAndId(1, 30, 0))),
                    u -> u.positionAndId);

    // WHEN
    Unit closest = finder.closestTo(30, 0, u -> u.id < 0).get();

    // THEN
    assertThat(closest).isEqualTo(new Unit(-1, new PositionAndId(-1, -30, 0)));
  }

  @Test
  void shouldFindAllInArea() {
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
  void shouldFindAllInRadius() {
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

  @Test
  void shouldFindAllInRadiusOfUnit() {
    // GIVEN
    PositionAndId unit = new PositionAndId(-1, 0, 0);
    UnitFinder<PositionAndId> finder =
        new UnitFinder<>(
            Arrays.asList(
                unit,
                new PositionAndId(-1, 0, 10),
                new PositionAndId(-1, 10, 0),
                new PositionAndId(-1, 10, 10),
                new PositionAndId(-1, -10, 0),
                new PositionAndId(-1, 0, -10)),
            Function.identity());

    // WHEN
    Collection<PositionAndId> result = finder.inRadius(unit, 10);

    // THEN
    assertThat(result).hasSize(5);
  }

  private static class Unit {
    public final int id;
    public final PositionAndId positionAndId;

    private Unit(int id, PositionAndId positionAndId) {
      this.id = id;
      this.positionAndId = positionAndId;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Unit unit = (Unit) o;
      return id == unit.id && Objects.equals(positionAndId, unit.positionAndId);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, positionAndId);
    }

    @Override
    public String toString() {
      return "Unit{" +
              "id=" + id +
              ", positionAndId=" + positionAndId +
              '}';
    }
  }
}

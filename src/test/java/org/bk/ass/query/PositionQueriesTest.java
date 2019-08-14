package org.bk.ass.query;

import org.bk.ass.path.Position;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.bk.ass.query.Distances.EUCLIDEAN_DISTANCE;

class PositionQueriesTest {
  private static List<Position> positions;

  @BeforeAll
  static void setup() {
    SplittableRandom rnd = new SplittableRandom(1);
    positions = new ArrayList<>();
    for (int i = 0; i < 100000; i++) {
      positions.add(new Position(rnd.nextInt(10000), rnd.nextInt(10000)));
    }
  }

  @Test
  void shouldFindNearest() {
    for (int i = 3; i < positions.size(); i = i * 14 / 10) {
      // GIVEN
      List<Position> testList = PositionQueriesTest.positions.subList(0, i);
      PositionQueries<Position> tree = new PositionQueries<>(testList, Function.identity());

      // WHEN
      Position nearest = tree.nearest(500, 500);

      // THEN
      Position actualNearest =
          testList.stream()
              .min(Comparator.comparingInt(a -> EUCLIDEAN_DISTANCE.distance(a.x, a.y, 500, 500)))
              .orElseThrow(RuntimeException::new);
      assertThat(nearest).isEqualTo(actualNearest);
    }
  }

  @Test
  void shouldPivotMatchingToOneSide() {
    // GIVEN
    List<Position> positions =
        IntStream.range(0, 30).mapToObj(it -> new Position(0, it)).collect(Collectors.toList());
    PositionQueries<Position> tree = new PositionQueries<>(positions, Function.identity());

    // WHEN
    Collection<Position> result = tree.inArea(0, 0, 0, 150);

    // THEN
    assertThat(result).containsExactlyInAnyOrderElementsOf(positions);
  }

  @Test
  void shouldFindInArea() {
    // GIVEN
    PositionQueries<Position> tree = new PositionQueries<>(positions, Function.identity());

    // WHEN
    Collection<Position> nearest = tree.inArea(10, 10, 1000, 1000);

    // THEN
    List<Position> actualNearest =
        positions.stream()
            .filter(it -> it.x >= 10 && it.y >= 10 && it.x <= 1000 && it.y <= 1000)
            .collect(Collectors.toList());

    assertThat(nearest).containsExactlyInAnyOrderElementsOf(actualNearest);
  }

  @Test
  void shouldFindInRadius() {
    // GIVEN
    PositionQueries<Position> tree = new PositionQueries<>(positions, Function.identity());

    // WHEN
    Collection<Position> nearest = tree.inRadius(5000, 5000, 1000);

    // THEN
    List<Position> actualNearest =
        positions.stream()
            .filter(it -> EUCLIDEAN_DISTANCE.distance(5000, 5000, it.x, it.y) <= 1000)
            .collect(Collectors.toList());

    assertThat(nearest).containsExactlyInAnyOrderElementsOf(actualNearest);
  }

  @Test
  void shouldFindAllInArea() {
    // GIVEN
    PositionQueries<Position> kdTree =
        new PositionQueries<>(
            Arrays.asList(
                new Position(0, 0),
                new Position(0, 10),
                new Position(10, 0),
                new Position(10, 10),
                new Position(0, 11),
                new Position(11, 0),
                new Position(11, 11)),
            Function.identity());

    // WHEN
    Collection<Position> result = kdTree.inArea(0, 0, 10, 10);

    // THEN
    assertThat(result).hasSize(4);
  }

  @Test
  void shouldNotDieIfEmpty() {
    // GIVEN
    PositionQueries<Object> finder =
        new PositionQueries<>(
            emptyList(),
            o -> {
              throw new IllegalStateException("On what object are you calling me?");
            });

    // WHEN
    finder.nearest(0, 0);
    finder.inArea(0, 0, 10, 10);
    finder.inRadius(30, 30, 100);

    // THEN
    // Should not throw any exception
  }

  @Test
  void shouldFindClosest() {
    // GIVEN
    PositionQueries<Position> finder =
        new PositionQueries<>(
            Arrays.asList(new Position(-30, 0), new Position(0, -31)), Function.identity());

    // WHEN
    Position closest = finder.nearest(0, 0);

    // THEN
    assertThat(closest).isEqualTo(new Position(-30, 0));
  }

  @Test
  void shouldFindClosestWithCriteria() {
    // GIVEN
    PositionQueries<Unit> finder =
        new PositionQueries<>(
            Arrays.asList(
                new Unit(-1, new Position(-30, 0)),
                new Unit(0, new Position(0, -30)),
                new Unit(1, new Position(30, 0))),
            u -> u.Position);

    // WHEN
    Unit closest = finder.nearest(0, 0, u -> u.id > 0);

    // THEN
    assertThat(closest).isEqualTo(new Unit(1, new Position(30, 0)));
  }

  @Test
  void shouldFindDifferentItemIfCriteriaFails() {
    // GIVEN
    PositionQueries<Unit> finder =
        new PositionQueries<>(
            Arrays.asList(
                new Unit(-1, new Position(-30, 0)),
                new Unit(0, new Position(0, -30)),
                new Unit(1, new Position(30, 0))),
            u -> u.Position);

    // WHEN
    Unit closest = finder.nearest(30, 0, u -> u.id < 0);

    // THEN
    assertThat(closest).isEqualTo(new Unit(-1, new Position(-30, 0)));
  }

  @Test
  void shouldFindAllInRadius() {
    // GIVEN
    PositionQueries<Position> finder =
        new PositionQueries<>(
            Arrays.asList(
                new Position(0, 0),
                new Position(0, 10),
                new Position(10, 0),
                new Position(10, 10),
                new Position(-10, 0),
                new Position(0, -10)),
            Function.identity());

    // WHEN
    Collection<Position> result = finder.inRadius(0, 0, 10);

    // THEN
    assertThat(result).hasSize(5);
  }

  @Test
  void shouldFindAllInRadiusOfUnit() {
    // GIVEN
    Position unit = new Position(0, 0);
    PositionQueries<Position> finder =
        new PositionQueries<>(
            Arrays.asList(
                unit,
                new Position(0, 10),
                new Position(10, 0),
                new Position(10, 10),
                new Position(-10, 0),
                new Position(0, -10)),
            Function.identity());

    // WHEN
    Collection<Position> result = finder.inRadius(unit, 10);

    // THEN
    assertThat(result).hasSize(5);
  }

  @Test
  void shouldAddItem() {
    // GIVEN
    PositionQueries<Position> queries = new PositionQueries<>(emptyList(), Function.identity());

    // WHEN
    queries.add(new Position(0, 0));

    // THEN
    assertThat(queries).containsExactly(new Position(0, 0));
  }

  @Test
  void shouldRemoveItem() {
    // GIVEN
    PositionQueries<Position> finder =
        new PositionQueries<>(
            Arrays.asList(
                new Position(0, 0),
                new Position(0, 10),
                new Position(10, 0),
                new Position(10, 10),
                new Position(-10, 0),
                new Position(0, -10)),
            Function.identity());

    // WHEN
    boolean removed = finder.remove(new Position(10, 0));

    // THEN
    assertThat(removed).isTrue();
  }

  @Test
  void shouldRemoveLastItem() {
    // GIVEN
    PositionQueries<Position> finder =
        new PositionQueries<>(Collections.singletonList(new Position(0, 0)), Function.identity());

    // WHEN
    boolean removed = finder.remove(new Position(0, 0));

    // THEN
    assertThat(removed).isTrue();
  }

  @Test
  void shouldRemoveAllItems() {
    // GIVEN
    PositionQueries<Position> finder =
        new PositionQueries<>(
            Arrays.asList(
                new Position(0, 0),
                new Position(0, 10),
                new Position(10, 0),
                new Position(10, 10),
                new Position(-10, 0),
                new Position(0, -10)),
            Function.identity());

    // WHEN
    boolean removed =
        finder.removeAll(
            Arrays.asList(new Position(10, 0), new Position(0, 10), new Position(222, 222)));

    // THEN
    assertThat(removed).isTrue();
    assertThat(finder).hasSize(4);
  }

  private static class Unit {
    final int id;
    final Position Position;

    private Unit(int id, Position Position) {
      this.id = id;
      this.Position = Position;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;
      Unit unit = (Unit) o;
      return id == unit.id && Objects.equals(Position, unit.Position);
    }

    @Override
    public int hashCode() {
      return Objects.hash(id, Position);
    }

    @Override
    public String toString() {
      return "Unit{" + "id=" + id + ", Position=" + Position + '}';
    }
  }
}

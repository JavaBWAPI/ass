package org.bk.ass.query;

import org.bk.ass.path.Position;
import org.junit.jupiter.api.Test;

import java.util.*;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bk.ass.query.Distances.EUCLIDEAN_DISTANCE;

class PositionQueriesTest {
    @Test
    public void shouldFindNearest() {
        // GIVEN
        SplittableRandom rnd = new SplittableRandom(1);
        Collection<Position> c = new ArrayList<>();
        for (int i = 0; i < 100000; i++) {
            c.add(new Position(rnd.nextInt(10000), rnd.nextInt(10000)));
        }
        PositionQueries<Position> tree = new PositionQueries<>(c, Function.identity());

        // WHEN
        Position nearest = tree.nearest(500, 500);

        // THEN
        Position actualNearest =
                c.stream()
                        .min(Comparator.comparingInt(a -> EUCLIDEAN_DISTANCE.distance(a.x, a.y, 500, 500)))
                        .get();
        assertThat(nearest).isEqualTo(actualNearest);
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
                        Collections.emptyList(),
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

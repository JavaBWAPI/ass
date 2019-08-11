package org.bk.ass.query;

import org.bk.ass.path.Position;
import org.openbw.bwapi4j.test.BWDataProvider;
import org.openjdk.jmh.annotations.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.SplittableRandom;
import java.util.function.Function;

@Measurement(iterations = 2, time = 2)
@Warmup(iterations = 2, time = 2)
@Fork(2)
public class PositionQueriesBenchmark {

    @State(Scope.Thread)
    public static class MyState {

        PositionQueries<Position> positionQueries;
        List<PositionAndId> entities;
        List<Position> items;

        @Setup
        public void setup() {
            SplittableRandom rnd = new SplittableRandom(815);
            entities = new ArrayList<>();
            items = new ArrayList<>();
            for (int i = 0; i < 1000; i++) {
                int x = rnd.nextInt(0, 10000);
                int y = rnd.nextInt(0, 10000);
                entities.add(new PositionAndId(i, x, y));
                items.add(new Position(x, y));
            }
            positionQueries = new PositionQueries<>(items, Function.identity());
        }
    }

    static {
        try {
            BWDataProvider.injectValues();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Benchmark
    public PositionQueries<Position> queryCreation(MyState state) {
        return new PositionQueries<>(state.items, Function.identity());
    }


    @Benchmark
    public Collection<Position> inRadius(MyState state) {
        return state.positionQueries.inRadius(5000, 5000, 1000);
    }

    @Benchmark
    public Position nearest(MyState state) {
        return state.positionQueries.nearest(2500, 2500);
    }
}

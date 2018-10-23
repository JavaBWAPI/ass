package org.bk.ass.query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.SplittableRandom;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.openbw.bwapi4j.test.BWDataProvider;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@Measurement(iterations = 3, time = 5)
@Fork(3)
public class UnitFinderBenchmark {

  @State(Scope.Thread)
  public static class MyState {

    UnitFinder unitFinder;
    List<PositionAndId> entities;

    @Setup
    public void setup() {
      SplittableRandom rnd = new SplittableRandom(815);
      entities = new ArrayList<>();
      for (int i = 0; i < 1000; i++) {
        entities.add(new PositionAndId(i, rnd.nextInt(0, 10000), rnd.nextInt(0, 10000)));
      }
      unitFinder = new UnitFinder(entities, Function.identity());
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
  public Collection inRadius(MyState state) {
    return state.unitFinder.inRadius(5000, 5000, 1000);
  }

  @Benchmark
  public Optional closestTo(MyState state) {
    return state.unitFinder.closestTo(2500, 2500);
  }

  @Benchmark
  public List<PositionAndId> inRadiusWithoutUnitFinder(MyState state) {
    return state
        .entities
        .stream()
        .filter(p -> Math.sqrt((p.x - 5000) * (p.x - 5000) + (p.y - 5000) * (p.y - 5000)) <= 1000)
        .collect(Collectors.toList());
  }
}

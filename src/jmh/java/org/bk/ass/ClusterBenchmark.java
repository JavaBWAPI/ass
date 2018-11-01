package org.bk.ass;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.bk.ass.cluster.Cluster;
import org.bk.ass.cluster.StableDBScanner;
import org.openbw.bwapi4j.test.BWDataProvider;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@Measurement(iterations = 5, time = 5)
@Fork(3)
public class ClusterBenchmark {

  @State(Scope.Thread)
  public static class MyState {

    StableDBScanner<Integer> scanner;

    @Setup(Level.Invocation)
    public void setup() {
      SplittableRandom rnd = new SplittableRandom(815);
      List<Integer> db = IntStream.range(0, 200).boxed().collect(Collectors.toList());
      Map<Integer, List<Integer>> radius = new HashMap<>();
      for (int i = 0; i < 200; i++) {
        List<Integer> inRadius =
            IntStream.range(0, rnd.nextInt(7))
                .mapToObj(unused -> rnd.nextInt(200))
                .collect(Collectors.toList());
        inRadius.add(i);
        radius.put(i, inRadius);
      }

      scanner = new StableDBScanner<>(db, 3, radius::get);
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
  public Collection<Cluster<Integer>> cluster(MyState state) {
    return state.scanner.scan(-1).getClusters();
  }
}

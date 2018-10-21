package org.bk.ass;

import org.openbw.bwapi4j.test.BWDataProvider;
import org.openbw.bwapi4j.type.UnitType;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

public class SimulatorBenchmark {

  @State(Scope.Thread)
  public static class MyState {

    Simulator simulator;
    BWAPI4JAgentFactory factory = new BWAPI4JAgentFactory(null);

    @Setup(Level.Invocation)
    public void setup() {
      simulator = new Simulator();

      for (int i = 0; i < 7; i++) {
        simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk));
      }
      for (int i = 0; i < 8; i++) {
        simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk));
      }
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
  public int _7MutasVs8Hydras(MyState state) {
    return state.simulator.simulate(-1);
  }
}

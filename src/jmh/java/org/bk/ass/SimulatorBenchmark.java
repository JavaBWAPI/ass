package org.bk.ass;

import org.openbw.bwapi4j.test.BWDataProvider;
import org.openbw.bwapi4j.type.UnitType;
import org.openjdk.jmh.annotations.*;

@Measurement(iterations = 5, time = 5)
@Fork(3)
public class SimulatorBenchmark {

  @State(Scope.Thread)
  public static class MyState {

    Simulator simulator;
    BWAPI4JAgentFactory factory = new BWAPI4JAgentFactory(null);

    @Setup(Level.Invocation)
    public void setup() {
      simulator = new Simulator();

      for (int i = 0; i < 30; i++) {
        simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk));
      }
      for (int i = 0; i < 30; i++) {
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
  public int _30MutasVs30Hydras(MyState state) {
    return state.simulator.simulate(-1);
  }

  @Benchmark
  public int clearCollisionMaps(MyState state) {
    state.simulator.reset();
    return state.simulator.getAgentsA().size() + state.simulator.getAgentsB().size();
  }
}

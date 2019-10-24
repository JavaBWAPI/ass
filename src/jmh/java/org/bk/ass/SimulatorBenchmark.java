package org.bk.ass;

import org.bk.ass.sim.AgentUtil;
import org.bk.ass.sim.ApproxAttackBehavior;
import org.bk.ass.sim.BWAPI4JAgentFactory;
import org.bk.ass.sim.Simulator;
import org.bk.ass.sim.Simulator.Builder;
import org.openbw.bwapi4j.test.BWDataProvider;
import org.openbw.bwapi4j.type.UnitType;
import org.openjdk.jmh.annotations.*;

@Measurement(iterations = 5, time = 5)
@Fork(3)
public class SimulatorBenchmark {

  @State(Scope.Thread)
  public static class MyState {

    Simulator simulator;
    Simulator simulatorFS4;
    Simulator approxSim;
    BWAPI4JAgentFactory factory = new BWAPI4JAgentFactory(null);

    @Setup(Level.Invocation)
    public void setup() {
      simulator = new Builder().build();
      simulatorFS4 = new Builder().withFrameSkip(4).build();
      approxSim = new Builder().withFrameSkip(37).withPlayerABehavior(new ApproxAttackBehavior()).withPlayerABehavior(new ApproxAttackBehavior()).build();

      for (int i = 0; i < 30; i++) {
        simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk));
        simulatorFS4.addAgentA(factory.of(UnitType.Zerg_Mutalisk));
        approxSim.addAgentA(factory.of(UnitType.Zerg_Mutalisk));
      }
      for (int i = 0; i < 30; i++) {
        simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk));
        simulatorFS4.addAgentB(factory.of(UnitType.Zerg_Hydralisk));
        approxSim.addAgentB(factory.of(UnitType.Zerg_Hydralisk));
      }

      AgentUtil.randomizePositions(approxSim.getAgentsA(), 0, 0, 32, 32);
      AgentUtil.randomizePositions(approxSim.getAgentsB(), 32, 0, 64, 64);
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
  public int _30MutasVs30Hydras_fs4(MyState state) {
    return state.simulatorFS4.simulate(-1);
  }

  @Benchmark
  public int _30MutasVs30Hydras_approx(MyState state) {
    return state.approxSim.simulate(-1);
  }

  @Benchmark
  public int clearCollisionMaps(MyState state) {
    state.simulator.reset();
    return state.simulator.getAgentsA().size() + state.simulator.getAgentsB().size();
  }
}

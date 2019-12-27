package org.bk.ass;

import bwapi.UnitType;
import java.util.ArrayList;
import java.util.List;
import org.bk.ass.sim.Agent;
import org.bk.ass.sim.Evaluator;
import org.bk.ass.sim.Evaluator.EvaluationResult;
import org.bk.ass.sim.JBWAPIAgentFactory;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@Measurement(iterations = 5, time = 5)
@Fork(3)
public class EvaluatorBenchmark {

  @State(Scope.Thread)
  public static class MyState {

    Evaluator evaluator = new Evaluator();
    JBWAPIAgentFactory factory = new JBWAPIAgentFactory(null);
    private List<Agent> agentsA = new ArrayList<>();
    private List<Agent> agentsB = new ArrayList<>();

    @Setup
    public void setup() {
      for (int i = 0; i < 7; i++) {
        agentsA.add(factory.of(UnitType.Zerg_Mutalisk));
      }
      for (int i = 0; i < 8; i++) {
        agentsB.add(factory.of(UnitType.Zerg_Hydralisk));
      }
    }
  }

  @Benchmark
  public EvaluationResult _7MutasVs8Hydras(MyState state) {
    return state.evaluator.evaluate(state.agentsA, state.agentsB);
  }
}

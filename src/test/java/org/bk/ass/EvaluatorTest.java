package org.bk.ass;

import static org.assertj.core.api.Assertions.assertThat;

import io.jenetics.Chromosome;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bk.ass.Evaluator.Parameters;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openbw.bwapi4j.test.BWDataProvider;
import org.openbw.bwapi4j.type.UnitType;

/**
 * The most interesting tests are those where the outcome is not a landslide. But of course some
 * tests in that department are also required to calibrate the {@link Evaluator}
 */
class EvaluatorTest {

  private Parameters parameters = new Parameters();
  private Evaluator evaluator = new Evaluator(parameters);
  private BWAPI4JAgentFactory factory = new BWAPI4JAgentFactory(null);

  @BeforeAll
  static void setup() throws Exception {
    BWDataProvider.injectValues();
  }

  @Test
  void noAgentsShouldNotResultInNaN() {
    // WHEN
    double result = evaluator.evaluate(Collections.emptyList(), Collections.emptyList());

    // THEN
    assertThat(result).isBetween(0.49, 0.51);
  }


  @Test
  void MMvsMM() {
    // GIVEN
    List<Agent> agentsA =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Medic, 0, 0),
            factory.of(UnitType.Terran_Medic, 0, 0));
    List<Agent> agentsB =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Medic, 0, 0),
            factory.of(UnitType.Terran_Medic, 0, 0));

    // WHEN
    double result = evaluator.evaluate(agentsA, agentsB);

    // THEN
    assertThat(result).isBetween(0.49, 0.51);
  }

  @Test
  void _7MutasVs8Hydras() {
    // GIVEN
    List<Agent> agentsA = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      agentsA.add(factory.of(UnitType.Zerg_Mutalisk, 0, 0));
    }
    List<Agent> agentsB = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      agentsB.add(factory.of(UnitType.Zerg_Hydralisk, 0, 0));
    }

    // WHEN
    double result = evaluator.evaluate(agentsA, agentsB);

    // THEN
    assertThat(result).isLessThan(0.48);
  }

  @Test
  void _10HydrasVsDT() {
    // GIVEN
    List<Agent> agentsA = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      agentsA.add(factory.of(UnitType.Zerg_Hydralisk, 0, 0));
    }
    List<Agent> agentsB =
        Collections.singletonList(
            factory.of(UnitType.Protoss_Dark_Templar, 0, 0).setDetected(false));

    // WHEN
    double result = evaluator.evaluate(agentsA, agentsB);

    // THEN
    assertThat(result).isLessThan(0.2);
  }

  @Test
  void _7MutasVs14Marines() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0));

    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0),
            factory.of(UnitType.Terran_Marine, 0, 0));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isLessThan(0.45);
  }

  @Test
  void _6MutasVs1Bunker() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Terran_Bunker, 0, 0));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isGreaterThan(0.51);
  }

  @Test
  void _5MutasVs1Bunker() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0),
            factory.of(UnitType.Zerg_Mutalisk, 0, 0));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Terran_Bunker, 0, 0));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isLessThan(0.5);
  }

  @Test
  void _1MutaVs1SCV() {
    // GIVEN
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Zerg_Mutalisk, 0, 0));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Terran_SCV, 0, 0));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isGreaterThan(0.9);
  }

  @Test
  void GoonVsTank() {
    // GIVEN
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Protoss_Dragoon, 0, 0));
    List<Agent> b =
        Collections.singletonList(factory.of(UnitType.Terran_Siege_Tank_Tank_Mode, 0, 0));

    // WHEN
    double result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isGreaterThan(0.51);
  }

  // Try to tune the parameter based on the test
  public static void main(String[] args) throws Exception {
    BWDataProvider.injectValues();

    Genotype<DoubleGene> genotype =
        Genotype.of(DoubleChromosome.of(0.0001, 3.0, 3), DoubleChromosome.of(0, 1000, 3));

    Function<Genotype<DoubleGene>, Integer> eval =
        gt -> {
          EvaluatorTest test = new EvaluatorTest();
          test.parameters.setFrom(
              gt.stream()
                  .flatMap(Chromosome::stream)
                  .mapToDouble(DoubleGene::doubleValue)
                  .map(EvaluatorTest::round)
                  .toArray());
          return hits(test);
        };

    Engine<DoubleGene, Integer> engine = Engine.builder(eval, genotype).maximizing().build();
    EvolutionResult<DoubleGene, Integer> result =
        engine
            .stream()
            .limit(Limits.<Integer>bySteadyFitness(20000).and(Limits.byFitnessThreshold(8)))
            .parallel()
            .collect(EvolutionResult.toBestEvolutionResult());
    System.out.println("Best result " + result.getBestFitness());
    Genotype<DoubleGene> bestGenotype = result.getBestPhenotype().getGenotype();
    System.out.println(
        bestGenotype
            .stream()
            .flatMap(Chromosome::stream)
            .mapToDouble(DoubleGene::doubleValue)
            .map(EvaluatorTest::round)
            .mapToObj(String::valueOf)
            .collect(Collectors.joining(", ", "new double[] {", "};")));
  }

  private static double round(double v) {
    return Math.round(v * 8000) / 8000.0;
  }

  private static int hits(EvaluatorTest test) {
    return (int)
        Arrays.stream(EvaluatorTest.class.getDeclaredMethods())
            .filter(
                m ->
                    m.getReturnType().equals(Void.TYPE)
                        && m.getParameterCount() == 0
                        && !Modifier.isStatic(m.getModifiers()))
            .filter(
                m -> {
                  try {
                    m.invoke(test);
                    return true;
                  } catch (Exception e) {
                    return false;
                  }
                })
            .count();
  }
}

package org.bk.ass.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bk.ass.sim.Evaluator.EVAL_NO_COMBAT;

import io.jenetics.util.IntRange;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import org.bk.ass.sim.Evaluator.EvalWithAgents;
import org.bk.ass.sim.Evaluator.EvaluationResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openbw.bwapi4j.test.BWDataProvider;
import org.openbw.bwapi4j.type.UnitType;

/**
 * The most interesting tests are those where the outcome is not a landslide. But of course some
 * tests in that department are also required to calibrate the {@link Evaluator}
 */
class EvaluatorTest {
  Evaluator evaluator = new Evaluator();
  private BWAPI4JAgentFactory factory = new BWAPI4JAgentFactory(null);

  @BeforeAll
  static void setup() throws Exception {
    BWDataProvider.injectValues();
  }

  @Test
  void noAgentsShouldNotResultInNaN() {
    // WHEN
    EvaluationResult result = evaluator.evaluate(Collections.emptyList(), Collections.emptyList());

    // THEN
    assertThat(result).isEqualTo(EVAL_NO_COMBAT);
  }

  @Test
  void MMvsMM() {
    // GIVEN
    List<Agent> agentsA =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Medic),
            factory.of(UnitType.Terran_Medic));
    List<Agent> agentsB =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Medic),
            factory.of(UnitType.Terran_Medic));

    // WHEN
    EvaluationResult result = evaluator.evaluate(agentsA, agentsB);

    // THEN
    assertThat(result.value).isBetween(0.49, 0.51);
    assertThat(result.value).isNotEqualTo(EVAL_NO_COMBAT);
  }

  @Test
  void _7MutasVs8Hydras() {
    // GIVEN
    List<Agent> agentsA = new ArrayList<>();
    for (int i = 0; i < 7; i++) {
      agentsA.add(factory.of(UnitType.Zerg_Mutalisk));
    }
    List<Agent> agentsB = new ArrayList<>();
    for (int i = 0; i < 8; i++) {
      agentsB.add(factory.of(UnitType.Zerg_Hydralisk));
    }

    // WHEN
    EvaluationResult result = evaluator.evaluate(agentsA, agentsB);

    // THEN
    assertThat(result.value).isBetween(0.4, 0.6);
  }

  @Test
  void _10HydrasVsDT() {
    // GIVEN
    List<Agent> agentsA = new ArrayList<>();
    for (int i = 0; i < 10; i++) {
      agentsA.add(factory.of(UnitType.Zerg_Hydralisk));
    }
    List<Agent> agentsB =
        Collections.singletonList(factory.of(UnitType.Protoss_Dark_Templar).setDetected(false));

    // WHEN
    EvaluationResult result = evaluator.evaluate(agentsA, agentsB);

    // THEN
    assertThat(result.value).isLessThan(0.2);
  }

  @Test
  void _7MutasVs14Marines() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk));

    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isLessThan(0.5);
  }

  @Test
  void _2LurkersVs6Marines() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(factory.of(UnitType.Zerg_Lurker), factory.of(UnitType.Zerg_Lurker));

    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isGreaterThan(0.6);
  }

  @Test
  void _2LurkersVs14Marines() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(factory.of(UnitType.Zerg_Lurker), factory.of(UnitType.Zerg_Lurker));

    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Terran_Marine));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isLessThan(0.3);
  }

  @Test
  void _6MutasVs1Bunker() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Terran_Bunker));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isGreaterThan(0.5);
  }

  @Test
  void _5MutasVs1Bunker() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Terran_Bunker));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isLessThan(0.5);
  }

  @Test
  void _1MutaVs1SCV() {
    // GIVEN
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Zerg_Mutalisk));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Terran_SCV));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isGreaterThan(0.9);
  }

  @Test
  void GoonVsTank() {
    // GIVEN
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Protoss_Dragoon));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Terran_Siege_Tank_Tank_Mode));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isGreaterThan(0.5);
  }

  @Test
  void _3ZerglingVsSiegedTankAndMarine() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling));
    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_Siege_Tank_Siege_Mode), factory.of(UnitType.Terran_Marine));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isBetween(0.2, 0.4);
  }

  @Test
  void _6ZerglingVsSiegedTankAndMarine() {
    // GIVEN
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling));
    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_Siege_Tank_Siege_Mode), factory.of(UnitType.Terran_Marine));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isBetween(0.5, 0.7);
  }

  @Test
  void reaverVs12Lings() {
    // GIVEN

    List<Agent> a = Collections.singletonList(factory.of(UnitType.Protoss_Reaver));

    List<Agent> b =
        IntRange.of(0, 11).stream()
            .mapToObj(unused -> factory.of(UnitType.Zerg_Zergling))
            .collect(Collectors.toList());

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isBetween(0.2, 0.45);
  }

  @Test
  void reaverVs9Lings() {
    // GIVEN

    List<Agent> a = Collections.singletonList(factory.of(UnitType.Protoss_Reaver));

    List<Agent> b =
        IntRange.of(0, 9).stream()
            .mapToObj(unused -> factory.of(UnitType.Zerg_Zergling))
            .collect(Collectors.toList());

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isBetween(0.5, 0.8);
  }

  @Test
  void MvsMM() {
    // GIVEN
    List<Agent> a =
        IntRange.of(0, 4).stream()
            .mapToObj(unused -> factory.of(UnitType.Terran_Marine))
            .collect(Collectors.toList());
    List<Agent> b =
        Stream.concat(
                IntRange.of(0, 3).stream().mapToObj(unused -> factory.of(UnitType.Terran_Marine)),
                IntRange.of(0, 1).stream().mapToObj(unused -> factory.of(UnitType.Terran_Medic)))
            .collect(Collectors.toList());

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isLessThan(0.3);
  }

  @Test
  void vsNothingIsUseless() {
    List<Agent> a =
        IntRange.of(0, 4).stream()
            .mapToObj(unused -> factory.of(UnitType.Terran_Marine))
            .collect(Collectors.toList());
    List<Agent> b = Collections.emptyList();

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isEqualTo(0.5);
  }

  @Test
  void vsHatcheryIsFreeWin() {
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Zerg_Zergling));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Zerg_Hatchery));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isGreaterThan(0.9);
  }

  @Test
  void vsSomeUnitsAndHatch() {
    List<Agent> a =
        IntRange.of(0, 8).stream()
            .mapToObj(unused -> factory.of(UnitType.Zerg_Zergling))
            .collect(Collectors.toList());
    List<Agent> b =
        Stream.concat(
                IntRange.of(0, 3).stream().mapToObj(unused -> factory.of(UnitType.Zerg_Zergling)),
                Stream.of(factory.of(UnitType.Zerg_Lair), factory.of(UnitType.Zerg_Overlord)))
            .collect(Collectors.toList());
    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isGreaterThan(0.8);
  }

  @Test
  void lingVsOverlordIsUseless() {
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Zerg_Zergling));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Zerg_Overlord));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isEqualTo(EVAL_NO_COMBAT);
  }

  @Test
  void optimizeAwayZergVsOverlord() {
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Zerg_Zergling));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Zerg_Overlord));

    // WHEN
    EvalWithAgents result = evaluator.optimizeEval(a, b);

    // THEN
    assertThat(result.agents).isEmpty();
  }

  @Test
  void optimizeAwayLingsInMutaVsZealots() {
    Agent mutalisk = factory.of(UnitType.Zerg_Mutalisk);
    List<Agent> a = Arrays.asList(factory.of(UnitType.Zerg_Zergling), mutalisk);
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Protoss_Zealot));

    // WHEN
    EvalWithAgents result = evaluator.optimizeEval(a, b);

    // THEN
    assertThat(result.agents).asList().containsOnly(mutalisk);
  }

  @Test
  void dontOptimizeAwayTwoLingsInMutaVsZealots() {
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Mutalisk));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Protoss_Zealot));

    // WHEN
    EvalWithAgents result = evaluator.optimizeEval(a, b);

    // THEN
    assertThat(result.agents).asList().containsAll(a);
  }

  @Test
  void optimizeAwayLingsVsZealotsWithEnoughMutas() {
    List<Agent> a =
        Arrays.asList(
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk),
            factory.of(UnitType.Zerg_Mutalisk));
    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Protoss_Zealot),
            factory.of(UnitType.Protoss_Zealot),
            factory.of(UnitType.Protoss_Zealot));

    // WHEN
    EvalWithAgents result = evaluator.optimizeEval(a, b);

    // THEN
    assertThat(result.agents).allMatch(it -> it.isFlyer);
  }

  @Test
  void _5firebatsVs9ZZealotsAreDead() {
    // GIVEN
    List<Agent> a =
        IntRange.of(0, 5).stream()
            .mapToObj(unused -> factory.of(UnitType.Terran_Firebat))
            .collect(Collectors.toList());
    List<Agent> b =
        IntRange.of(0, 9).stream()
            .mapToObj(unused -> factory.of(UnitType.Protoss_Zealot))
            .collect(Collectors.toList());

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isLessThan(0.2);
  }

  @Test
  void _9firebatsVs9ZZealotsAreEvenlyMatched() {
    // GIVEN
    List<Agent> a =
        IntRange.of(0, 9).stream()
            .mapToObj(unused -> factory.of(UnitType.Terran_Firebat))
            .collect(Collectors.toList());
    List<Agent> b =
        IntRange.of(0, 9).stream()
            .mapToObj(unused -> factory.of(UnitType.Protoss_Zealot))
            .collect(Collectors.toList());

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isBetween(0.4, 0.6);
  }

  @Test
  void spiderMinesDontAttackHoveringOrStaticUnits() {
    List<Agent> a =
        Collections.singletonList(
            factory.of(UnitType.Terran_Vulture_Spider_Mine).setDetected(false));
    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_SCV),
            factory.of(UnitType.Protoss_Probe),
            factory.of(UnitType.Zerg_Drone),
            factory.of(UnitType.Protoss_Scout),
            factory.of(UnitType.Protoss_Photon_Cannon));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result).isEqualTo(EVAL_NO_COMBAT);
  }

  @Test
  void spiderMinesOnlyAttackGroundMovingUnits() {
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Terran_Vulture_Spider_Mine));
    List<Agent> b =
        Arrays.asList(
            factory.of(UnitType.Terran_Marine),
            factory.of(UnitType.Protoss_Zealot),
            factory.of(UnitType.Zerg_Zergling));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isBetween(0.3, 0.4);
  }

  @Test
  void carrierVsCarrier() {
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Protoss_Carrier));
    List<Agent> b = Collections.singletonList(factory.of(UnitType.Protoss_Carrier));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isBetween(0.49, 0.51);
  }

  @Test
  void firebatsVs3Lings() {
    List<Agent> a = Collections.singletonList(factory.of(UnitType.Terran_Firebat));
    List<Agent> b = Arrays
        .asList(factory.of(UnitType.Zerg_Zergling), factory.of(UnitType.Zerg_Zergling),
            factory.of(UnitType.Zerg_Zergling));

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN
    assertThat(result.value).isBetween(0.35, 0.45);
  }

  @Test
  void mmVsLingSunken() {
    // GIVEN
    List<Agent> a = Stream
        .concat(IntStream.range(0, 16).mapToObj(_i -> factory.of(UnitType.Zerg_Zergling)),
            Stream.of(factory.of(UnitType.Zerg_Sunken_Colony),
                factory.of(UnitType.Zerg_Sunken_Colony))).collect(Collectors.toList());
    List<Agent> b = Stream
        .concat(IntStream.range(0, 12).mapToObj(_i -> factory.of(UnitType.Terran_Marine)),
            IntStream.range(0, 4).mapToObj(_i -> factory.of(UnitType.Terran_Medic)))
        .collect(Collectors.toList());

    // WHEN
    EvaluationResult result = evaluator.evaluate(a, b);

    // THEN

    assertThat(result.value).isBetween(0.45, 0.55);
  }

}

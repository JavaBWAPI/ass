package org.bk.ass;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openbw.bwapi4j.test.BWDataProvider;
import org.openbw.bwapi4j.type.UnitType;

class EvaluatorTest {

  private Evaluator evaluator = new Evaluator();
  private BWAPI4JAgentFactory factory = new BWAPI4JAgentFactory();

  @BeforeAll
  static void setup() throws Exception {
    BWDataProvider.injectValues();
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
    assertThat(result).isLessThan(0.48);
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
    assertThat(result).isGreaterThan(0.52);
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
}

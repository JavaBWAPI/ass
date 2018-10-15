package org.bk.ass;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openbw.bwapi4j.test.BWDataProvider;
import org.openbw.bwapi4j.type.UnitType;

class SimulatorTest {

  private Simulator simulator = new Simulator();
  private BWAPI4JAgentFactory factory = new BWAPI4JAgentFactory(null);

  @BeforeAll
  static void setup() throws Exception {
    BWDataProvider.injectValues();
  }

  @Test
  void stimmedVsUnstimmed() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine, 0, 0).setCanStim(true));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine, 0, 0).setCanStim(true));
    simulator.addAgentB(factory.of(UnitType.Terran_Medic, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void MMVsSunkens() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Medic, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Medic, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Medic, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Zerg_Sunken_Colony, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Zerg_Sunken_Colony, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).size().isLessThanOrEqualTo(5);
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void MMvsMM() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Medic, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Medic, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Medic, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Medic, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).size().isLessThanOrEqualTo(2);
    assertThat(simulator.getAgentsB()).size().isLessThanOrEqualTo(2);
  }

  @Test
  void vultureVs4Zergling() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Vulture, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsB()).size().isLessThanOrEqualTo(2);
  }

  @Test
  void twoMarinesVsOneToTheDeath() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void marineVsValkyrieSingleFrame() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Valkyrie, 0, 0));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsB())
        .element(0)
        .hasFieldOrPropertyWithValue("health", (UnitType.Terran_Valkyrie.maxHitPoints() - 4));
  }

  @Test
  void GoonCloseToSiegedTank() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Protoss_Dragoon, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Siege_Tank_Siege_Mode, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void GoonAwayFromSiegedTank() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Protoss_Dragoon, 0, 0).setX(1000));
    simulator.addAgentB(factory.of(UnitType.Terran_Siege_Tank_Siege_Mode, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void _3ZerglingAwayFromSiegedTankAndMarine() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling, 0, 0).setX(1000));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling, 0, 0).setX(1000).setY(20));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling, 0, 0).setX(1000).setY(-20));
    simulator.addAgentB(factory.of(UnitType.Terran_Siege_Tank_Siege_Mode, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void _6ZerglingAwayFromSiegedTankAndMarine() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling, 0, 0).setX(-400).setY(60));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling, 0, 0).setX(400).setY(-40));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling, 0, 0).setX(-400).setY(-40));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling, 0, 0).setX(400).setY(20));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling, 0, 0).setX(-400).setY(0));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling, 0, 0).setX(400).setY(-20));
    simulator.addAgentB(factory.of(UnitType.Terran_Siege_Tank_Siege_Mode, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void GoonVsTank() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Protoss_Dragoon, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Siege_Tank_Tank_Mode, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void MarineVsLurker() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Zerg_Lurker, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void MarineVsBurrowedLurker() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Zerg_Lurker, 0, 0).setBurrowed(true));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }


  @Test
  void _7MutaVs1BunkerAndSCV() {
    // GIVEN
    for (int i = 0; i < 7; i++) {
      simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk, 0, 0));
    }
    simulator.addAgentB(factory.of(UnitType.Terran_Bunker, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_SCV, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void _5MutaVs1BunkerAndSCV() {
    // GIVEN
    for (int i = 0; i < 5; i++) {
      simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk, 0, 0));
    }
    simulator.addAgentB(factory.of(UnitType.Terran_Bunker, 0, 0));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void _7MutasVs8Hydras() {
    // GIVEN
    for (int i = 0; i < 7; i++) {
      simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk, 0, 0));
    }
    for (int i = 0; i < 8; i++) {
      simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk, 0, 0));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).size().isLessThanOrEqualTo(1);
    assertThat(simulator.getAgentsB()).size().isLessThanOrEqualTo(4);
  }

  @Test
  void MutaVsVulture() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk, 0, 0));
    simulator.addAgentB(factory.of(UnitType.Terran_Vulture, 0, 0));

    // WHEN
    simulator.simulate(200);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void LargeArmiesTest() {
    // GIVEN
    for (int i = 0; i < 1000; i++) {
      simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk, 0, 0));
      simulator.addAgentB(factory.of(UnitType.Terran_Marine, 0, 0));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void _10HydrasVsDT() {
    // GIVEN
    for (int i = 0; i < 10; i++) {
      simulator.addAgentA(factory.of(UnitType.Zerg_Hydralisk, 0, 0));
    }
    simulator.addAgentB(factory.of(UnitType.Protoss_Dark_Templar, 0, 0).setDetected(false));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void _7MutasVs14Marines() {
    // GIVEN
    for (int i = 0; i < 7; i++) {
      simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk, 0, 0));
    }
    for (int i = 0; i < 14; i++) {
      simulator.addAgentB(factory.of(UnitType.Terran_Marine, 0, 0));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

}

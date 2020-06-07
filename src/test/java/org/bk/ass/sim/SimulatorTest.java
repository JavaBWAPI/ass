package org.bk.ass.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.bk.ass.PositionOutOfBoundsException;
import org.bk.ass.sim.Simulator.Builder;
import org.bk.ass.sim.Simulator.RoleBasedBehavior;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openbw.bwapi4j.test.BWDataProvider;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.WeaponType;

class SimulatorTest {

  private Simulator simulator = new Builder().build();
  private BWAPI4JAgentFactory factory = new BWAPI4JAgentFactory(null);

  @BeforeAll
  static void setup() throws Exception {
    BWDataProvider.injectValues();
  }

  @Test
  void stimmedVsUnstimmed() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine).setCanStim(true));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine).setCanStim(true));
    simulator.addAgentB(factory.of(UnitType.Terran_Medic));
    simulator.addAgentB(factory.of(UnitType.Terran_Medic));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void mmVsM() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Medic));
    simulator.addAgentB(factory.of(UnitType.Terran_Medic));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void MMVsSunkens() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Medic));
    simulator.addAgentA(factory.of(UnitType.Terran_Medic));
    simulator.addAgentA(factory.of(UnitType.Terran_Medic));
    simulator.addAgentB(factory.of(UnitType.Zerg_Sunken_Colony));
    simulator.addAgentB(factory.of(UnitType.Zerg_Sunken_Colony));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).size().isLessThanOrEqualTo(5);
    assertThat(simulator.getAgentsB()).size().isLessThanOrEqualTo(1);
  }

  @Test
  void MMvsMM() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Medic));
    simulator.addAgentA(factory.of(UnitType.Terran_Medic));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Medic));
    simulator.addAgentB(factory.of(UnitType.Terran_Medic));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).size().isLessThanOrEqualTo(2);
    assertThat(simulator.getAgentsB()).size().isLessThanOrEqualTo(2);
  }

  @Test
  void MMvsMM_FS4() {
    // GIVEN
    simulator = new Builder().withFrameSkip(4).build();
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Medic));
    simulator.addAgentA(factory.of(UnitType.Terran_Medic));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Medic));
    simulator.addAgentB(factory.of(UnitType.Terran_Medic));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).size().isLessThanOrEqualTo(2);
    assertThat(simulator.getAgentsB()).size().isLessThanOrEqualTo(2);
  }

  @Test
  void vultureVs4Zergling() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Vulture).setX(20).setY(50));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsB()).size().isLessThanOrEqualTo(2);
  }

  @Test
  void twoMarinesVsOneToTheDeath() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void marineVsValkyrieSingleFrame() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Terran_Valkyrie));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsB())
        .element(0)
        .hasFieldOrPropertyWithValue("health", (UnitType.Terran_Valkyrie.maxHitPoints() - 4));
  }

  @Test
  void lurkerVsTwoOpposingMarinesSingleFrame() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine).setX(0));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine).setX(60));
    simulator.addAgentB(factory.of(UnitType.Zerg_Lurker).setX(30).setBurrowed(true));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsA()).extracting("health").contains(20, 40);
  }

  @Test
  void tankVsBurrowedZerglings() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Siege_Tank_Siege_Mode));
    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(384).setBurrowed(true));
    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(394).setBurrowed(true));
    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(404).setBurrowed(true));
    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(404));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsB())
        .extracting("health")
        .contains(
            80, // out of inner splash range while burrowed
            62, // direct hit
            62, // inner splash hit burrowed
            71); // unburrowed, inside outer splash
  }

  @Test
  void lurkerVsTwoMarinesSingleFrame() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine).setX(20));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine).setX(40));
    simulator.addAgentB(factory.of(UnitType.Zerg_Lurker).setBurrowed(true));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsA()).extracting("health").containsOnly(20);
  }

  @Test
  void lurkerVsOneMarineAndOneInSplashRangeSingleFrame() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine).setX(20));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine).setX(200));
    simulator.addAgentB(factory.of(UnitType.Zerg_Lurker).setBurrowed(true));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsA()).extracting("health").containsOnly(20);
  }

  @Test
  void lurkerVsOneMarineAndOneOutOfSplashRangeSingleFrame() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine).setX(20));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine).setX(192 + 20 + 1));
    simulator.addAgentB(factory.of(UnitType.Zerg_Lurker).setBurrowed(true));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsA()).extracting("health").containsOnly(40, 20);
  }

  @Test
  void fireBatVs2Lings() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Firebat).setX(20));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void GoonCloseToSiegedTank() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Protoss_Dragoon));
    simulator.addAgentB(factory.of(UnitType.Terran_Siege_Tank_Siege_Mode));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void mutaVs3GhostsSingleFrame() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk).setX(20));
    simulator.addAgentB(factory.of(UnitType.Terran_Ghost));
    simulator.addAgentB(factory.of(UnitType.Terran_Ghost));
    simulator.addAgentB(factory.of(UnitType.Terran_Ghost));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsB()).extracting("health").containsOnly(45 - 9, 45 - 3, 45 - 1);
  }

  @Test
  void GoonAwayFromSiegedTank() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Protoss_Dragoon).setX(1000));
    simulator.addAgentB(factory.of(UnitType.Terran_Siege_Tank_Siege_Mode));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void _3ZerglingAwayFromSiegedTankAndMarine() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling).setX(400).setY(20));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling).setX(400));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling).setX(400).setY(40));
    simulator.addAgentB(factory.of(UnitType.Terran_Siege_Tank_Siege_Mode));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void _6ZerglingAwayFromSiegedTankAndMarine() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling).setX(1200));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling).setX(2000));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling).setX(1200).setY(600));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling).setX(2000).setY(600));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling).setX(1200).setY(300));
    simulator.addAgentA(factory.of(UnitType.Zerg_Zergling).setX(2000).setY(300));

    simulator.addAgentB(factory.of(UnitType.Terran_Siege_Tank_Siege_Mode).setX(1600).setY(300));
    simulator.addAgentB(factory.of(UnitType.Terran_Marine).setX(1600).setY(300));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void GoonVsTank() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Protoss_Dragoon));
    simulator.addAgentB(factory.of(UnitType.Terran_Siege_Tank_Tank_Mode));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void MarineVsLurker() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Zerg_Lurker));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void MarineVsBurrowedLurker() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentB(factory.of(UnitType.Zerg_Lurker).setBurrowed(true));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void _2LurkersVs10Marines() {
    // GIVEN
    simulator
        .addAgentA(factory.of(UnitType.Zerg_Lurker).setBurrowed(true).setX(200).setY(200))
        .addAgentA(factory.of(UnitType.Zerg_Lurker).setBurrowed(true).setX(210).setY(200));

    for (int i = 0; i < 10; i++) {
      simulator.addAgentB(factory.of(UnitType.Terran_Marine).setX(10 * i).setY(20));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void _2LurkersVs12Marines() {
    // GIVEN
    simulator
        .addAgentA(factory.of(UnitType.Zerg_Lurker).setBurrowed(true).setX(130).setY(30))
        .addAgentA(factory.of(UnitType.Zerg_Lurker).setBurrowed(true).setX(150).setY(50));

    for (int i = 0; i < 12; i++) {
      simulator.addAgentB(factory.of(UnitType.Terran_Marine).setX(16 * i + 76).setY(20));
    }

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
      simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk));
    }
    simulator.addAgentB(factory.of(UnitType.Terran_Bunker));
    simulator.addAgentB(factory.of(UnitType.Terran_SCV));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void _6MutaVs1BunkerAnd4SCVs() {
    // GIVEN
    for (int i = 0; i < 6; i++) {
      simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk));
    }
    simulator.addAgentB(factory.of(UnitType.Terran_Bunker));
    for (int i = 0; i < 4; i++) {
      simulator.addAgentB(factory.of(UnitType.Terran_SCV));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void _5MutaVs1Bunker() {
    // GIVEN
    for (int i = 0; i < 5; i++) {
      simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk).setX(1000));
    }
    simulator.addAgentB(factory.of(UnitType.Terran_Bunker).setX(1100));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void _7MutasVs9Hydras() {
    // GIVEN
    for (int i = 0; i < 7; i++) {
      simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk).setX(i * 16));
    }
    for (int i = 0; i < 9; i++) {
      simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(i * 16).setY(32));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).size().isLessThanOrEqualTo(3);
    assertThat(simulator.getAgentsB()).size().isLessThanOrEqualTo(1);
  }

  @Test
  void _8DragoonsVs6Hydras() {
    // GIVEN
    for (int i = 0; i < 8; i++) {
      simulator.addAgentA(factory.of(UnitType.Protoss_Dragoon).setX(1000 + i * 8).setY(800));
    }
    for (int i = 0; i < 6; i++) {
      simulator.addAgentB(
          factory
              .of(UnitType.Zerg_Hydralisk, 0, 0, 0, 0, false, false, false)
              .setX(1000 + i * 8)
              .setY(1200));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).hasSizeLessThanOrEqualTo(3);
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void _8DragoonsWithRangeUpgradeVs6Hydras() {
    // GIVEN
    for (int i = 0; i < 8; i++) {
      simulator.addAgentA(factory.of(UnitType.Protoss_Dragoon, 0, 0, 64, 64, false, false, false).setX(1000 + i * 8).setY(800));
    }
    for (int i = 0; i < 6; i++) {
      simulator.addAgentB(
          factory
              .of(UnitType.Zerg_Hydralisk, 0, 0, 0, 0, false, false, false)
              .setX(1000 + i * 8)
              .setY(1200));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).hasSize(8);
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void _12DragoonsVs10UpgradedHydras() {
    // GIVEN
    for (int i = 0; i < 12; i++) {
      simulator.addAgentA(factory.of(UnitType.Protoss_Dragoon).setX(400 + i * 8).setY(400));
    }
    for (int i = 0; i < 10; i++) {
      simulator.addAgentB(
          factory.of(UnitType.Zerg_Hydralisk, 0, 0, 32, 32, true, false, false).setX(200 + i * 8));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).size().isLessThanOrEqualTo(3);
  }

  @Test
  void _12MarinesVs10Hydras() {
    // GIVEN
    for (int i = 0; i < 12; i++) {
      simulator.addAgentA(factory.of(UnitType.Terran_Marine).setX(200 + i * 8).setY(400));
    }
    for (int i = 0; i < 10; i++) {
      simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(200 + i * 8).setY(500));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).size().isLessThanOrEqualTo(3);
  }

  @Test
  void MutaVsVulture() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk));
    simulator.addAgentB(factory.of(UnitType.Terran_Vulture));

    // WHEN
    simulator.simulate(200);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void LargeArmiesTest() {
    // GIVEN
    for (int i = 0; i < 500; i++) {
      simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk).setX(500 + i * 4).setY(500));
      simulator.addAgentB(factory.of(UnitType.Terran_Marine).setX(500 + i * 8).setY(700));
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
      simulator.addAgentA(factory.of(UnitType.Zerg_Hydralisk).setX(1000 + i * 8).setY(1000));
    }
    simulator.addAgentB(
        factory.of(UnitType.Protoss_Dark_Templar).setDetected(false).setX(1000).setY(1100));

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
      simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk));
    }
    for (int i = 0; i < 14; i++) {
      simulator.addAgentB(factory.of(UnitType.Terran_Marine));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void tankSplashShouldAffectOwnUnits() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Siege_Tank_Siege_Mode));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine).setX(100));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling).setX(100));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsA()).hasSize(1);
  }

  @Test
  void archonSplashShouldNotAffectOwnUnits() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Protoss_Archon));
    simulator.addAgentA(factory.of(UnitType.Protoss_Zealot).setX(48));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling).setX(48));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsA())
        .element(1)
        .hasFieldOrPropertyWithValue("shieldsShifted", 14087);
  }

  @Test
  void goonShouldDieWhenRunningAwayFromScout() {
    // GIVEN
    simulator = new Builder().withPlayerABehavior(new RetreatBehavior()).build();
    simulator.addAgentA(factory.of(UnitType.Protoss_Dragoon).setX(500));
    simulator.addAgentB(factory.of(UnitType.Protoss_Scout).setX(495));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB())
        .element(0)
        .hasFieldOrPropertyWithValue("healthShifted", 38400);
  }

  @Test
  void addAgentAAtInvalidPositionShouldThrowException() {
    simulator.addAgentA(factory.of(UnitType.Protoss_Scout).setX(9000));

    assertThrows(
        PositionOutOfBoundsException.class,
        () -> simulator.simulate(1));
  }

  @Test
  void addAgentBAtInvalidPositionShouldThrowException() {
    simulator.addAgentB(factory.of(UnitType.Protoss_Scout).setY(9000));

    assertThrows(
        PositionOutOfBoundsException.class,
        () -> simulator.simulate(1));
  }

  @Test
  void shouldResetCollisions() {
    // GIVEN
    for (int i = 0; i < 1000; i++)
      simulator.addAgentA(factory.of(UnitType.Protoss_Dragoon).setX(i * 8).setY(i * 8));
    for (int i = 0; i < 1000; i++)
      simulator.addAgentB(factory.of(UnitType.Protoss_Scout).setX(8191 - i * 8).setY(i * 8));
    simulator.simulate();

    // WHEN
    simulator.reset();

    // THEN
    assertThat(simulator.collision).containsOnly(0);
  }

  @Test
  void shouldNotAttackStasisedUnitsNorBeAttackedByThem() {
    // GIVEN
    simulator = new Builder().build();
    simulator.addAgentA(
        factory.of(UnitType.Terran_Goliath).setX(500).setStasisTimer(Integer.MAX_VALUE));
    simulator.addAgentB(factory.of(UnitType.Terran_Wraith).setX(495));

    // WHEN
    simulator.simulate();

    // THEN
    assertThat(simulator.getAgentsA()).first().extracting(Agent::getHealth).isEqualTo(125);
    assertThat(simulator.getAgentsB()).first().extracting(Agent::getHealth).isEqualTo(120);
  }

  @Test
  void shouldAttackLockeddownUnitsButDontBeAttackedByThem() {
    // GIVEN
    simulator = new Builder().build();
    simulator.addAgentA(
        factory.of(UnitType.Terran_Goliath).setX(500).setLockDownTimer(Integer.MAX_VALUE));
    simulator.addAgentB(factory.of(UnitType.Terran_Wraith).setX(495));

    // WHEN
    simulator.simulate();

    // THEN
    assertThat(simulator.getAgentsA()).first().extracting(Agent::getHealth).isEqualTo(97);
    assertThat(simulator.getAgentsB()).first().extracting(Agent::getHealth).isEqualTo(120);
  }

  @Test
  void shouldNotRepairStasisedUnit() {
    // GIVEN
    simulator = new Builder().build();
    simulator.addAgentA(
        factory
            .of(UnitType.Terran_Goliath)
            .setX(500)
            .setStasisTimer(Integer.MAX_VALUE)
            .setHealth(97));
    simulator.addAgentA(factory.of(UnitType.Terran_SCV).setX(500));

    // WHEN
    simulator.simulate();

    // THEN
    assertThat(simulator.getAgentsA()).extracting(Agent::getHealth).contains(97);
  }

  @Test
  void shouldRepairLockeddownUnit() {
    // GIVEN
    simulator = new Builder().build();
    simulator.addAgentA(
        factory
            .of(UnitType.Terran_Goliath)
            .setX(500)
            .setLockDownTimer(Integer.MAX_VALUE)
            .setHealth(97));
    simulator.addAgentA(factory.of(UnitType.Terran_SCV).setX(500));

    // WHEN
    simulator.simulate();

    // THEN
    assertThat(simulator.getAgentsA()).extracting(Agent::getHealth).contains(97);
  }

  @Test
  void shouldSelectCarrierAndNotInterceptor() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Protoss_Interceptor).setX(500));
    Agent carrier = factory.of(UnitType.Protoss_Carrier);
    simulator.addAgentA(carrier.setX(500));
    simulator.addAgentA(factory.of(UnitType.Protoss_Interceptor).setX(500));
    simulator.addAgentB(factory.of(UnitType.Terran_Wraith).setX(500));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsB()).first().extracting(a -> a.attackTarget).isEqualTo(carrier);
  }

  @Test
  void InterceptorsShouldDieIfCarrierDies() {
    // GIVEN
    simulator.addAgentA(
        factory
            .of(UnitType.Protoss_Interceptor)
            .setX(500)
            .setAttackTargetPriority(Agent.TargetingPriority.MEDIUM));
    simulator.addAgentA(
        factory
            .of(UnitType.Protoss_Interceptor)
            .setX(500)
            .setAttackTargetPriority(Agent.TargetingPriority.MEDIUM));
    simulator.addAgentA(
        factory
            .of(UnitType.Protoss_Interceptor)
            .setX(500)
            .setAttackTargetPriority(Agent.TargetingPriority.MEDIUM));
    simulator.addAgentA(
        factory
            .of(UnitType.Protoss_Interceptor)
            .setX(500)
            .setAttackTargetPriority(Agent.TargetingPriority.MEDIUM));
    simulator.addAgentA(
        factory
            .of(UnitType.Protoss_Interceptor)
            .setX(500)
            .setAttackTargetPriority(Agent.TargetingPriority.MEDIUM));
    Agent carrier = factory.of(UnitType.Protoss_Carrier);
    carrier.setInterceptors(simulator.getAgentsA());
    simulator.addAgentA(carrier.setX(500));

    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(500).setY(10));
    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(500).setY(30));
    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(500).setY(50));
    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(500).setY(70));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void darkSwarmZerglingVsMarines() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Marine).setProtectedByDarkSwarm(true));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine).setProtectedByDarkSwarm(true));
    simulator.addAgentA(factory.of(UnitType.Terran_Marine).setProtectedByDarkSwarm(true));

    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling).setProtectedByDarkSwarm(true));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void shouldDealPlagueDamage() {
    // GIVEN
    simulator.addAgentA(
        factory.of(UnitType.Terran_Marine).setPlagueDamage(WeaponType.Plague.damageAmount()));

    // Dummy unit
    simulator.addAgentB(factory.of(UnitType.Protoss_Carrier));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsA())
        .first()
        .extracting(a -> a.healthShifted)
        .isEqualTo(9230); // ~4 damage taken
  }

  @Test
  void shouldNotDieFromPlague() {
    // GIVEN
    simulator.addAgentA(
        factory
            .of(UnitType.Terran_Marine)
            .setPlagueDamage(WeaponType.Plague.damageAmount())
            .setHealth(2));

    // Dummy unit
    simulator.addAgentB(factory.of(UnitType.Protoss_Carrier));

    // WHEN
    simulator.simulate(1);

    // THEN
    assertThat(simulator.getAgentsA())
        .first()
        .extracting(a -> a.healthShifted)
        .isEqualTo(512); // No damage taken
  }

  @Test
  void reaverVs9Lings() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Protoss_Reaver));

    for (int i = 0; i < 9; i++) {
      simulator.addAgentB(factory.of(UnitType.Zerg_Zergling).setX(i * 30));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void reaverVs12Lings() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Protoss_Reaver));

    for (int i = 0; i < 13; i++) {
      simulator.addAgentB(factory.of(UnitType.Zerg_Zergling).setX(i * 30));
    }

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void lingsVsZealots_FS3() {
    // GIVEN
    simulator = new Builder().withFrameSkip(3).build();
    simulator.addAgentA(factory.of(UnitType.Protoss_Zealot));
    simulator.addAgentA(factory.of(UnitType.Protoss_Zealot));

    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));

    // WHEN
    simulator.simulate(250);

    // THEN
    assertThat(simulator.getAgentsA()).size().isOne();
    assertThat(simulator.getAgentsB()).size().isZero();
  }

  @Test
  void scourgesVsScouts() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Zerg_Scourge).setX(100).setY(100));
    simulator.addAgentA(factory.of(UnitType.Zerg_Scourge).setX(100).setY(120));
    simulator.addAgentA(factory.of(UnitType.Zerg_Scourge).setX(100).setY(140));

    simulator.addAgentB(factory.of(UnitType.Protoss_Scout).setX(200).setY(100));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).size().isZero();
    assertThat(simulator.getAgentsB()).size().isZero();
  }

  @Test
  void approxReaverVs12Zergling() {
    // GIVEN
    approxSim();

    simulator.addAgentA(factory.of(UnitType.Protoss_Reaver));

    for (int i = 0; i < 12; i++) {
      simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    }
    AgentUtil.randomizePositions(simulator.getAgentsA(), 0, 0, 32, 32);
    AgentUtil.randomizePositions(simulator.getAgentsB(), 32, 0, 80, 32);

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsB()).hasSizeLessThan(3);
  }

  @Test
  void approxLingsVsZealots() {
    // GIVEN
    approxSim();

    simulator.addAgentA(factory.of(UnitType.Protoss_Zealot));
    simulator.addAgentA(factory.of(UnitType.Protoss_Zealot));

    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).hasSizeLessThan(2);
    assertThat(simulator.getAgentsB()).hasSizeLessThan(2);
  }

  private void approxSim() {
    simulator =
        new Builder()
            .withFrameSkip(37)
            .withPlayerABehavior(new ApproxAttackBehavior())
            .withPlayerBBehavior(new ApproxAttackBehavior())
            .build();
  }

  @Test
  void _6ZerglingVsSiegedTankAndMarine() {
    // GIVEN
    approxSim();
    simulator.reset();
    simulator.addAgentA(factory.of(UnitType.Terran_Marine));
    simulator.addAgentA(factory.of(UnitType.Terran_Siege_Tank_Siege_Mode));

    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));

    AgentUtil.randomizePositions(simulator.getAgentsA(), 0, 0, 32, 32);
    AgentUtil.randomizePositions(simulator.getAgentsB(), 32, 0, 64, 64);

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).hasSizeLessThan(2);
    assertThat(simulator.getAgentsB()).hasSizeLessThan(4);
  }

  @Test
  void approx1ScourceVs2Overlords() {
    // GIVEN
    approxSim();

    simulator.reset();
    simulator.addAgentA(factory.of(UnitType.Zerg_Scourge));

    simulator.addAgentB(factory.of(UnitType.Zerg_Overlord));
    simulator.addAgentB(factory.of(UnitType.Zerg_Overlord));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void shouldDieWhenRunningAwayWithSpeedPenalty() {
    // GIVEN
    simulator = new Builder().withPlayerABehavior(new RetreatBehavior()).build();
    simulator.addAgentA(
        factory.of(UnitType.Zerg_Zergling).setX(120).setY(100).setSpeedFactor(0.9f));

    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling).setX(100).setY(100));

    // WHEN
    simulator.simulate(100);

    // THEN
    assertThat(simulator.getAgentsA()).size().isZero();
    assertThat(simulator.getAgentsB()).size().isOne();
  }

  @Test
  void spiderMineShouldNotAttackBuilding() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Vulture_Spider_Mine).setDetected(false).setBurrowed(true));
    simulator.addAgentB(factory.of(UnitType.Zerg_Sunken_Colony));

    // WHEN
    simulator.simulate(200);

    // THEN
    assertThat(simulator.getAgentsA()).hasSize(1);
    assertThat(simulator.getAgentsB()).hasSize(1);
  }

  @Test
  void spiderMineShouldAttackWithinSeekRange() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Vulture_Spider_Mine).setDetected(false).setBurrowed(true));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void spiderMineShouldNotAttackOutsideOfSeekRange() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Vulture_Spider_Mine).setDetected(false).setBurrowed(true));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling).setX(128));

    // WHEN
    simulator.simulate(200);

    // THEN
    assertThat(simulator.getAgentsA())
        .first()
        .extracting(it -> it.x, it -> it.y)
        .containsExactly(0, 0);
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void spiderMineShouldDoSplashDamage() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Vulture_Spider_Mine).setDetected(false).setBurrowed(true));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling).setX(16));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling).setX(16));

    // WHEN
    simulator.simulate(20);

    // THEN
    assertThat(simulator.getAgentsB()).isEmpty();
  }

  @Test
  void hydrasShouldTakeCareOfSpiderMines() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Vulture_Spider_Mine).setDetected(false).setBurrowed(true));
    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(96).setHealth(1));
    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(96).setHealth(1));
    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(96).setHealth(1));
    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(96).setHealth(1));
    simulator.addAgentB(factory.of(UnitType.Zerg_Hydralisk).setX(96).setHealth(1));

    // WHEN
    simulator.simulate(20);

    // THEN
    assertThat(simulator.getAgentsB()).isNotEmpty();
  }

  @Test
  void spiderMineShouldNotAttackWorker() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Terran_Vulture_Spider_Mine).setDetected(false).setBurrowed(true));
    simulator.addAgentB(factory.of(UnitType.Zerg_Drone));
    simulator.addAgentB(factory.of(UnitType.Terran_SCV));
    simulator.addAgentB(factory.of(UnitType.Protoss_Probe));

    // WHEN
    simulator.simulate(200);

    // THEN
    assertThat(simulator.getAgentsA()).hasSize(1);
    assertThat(simulator.getAgentsB()).hasSize(3);
  }

  @Test
  void mutaWillLoseVsGoliath() {
    // GIVEN
    simulator.addAgentA(factory.of(UnitType.Zerg_Mutalisk));
    simulator.addAgentB(factory.of(UnitType.Terran_Goliath));

    // WHEN
    simulator.simulate(300);

    // THEN
    assertThat(simulator.getAgentsA()).isEmpty();
    assertThat(simulator.getAgentsB()).hasSize(1);
  }

  @Test
  void staticDefenseShouldKeepAttackingOnRetreat() {
    // GIVEN
    simulator = new Builder().withPlayerABehavior(new RetreatBehavior())
        .withPlayerBBehavior(new RoleBasedBehavior()).build();
    simulator.addAgentA(factory.of(UnitType.Zerg_Sunken_Colony));
    simulator.addAgentB(factory.of(UnitType.Zerg_Zergling));

    // WHEN
    simulator.simulate(-1);

    // THEN
    assertThat(simulator.getAgentsA()).isNotEmpty();
    assertThat(simulator.getAgentsB()).isEmpty();
  }
}

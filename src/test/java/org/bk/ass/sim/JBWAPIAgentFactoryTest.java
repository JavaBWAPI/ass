package org.bk.ass.sim;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import bwapi.Unit;
import bwapi.UnitType;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Answers;

class JBWAPIAgentFactoryTest {

  private JBWAPIAgentFactory sut = new JBWAPIAgentFactory();

  @ParameterizedTest
  @MethodSource("buildings")
  void shouldMarkBuildingsPassive(UnitType type) {
    // WHEN
    Agent agent = sut.of(type);

    // THEN
    assertThat(agent.sleepTimer).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void shouldKeepBunkerActive() {
    // WHEN
    Agent agent = sut.of(UnitType.Terran_Bunker);

    // THEN
    assertThat(agent.sleepTimer).isZero();

  }

  @ParameterizedTest
  @MethodSource("nonAttackers")
  void nonAttackersShouldSleepEvenIfUnfinished(UnitType type) {
    // GIVEN
    Unit unit = mockUnit(type);
    given(unit.getRemainingBuildTime()).willReturn(123);

    // WHEN
    Agent agent = sut.of(unit);

    // THEN
    assertThat(agent.sleepTimer).isEqualTo(Integer.MAX_VALUE);
  }

  private Unit mockUnit(UnitType type) {
    Unit unit = mock(Unit.class, Answers.RETURNS_DEEP_STUBS);
    given(unit.getType()).willReturn(type);
    given(unit.isPowered()).willReturn(true);
    return unit;
  }

  @ParameterizedTest
  @MethodSource("attackers")
  void attackersShouldSleepWhenUnfinished(UnitType type) {
    // GIVEN
    Unit unit = mockUnit(type);
    given(unit.getRemainingBuildTime()).willReturn(374);

    // WHEN
    Agent agent = sut.of(unit);

    // THEN
    assertThat(agent.sleepTimer).isEqualTo(374);
  }

  private static Stream<UnitType> buildings() {
    return Arrays.stream(UnitType.values())
        .filter(it -> it.isBuilding() &&
            !it.canAttack() &&
            it != UnitType.Terran_Bunker);
  }

  private static Stream<UnitType> nonAttackers() {
    return Arrays.stream(UnitType.values())
        .filter(it -> !it.canAttack() &&
            !it.canMove() &&
            it != UnitType.Terran_Bunker);
  }

  private static Stream<UnitType> attackers() {
    return Arrays.stream(UnitType.values())
        .filter(it -> it.canAttack() ||
            it == UnitType.Terran_Bunker);
  }
}
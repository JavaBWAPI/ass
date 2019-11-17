package org.bk.ass.sim;

import static org.assertj.core.api.Assertions.assertThat;

import bwapi.UnitType;
import java.util.Arrays;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class BWMirrorAgentFactoryTest {

  private BWMirrorAgentFactory sut = new BWMirrorAgentFactory();

  @ParameterizedTest
  @MethodSource("buildings")
  void shouldMarkBuildingsPassive(UnitType type) {
    // WHEN
    Agent agent = sut.of(type);

    // THEN
    assertThat(agent.sleepFrames).isEqualTo(Integer.MAX_VALUE);
  }

  @Test
  void shouldKeepBunkerActive() {
    // WHEN
    Agent agent = sut.of(UnitType.Terran_Bunker);

    // THEN
    assertThat(agent.sleepFrames).isZero();

  }

  private static Stream<UnitType> buildings() {
    return Arrays.stream(UnitType.values()).filter(it -> it.isBuilding() && !it.canAttack() && it != UnitType.Terran_Bunker);
  }
}
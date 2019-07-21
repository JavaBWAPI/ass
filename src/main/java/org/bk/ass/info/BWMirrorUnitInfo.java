package org.bk.ass.info;

import bwapi.UnitType;

public class BWMirrorUnitInfo {
  private BWMirrorUnitInfo() {
    // Utility class
  }

  public static int stopFrames(UnitType unitType) {
    switch (unitType) {
      case Terran_Goliath:
      case Terran_Siege_Tank_Tank_Mode:
      case Terran_Siege_Tank_Siege_Mode:
      case Protoss_Reaver:
        return 1;
      case Terran_Ghost:
      case Zerg_Hydralisk:
        return 3;
      case Protoss_Arbiter:
      case Zerg_Zergling:
        return 4;
      case Protoss_Zealot:
      case Protoss_Dragoon:
        return 7;
      case Terran_Marine:
      case Terran_Firebat:
      case Protoss_Corsair:
        return 8;
      case Protoss_Dark_Templar:
      case Zerg_Devourer:
        return 9;
      case Zerg_Ultralisk:
        return 14;
      case Protoss_Archon:
        return 15;
      case Terran_Valkyrie:
        return 40;
      default:
        return 2;
    }
  }
}

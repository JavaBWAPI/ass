package org.bk.ass.manage;

import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;

/**
 * Utility class to create cost locks for units, tech and upgrades. A common reservation is shared,
 * which allows for a simpler resource management.
 */
public class CostLocks {

  private final Reservation<GMS> reservation;

  public CostLocks(Reservation<GMS> reservation) {
    this.reservation = reservation;
  }

  public Lock<GMS> unitCostLock(UnitType type) {
    return new Lock<>(reservation, () -> GMS.unitCost(type));
  }

  public Lock<GMS> techCostLock(TechType type) {
    return new Lock<>(reservation, () -> GMS.techCost(type));
  }

  public Lock<GMS> upgradeCostLock(UpgradeType type, int level) {
    return new Lock<>(reservation, () -> GMS.upgradeCost(type, level));
  }
}

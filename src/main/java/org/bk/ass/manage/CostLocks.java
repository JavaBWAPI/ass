package org.bk.ass.manage;

import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;

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

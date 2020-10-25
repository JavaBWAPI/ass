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
    Lock<GMS> gmsLock = new Lock<>(reservation);
    gmsLock.setItem(GMS.unitCost(type));
    return gmsLock;
  }

  public Lock<GMS> techCostLock(TechType type) {
    Lock<GMS> gmsLock = new Lock<>(reservation);
    gmsLock.setItem(GMS.techCost(type));
    return gmsLock;
  }

  public Lock<GMS> upgradeCostLock(UpgradeType type, int level) {
    Lock<GMS> gmsLock = new Lock<>(reservation);
    gmsLock.setItem(GMS.upgradeCost(type, level));
    return gmsLock;
  }
}

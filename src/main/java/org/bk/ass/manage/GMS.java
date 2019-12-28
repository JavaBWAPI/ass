package org.bk.ass.manage;

import static java.lang.Integer.max;

import bwapi.Player;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;

/** Representing gas + minerals + supply */
public final class GMS {
  public static final GMS ZERO = new GMS(0, 0, 0);
  public final int gas;
  public final int minerals;
  public final int supply;

  public GMS(int gas, int minerals, int supply) {
    this.gas = gas;
    this.minerals = minerals;
    this.supply = supply;
  }

  public static GMS available(Player player) {
    return new GMS(player.gas(), player.minerals(), player.supplyTotal() - player.supplyUsed());
  }

  public static GMS unitCost(UnitType unitType) {
    return new GMS(unitType.gasPrice(), unitType.mineralPrice(), unitType.supplyRequired());
  }

  public static GMS unitCost(org.openbw.bwapi4j.type.UnitType unitType) {
    return new GMS(unitType.gasPrice(), unitType.mineralPrice(), unitType.supplyRequired());
  }

  public static GMS techCost(TechType techType) {
    return new GMS(techType.gasPrice(), techType.mineralPrice(), 0);
  }

  public static GMS techCost(org.openbw.bwapi4j.type.TechType techType) {
    return new GMS(techType.gasPrice(), techType.mineralPrice(), 0);
  }

  public static GMS upgradeCost(UpgradeType upgradeType, int level) {
    return new GMS(upgradeType.gasPrice(level), upgradeType.mineralPrice(level), 0);
  }

  public static GMS upgradeCost(org.openbw.bwapi4j.type.UpgradeType upgradeType, int level) {
    return new GMS(upgradeType.gasPrice(level), upgradeType.mineralPrice(level), 0);
  }

  public GMS subtract(GMS gms) {
    return new GMS(gas - gms.gas, minerals - gms.minerals, supply - gms.supply);
  }

  public GMS add(GMS gms) {
    return new GMS(gas + gms.gas, minerals + gms.minerals, supply + gms.supply);
  }

  /**
   * If all components (gas, minerals, supply) of this value are non-negative, returns true if all
   * are &gt;= that of the given value. If a component is negative, returns false if the given
   * value's component is &gt;= 0. <br>
   * Usually all components are &gt;= 0 and this can be used to check if a price could be payed
   * <em>now</em>. <br>
   * If planning ahead is used, components could already be negative. But if the cost (ie. supply)
   * is 0, it might still be possible to purchase it immediately.
   */
  public boolean greaterOrEqual(GMS gms) {
    return max(0, gas) >= gms.gas
        && max(0, minerals) >= gms.minerals
        && max(0, supply) >= gms.supply;
  }

  @Override
  public String toString() {
    return "gas: " + gas + ", minerals: " + minerals + ", supply: " + supply;
  }
}

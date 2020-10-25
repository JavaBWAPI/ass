package org.bk.ass.manage;

import static java.lang.Integer.max;
import static java.lang.Integer.min;

import bwapi.Player;
import bwapi.TechType;
import bwapi.UnitType;
import bwapi.UpgradeType;
import java.util.Objects;

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

  public GMS multiply(int factor) {
    return new GMS(gas * factor, minerals * factor, supply * factor);
  }

  public int div(GMS other) {
    return min(min(other.gas > 0 ? gas / other.gas : Integer.MAX_VALUE,
        other.minerals > 0 ? minerals / other.minerals : Integer.MAX_VALUE),
        other.supply > 0 ? supply / other.supply : Integer.MAX_VALUE);
  }

  /**
   * If all components (gas, minerals, supply) of this value are non-negative, returns true if all
   * are &gt;= that of the given value. If a component is negative, returns false if the given
   * value's component is &gt;= 0. <br> Usually all components are &gt;= 0 and this can be used to
   * check if a price could be payed
   * <em>now</em>. <br>
   * If planning ahead is used, components could already be negative. But if the cost (ie. supply)
   * is 0, it might still be possible to purchase it immediately.
   */
  public boolean canAfford(GMS gms) {
    return max(0, gas) >= gms.gas
        && max(0, minerals) >= gms.minerals
        && max(0, supply) >= gms.supply;
  }

  @Override
  public String toString() {
    return "gas: " + gas + ", minerals: " + minerals + ", supply: " + supply;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GMS gms = (GMS) o;
    return gas == gms.gas &&
        minerals == gms.minerals &&
        supply == gms.supply;
  }

  @Override
  public int hashCode() {
    return Objects.hash(gas, minerals, supply);
  }
}

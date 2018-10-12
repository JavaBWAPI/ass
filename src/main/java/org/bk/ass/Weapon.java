package org.bk.ass;

public class Weapon {

  int maxRange;
  int minRangeSquared;
  int maxRangeSquared;
  int damageShifted;
  DamageType damageType;

  public Weapon setMaxRange(int maxRange) {
    this.maxRange = maxRange;
    this.maxRangeSquared = maxRange * maxRange;
    return this;
  }

  public Weapon setMinRange(int minRange) {
    this.minRangeSquared = minRange * minRange;
    return this;
  }

  public Weapon setDamage(int damage) {
    this.damageShifted = damage << 8;
    return this;
  }

  public Weapon setDamageType(DamageType damageType) {
    this.damageType = damageType;
    return this;
  }
}

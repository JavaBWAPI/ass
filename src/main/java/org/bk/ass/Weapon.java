package org.bk.ass;

public class Weapon {

  int maxRange;
  int minRangeSquared;
  int maxRangeSquared;
  int damageShifted;
  int innerSplashRadiusSquared;
  int medianSplashRadiusSquared;
  int outerSplashRadiusSquared;

  DamageType damageType;
  ExplosionType explosionType;

  public Weapon setOuterSplashRadius(int outerSplashRadiusSquared) {
    this.outerSplashRadiusSquared = outerSplashRadiusSquared * outerSplashRadiusSquared;
    return this;
  }

  public Weapon setMedianSplashRadius(int medianSplashRadiusSquared) {
    this.medianSplashRadiusSquared = medianSplashRadiusSquared * medianSplashRadiusSquared;
    return this;
  }

  public Weapon setInnerSplashRadius(int innerSplashRadiusSquared) {
    this.innerSplashRadiusSquared = innerSplashRadiusSquared * innerSplashRadiusSquared;
    return this;
  }

  public Weapon setExplosionType(ExplosionType explosionType) {
    this.explosionType = explosionType;
    return this;
  }

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

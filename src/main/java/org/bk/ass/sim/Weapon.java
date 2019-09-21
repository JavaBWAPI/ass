package org.bk.ass.sim;

public class Weapon {
  int maxRange;
  int minRangeSquared;
  int maxRangeSquared;
  // Complete damage including all hits
  int damageShifted;
  int hits;
  int innerSplashRadius;
  int innerSplashRadiusSquared;
  int medianSplashRadiusSquared;
  int outerSplashRadiusSquared;

  DamageType damageType;
  SplashType splashType;

  public Weapon setOuterSplashRadius(int outerSplashRadiusSquared) {
    this.outerSplashRadiusSquared = outerSplashRadiusSquared * outerSplashRadiusSquared;
    return this;
  }

  public Weapon setMedianSplashRadius(int medianSplashRadiusSquared) {
    this.medianSplashRadiusSquared = medianSplashRadiusSquared * medianSplashRadiusSquared;
    return this;
  }

  public Weapon setInnerSplashRadius(int innerSplashRadiusSquared) {
    this.innerSplashRadius = innerSplashRadiusSquared;
    this.innerSplashRadiusSquared = innerSplashRadiusSquared * innerSplashRadiusSquared;
    return this;
  }

  public Weapon setSplashType(SplashType splashType) {
    this.splashType = splashType;
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

  /** Number of hits this weapon will apply */
  public Weapon setHits(int hits) {
    this.hits = hits;
    return this;
  }

  public Weapon setDamageType(DamageType damageType) {
    this.damageType = damageType;
    return this;
  }
}

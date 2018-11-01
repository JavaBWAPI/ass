package org.bk.ass;

import static java.lang.Math.max;

import java.util.Collection;
import java.util.function.Consumer;

public class Agent {

  private final String name;
  int armorShifted;
  int shieldUpgrades;
  Object userObject;

  int elevationLevel = -2;
  int x;
  int y;
  int speedSquared;
  float speed;

  // Velocity (pixel per frame) for this frame to apply
  int vx;
  int vy;

  int healthShifted;
  int maxHealthShifted;
  boolean healedThisFrame;

  int hpConstructionRate;

  int shieldsShifted;
  int maxShieldsShifted;

  int energyShifted;
  int maxEnergyShifted;

  int cooldown;
  int maxCooldown;
  // Number of frames a move would break the attack
  int stopFrames;
  int remainingStimFrames;
  boolean canStim;

  // Is Zerg and not an Egg/Larva
  boolean regeneratesHealth;

  // Dies on attack
  boolean isSuicider;
  boolean isHealer;
  boolean isFlyer;
  boolean isOrganic;
  boolean isMechanic;
  boolean isKiter;
  boolean isRepairer;
  boolean burrowed;
  boolean burrowedAttacker;
  // Visible to the other force
  boolean detected;

  UnitSize size;

  boolean isMelee;
  Weapon airWeapon;
  Weapon groundWeapon;

  // Allow replacement of units on death (for example bunker -> marines)
  Consumer<Collection<Agent>> onDeathReplacer = ignored -> {
  };

  public Agent(String name) {
    this.name = name;
  }

  public Agent setUserObject(Object userObject) {
    this.userObject = userObject;
    return this;
  }

  public Object getUserObject() {
    return userObject;
  }

  public Agent setMelee(boolean melee) {
    isMelee = melee;
    return this;
  }

  public Agent setBurrowedAttacker(boolean burrowedAttacker) {
    this.burrowedAttacker = burrowedAttacker;
    return this;
  }

  public Agent setBurrowed(boolean burrowed) {
    this.burrowed = burrowed;
    return this;
  }

  public Agent setCanStim(boolean canStim) {
    this.canStim = canStim;
    return this;
  }

  public Agent setRemainingStimFrames(int remainingStimFrames) {
    this.remainingStimFrames = remainingStimFrames;
    return this;
  }

  @Override
  public String toString() {
    return name
        + " ("
        + x
        + ", "
        + y
        + "), hp: "
        + (healthShifted >> 8)
        + ", sh: "
        + (shieldsShifted >> 8);
  }

  public Agent setDetected(boolean detected) {
    this.detected = detected;
    return this;
  }

  public Agent setArmor(int armor) {
    this.armorShifted = armor << 8;
    return this;
  }

  public Agent setMaxEnergy(int maxEnergyShifted) {
    this.maxEnergyShifted = maxEnergyShifted << 8;
    return this;
  }

  public Agent setEnergy(int energyShifted) {
    this.energyShifted = energyShifted << 8;
    return this;
  }

  public Agent setShieldUpgrades(int shieldUpgrades) {
    this.shieldUpgrades = shieldUpgrades;
    return this;
  }

  public Agent setElevationLevel(int elevationLevel) {
    this.elevationLevel = elevationLevel;
    return this;
  }

  public Agent setX(int x) {
    this.x = x;
    return this;
  }

  public Agent setY(int y) {
    this.y = y;
    return this;
  }

  public Agent setSpeed(float speed) {
    this.speedSquared = Math.round(speed * speed);
    this.speed = speed;
    return this;
  }

  public Agent setHealth(int health) {
    this.healthShifted = health << 8;
    return this;
  }

  public int getHealth() {
    return healthShifted >> 8;
  }

  public Agent setMaxHealth(int maxHealth) {
    this.maxHealthShifted = maxHealth << 8;
    return this;
  }

  public Agent setShields(int shields) {
    this.shieldsShifted = shields << 8;
    return this;
  }

  public int getShields() {
    return shieldsShifted >> 8;
  }

  public Agent setMaxShields(int maxShields) {
    this.maxShieldsShifted = maxShields << 8;
    return this;
  }

  public Agent setCooldown(int cooldown) {
    this.cooldown = cooldown;
    return this;
  }

  public Agent setMaxCooldown(int maxCooldown) {
    this.maxCooldown = maxCooldown;
    return this;
  }

  public Agent setStopFrames(int stopFrames) {
    this.stopFrames = stopFrames;
    return this;
  }

  public Agent setRegeneratesHealth(boolean regeneratesHealth) {
    this.regeneratesHealth = regeneratesHealth;
    return this;
  }

  public Agent setSuicider(boolean suicider) {
    isSuicider = suicider;
    return this;
  }

  public Agent setRepairer(boolean repairer) {
    isRepairer = repairer;
    return this;
  }

  public Agent setMechanic(boolean mechanic) {
    this.isMechanic = mechanic;
    return this;
  }

  public Agent setHealer(boolean healer) {
    isHealer = healer;
    return this;
  }

  public Agent setFlyer(boolean flyer) {
    isFlyer = flyer;
    return this;
  }

  public Agent setOrganic(boolean organic) {
    isOrganic = organic;
    return this;
  }

  public Agent setSize(UnitSize size) {
    this.size = size;
    return this;
  }

  public Agent setOnDeathReplacer(Consumer<Collection<Agent>> onDeathReplacer) {
    this.onDeathReplacer = onDeathReplacer;
    return this;
  }

  public Agent setKiter(boolean kiter) {
    isKiter = kiter;
    return this;
  }

  public Agent setAirWeapon(Weapon airWeapon) {
    this.airWeapon = airWeapon;
    return this;
  }

  public Agent setGroundWeapon(Weapon groundWeapon) {
    this.groundWeapon = groundWeapon;
    return this;
  }

  final Weapon weaponVs(Agent other) {
    if (other.isFlyer) {
      return airWeapon;
    }
    return groundWeapon;
  }

  /**
   * Has to be called *after* max health has been set
   */
  public Agent setHpConstructionRate(int buildTime) {
    this.hpConstructionRate =
        max(1, (maxHealthShifted - maxHealthShifted / 10 + buildTime - 1) / buildTime);
    return this;
  }
}

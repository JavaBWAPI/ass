package org.bk.ass.sim;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public class Agent implements Cloneable {

  static final Consumer<UnitDeathContext> CARRIER_DEATH_HANDLER =
      context -> {
        Agent carrier = context.deadUnit;
        context.removeAgents(carrier.interceptors);
      };
  // Retrieved from OpenBW
  private static final int STIM_TIMER = 37;
  private static final int STIM_HEALTH_COST_SHIFTED = 10 << 8;
  private static final int ENSNARE_TIMER = 75;
  private static final int ENSNARE_ENERGY_COST_SHIFTED = 75 << 8;

  private final String name;
  TargetingPriority attackTargetPriority = TargetingPriority.HIGHEST;
  int armorShifted;
  int shieldUpgrades;
  Object userObject;

  int elevationLevel = -2;
  int x;
  int y;
  int nx;
  int ny;
  boolean speedUpgrade;
  float baseSpeed;
  int speedSquared;
  float speed;
  private float speedFactor = 1;
  private boolean scout;

  // Velocity (pixel per frame) for this frame to apply
  int vx;
  int vy;

  int healthShifted;
  int maxHealthShifted;
  boolean healedThisFrame;

  int stimTimer;
  int ensnareTimer;

  int hpConstructionRate;

  int shieldsShifted;
  int maxShieldsShifted;

  int energyShifted;
  int maxEnergyShifted;

  int attackCounter;

  int cooldown;
  boolean cooldownUpgrade;

  // Number of frames to sleep (ie. to prevent a move to break the attack)
  int sleepTimer;
  int stopFrames;
  boolean canStim;
  int plagueDamagePerFrameShifted;

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
  boolean protectedByDarkSwarm;
  boolean burrowed;
  boolean burrowedAttacker;
  // Visible to the other force
  boolean detected;

  int stasisTimer;
  // Lockdown or no damage and no movement

  UnitSize size;
  boolean isMelee;
  Weapon airWeapon;
  Weapon groundWeapon;
  boolean seekableTarget;
  int groundSeekRangeSquared = 0;

  Agent attackTarget;
  // Target for healing/repairing
  Agent restoreTarget;

  List<Agent> interceptors = Collections.emptyList();

  // Allow replacement of units on death (for example bunker -> marines)
  Consumer<UnitDeathContext> onDeathHandler = (ignored1) -> {
  };

  public Agent(String name) {
    this.name = name;
  }

  @Override
  public Agent clone() {
    try {
      Agent agent = (Agent) super.clone();
      agent.attackTarget = null;
      agent.restoreTarget = null;
      agent.interceptors = Collections.emptyList();
      return agent;
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
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

  public Agent setStimTimer(int stimTimer) {
    this.stimTimer = stimTimer;
    return this;
  }

  /**
   * Set the unit to sleep for some frames, it can still be attacked but is essentially "locked
   * down" in that time. <em>Note: This will override lockdown.</em>
   */
  public Agent setSleepTimer(int sleepTimer) {
    this.sleepTimer = sleepTimer;
    return this;
  }

  public int getSleepTimer() {
    return sleepTimer;
  }

  public Agent setEnsnareTimer(int ensnareTimer) {
    this.ensnareTimer = ensnareTimer;
    return this;
  }

  public Agent setStasisTimer(int stasisTimer) {
    this.stasisTimer = stasisTimer;
    return this;
  }

  final boolean isStasised() {
    return stasisTimer > 0;
  }

  /**
   * Marks this agent as being under lockdown. This is an alias for {@link #setPassive(boolean)}.
   *
   * @see #setPassive(boolean)
   */
  public Agent setLockDownTimer(int lockDownTimer) {
    sleepTimer = max(sleepTimer, lockDownTimer);
    return this;
  }

  /**
   * Makes this agent passive in a simulation. It will not damage other units or move. It can still
   * be attacked and destroyed.
   */
  public Agent setPassive(boolean passive) {
    sleepTimer = passive ? Integer.MAX_VALUE : sleepTimer;
    return this;
  }

  @Override
  public String toString() {
    return name + " (" + x + ", " + y + "), hp: " + getHealth() + ", sh: " + getShields();
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
    this.nx = x;
    return this;
  }

  public Agent setY(int y) {
    this.ny = y;
    return this;
  }

  public Agent setBaseSpeed(float speed) {
    this.baseSpeed = speed;
    return this;
  }

  public Agent setSpeedUpgrade(boolean speedUpgrade) {
    this.speedUpgrade = speedUpgrade;
    return this;
  }

  /**
   * Multiply the calculated speed by this factor. A potential use is to slow down units running
   * away a bit to compensate for obstacles.
   */
  public Agent setSpeedFactor(float speedFactor) {
    this.speedFactor = speedFactor;
    return this;
  }

  public Agent setCooldownUpgrade(boolean cooldownUpgrade) {
    this.cooldownUpgrade = cooldownUpgrade;
    return this;
  }

  void updateSpeed() {
    speed = baseSpeed;
    int mod = 0;
    if (stimTimer > 0) mod++;
    if (speedUpgrade) mod++;
    if (ensnareTimer > 0) mod--;
    if (mod < 0) speed /= 2f;
    if (mod > 0) {
      if (scout) {
        speed = 6 + 2 / 3f;
      } else {
        speed *= 1.5f;
        float minSpeed = 3 + 1 / 3f;
        if (speed < minSpeed) {
          speed = minSpeed;
        }
      }
    }
    speed *= speedFactor;
    this.speedSquared = Math.round(speed * speed);
  }

  public Agent setHealth(int health) {
    this.healthShifted = health << 8;
    return this;
  }

  public int getHealth() {
    return min(healthShifted, maxHealthShifted) >> 8;
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
    return min(shieldsShifted, maxShieldsShifted) >> 8;
  }

  public Agent setMaxShields(int maxShields) {
    this.maxShieldsShifted = maxShields << 8;
    return this;
  }

  public Agent setCooldown(int cooldown) {
    this.cooldown = cooldown;
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

  public Agent setOnDeathHandler(Consumer<UnitDeathContext> onDeathHandler) {
    this.onDeathHandler = onDeathHandler;
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

  /** Returns the number of times this agent has started an attack in simulations. */
  public int getAttackCounter() {
    return attackCounter;
  }

  public void resetAttackCounter() {
    attackCounter = 0;
  }

  /**
   * Sets the agents target to attack. Might be overridden by behavior. Ie. if the target is out of
   * range in a simulation frame.
   */
  public void setAttackTarget(Agent attackTarget) {
    this.attackTarget = attackTarget;
  }

  public void setRestoreTarget(Agent restoreTarget) {
    this.restoreTarget = restoreTarget;
  }

  /** Set this carriers interceptors. These will be killed as well, if the carrier dies. */
  public void setInterceptors(Collection<Agent> childAgents) {
    this.interceptors = new ArrayList<>(childAgents);
  }

  /**
   * Change the priority of this agent when other agents try to find a new target. For best
   * performance keep most agents on {@link TargetingPriority#HIGHEST}.
   *
   * <p>Interceptors are assigned a low priority by default, the rest defaults to {@link
   * TargetingPriority#HIGHEST}.
   */
  public Agent setAttackTargetPriority(TargetingPriority attackTargetPriority) {
    this.attackTargetPriority = attackTargetPriority;
    return this;
  }

  /** Mark this agent as being protected by dark swarm (ground unit and under dark swarm). */
  public Agent setProtectedByDarkSwarm(boolean protectedByDarkSwarm) {
    this.protectedByDarkSwarm = protectedByDarkSwarm;
    return this;
  }

  final Weapon weaponVs(Agent other) {
    if (other.isFlyer) {
      return airWeapon;
    }
    return groundWeapon;
  }

  /** Has to be called *after* max health has been set */
  public Agent setHpConstructionRate(int buildTime) {
    if (buildTime == 0) {
      return this;
    }
    this.hpConstructionRate =
        max(1, (maxHealthShifted - maxHealthShifted / 10 + buildTime - 1) / buildTime);
    return this;
  }

  public Agent setPlagueDamage(int plagueDamage) {
    this.plagueDamagePerFrameShifted = (plagueDamage << 8) / 76;
    return this;
  }

  public final void consumeEnergy(int amountShifted) {
    energyShifted = min(energyShifted, maxEnergyShifted) - amountShifted;
  }

  public final void consumeHealth(int amountShifted) {
    healthShifted = min(healthShifted, maxHealthShifted) - amountShifted;
  }

  public Agent setScout(boolean scout) {
    this.scout = scout;
    return this;
  }

  public Agent setGroundSeekRange(int range) {
    this.groundSeekRangeSquared = range * range;
    return this;
  }

  public Agent setSeekableTarget(boolean seekableTarget) {
    this.seekableTarget = seekableTarget;
    return this;
  }

  public boolean isMovable() {
    return baseSpeed > 0;
  }

  public final void heal(int amountShifted) {
    healthShifted += amountShifted;
  }

  public final void stim() {
    stimTimer = STIM_TIMER;
    consumeHealth(STIM_HEALTH_COST_SHIFTED);
  }

  public final void ensnare() {
    ensnareTimer = ENSNARE_TIMER;
    consumeEnergy(ENSNARE_ENERGY_COST_SHIFTED);
  }

  public enum TargetingPriority {
    LOW,
    MEDIUM,
    HIGHEST // Intentionally named "highest" as it also speeds up target selection
  }
}

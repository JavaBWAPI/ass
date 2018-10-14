package org.bk.ass;

import static java.lang.Math.max;
import static java.util.Arrays.asList;

import bwapi.Game;
import bwapi.Race;
import bwapi.Unit;
import bwapi.UnitSizeType;
import bwapi.UnitType;
import bwapi.WeaponType;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

public class BWMirrorAgentFactory {

  private static final Set<UnitType> SUICIDERS =
      new HashSet<>(
          asList(
              UnitType.Zerg_Scourge,
              UnitType.Zerg_Infested_Terran,
              UnitType.Terran_Vulture_Spider_Mine,
              UnitType.Protoss_Scarab));
  private static final Set<UnitType> KITERS =
      new HashSet<>(
          asList(
              UnitType.Terran_Marine, UnitType.Terran_Vulture,
              UnitType.Zerg_Mutalisk, UnitType.Protoss_Dragoon));
  private static Map<UnitType, Integer> stopFrames = new HashMap<>();

  private Consumer<Collection<Agent>> bunkerReplacer =
      agents -> {
        agents.add(of(UnitType.Terran_Marine, 0, 0));
        agents.add(of(UnitType.Terran_Marine, 0, 0));
        agents.add(of(UnitType.Terran_Marine, 0, 0));
        agents.add(of(UnitType.Terran_Marine, 0, 0));
      };

  static {
    asList(
        UnitType.Terran_Goliath,
        UnitType.Terran_Siege_Tank_Tank_Mode,
        UnitType.Terran_Siege_Tank_Siege_Mode,
        UnitType.Protoss_Reaver)
        .forEach(u -> stopFrames.put(u, 1));
    asList(UnitType.Terran_Ghost, UnitType.Zerg_Hydralisk).forEach(u -> stopFrames.put(u, 3));
    asList(UnitType.Protoss_Arbiter, UnitType.Zerg_Zergling).forEach(u -> stopFrames.put(u, 4));
    asList(UnitType.Protoss_Zealot, UnitType.Protoss_Dragoon).forEach(u -> stopFrames.put(u, 7));
    asList(
        UnitType.Terran_Marine,
        UnitType.Terran_Firebat,
        UnitType.Protoss_Corsair,
        UnitType.Terran_Bunker)
        .forEach(u -> stopFrames.put(u, 8));
    asList(UnitType.Protoss_Dark_Templar, UnitType.Zerg_Devourer)
        .forEach(u -> stopFrames.put(u, 9));
    stopFrames.put(UnitType.Zerg_Ultralisk, 14);
    stopFrames.put(UnitType.Protoss_Archon, 15);
    stopFrames.put(UnitType.Terran_Valkyrie, 40);
  }

  private final Game game;

  public BWMirrorAgentFactory(Game game) {
    this.game = game;
  }

  public BWMirrorAgentFactory() {
    this(null);
  }

  public Agent of(UnitType unitType, int groundWeaponUpgrades, int airWeaponUpgrades) {
    return fromUnitType(unitType, groundWeaponUpgrades, airWeaponUpgrades)
        .setHealth(unitType.maxHitPoints())
        .setShields(unitType.maxShields())
        .setEnergy(unitType.maxEnergy());
  }

  private Agent fromUnitType(UnitType unitType, int groundWeaponUpgrades, int airWeaponUpgrades) {
    int rangeExtension = 0;
    int hitsFactor = 1;
    WeaponType airWeapon = unitType.airWeapon();
    int maxAirHits = unitType.maxAirHits();
    WeaponType groundWeapon = unitType.groundWeapon();
    int maxGroundHits = unitType.maxGroundHits();
    if (unitType == UnitType.Terran_Bunker) {
      airWeapon = groundWeapon = UnitType.Terran_Marine.groundWeapon();
      maxAirHits = maxGroundHits = UnitType.Terran_Marine.maxAirHits();
      rangeExtension = 64;
      hitsFactor = 4;
    }

    Agent agent =
        new Agent(unitType.toString())
            .setFlyer(unitType.isFlyer())
            .setHealer(unitType == UnitType.Terran_Medic)
            .setMaxHealth(unitType.maxHitPoints())
            .setMaxCooldown(max(groundWeapon.damageCooldown(), airWeapon.damageCooldown()))
            .setAirWeapon(
                new Weapon()
                    .setMaxRange(airWeapon.maxRange() + rangeExtension)
                    .setMinRange(airWeapon.minRange())
                    .setDamage(damageOf(airWeapon, maxAirHits, airWeaponUpgrades) * hitsFactor)
                    .setDamageType(damageType(airWeapon.damageType())))
            .setGroundWeapon(
                new Weapon()
                    .setMaxRange(groundWeapon.maxRange() + rangeExtension)
                    .setMinRange(groundWeapon.minRange())
                    .setDamage(
                        damageOf(groundWeapon, maxGroundHits, groundWeaponUpgrades) * hitsFactor)
                    .setDamageType(damageType(groundWeapon.damageType())))
            .setMaxShields(unitType.maxShields())
            .setOrganic(unitType.isOrganic())
            .setRegeneratesHealth(
                unitType.getRace() == Race.Zerg
                    && unitType != UnitType.Zerg_Egg
                    && unitType != UnitType.Zerg_Lurker_Egg
                    && unitType != UnitType.Zerg_Larva)
            .setRegeneratesShields(unitType.getRace() == Race.Protoss)
            .setSuicider(SUICIDERS.contains(unitType))
            .setStopFrames(stopFrames.getOrDefault(unitType, 2))
            .setSize(size(unitType.size()))
            .setSpeed(unitType.topSpeed())
            .setArmor(unitType.armor())
            .setKiter(KITERS.contains(unitType))
            .setMaxEnergy(unitType.maxEnergy())
            .setDetected(true);
    if (unitType == UnitType.Terran_Bunker) {
      agent.setOnDeathReplacer(bunkerReplacer);
    }
    return agent;
  }

  public Agent of(Unit unit, int groundWeaponUpgrades, int airWeaponUpgrades) {
    int energy = 0;
    if (unit.getType().isSpellcaster()) {
      energy = unit.getEnergy();
    }
    return fromUnitType(unit.getType(), 0, 0)
        .setHealth(unit.getHitPoints())
        .setShields(unit.getShields())
        .setEnergy(energy)
        .setX(unit.getX())
        .setY(unit.getY())
        // Should be "adjusted" for own cloaked units
        .setDetected(unit.isDetected())
        // By default set unit as user object
        .setUserObject(unit);
  }

  public Agent of(Unit unit) {
    WeaponType airWeapon =
        unit.getType() != UnitType.Terran_Bunker
            ? unit.getType().airWeapon()
            : WeaponType.Gauss_Rifle;
    WeaponType groundWeapon =
        unit.getType() != UnitType.Terran_Bunker
            ? unit.getType().groundWeapon()
            : WeaponType.Gauss_Rifle;
    int groundWeaponUpgrades = unit.getPlayer().getUpgradeLevel(groundWeapon.upgradeType());
    int airWeaponUpgrades = unit.getPlayer().getUpgradeLevel(airWeapon.upgradeType());
    Agent agent = of(unit, groundWeaponUpgrades, airWeaponUpgrades);
    if (game != null) {
      agent.setElevationLevel(game.getGroundHeight(unit.getTilePosition()));
    }
    return agent;
  }

  private UnitSize size(UnitSizeType sizeType) {
    if (sizeType == UnitSizeType.Small) {
      return UnitSize.SMALL;
    }
    if (sizeType == UnitSizeType.Medium) {
      return UnitSize.MEDIUM;
    }
    if (sizeType == UnitSizeType.Large) {
      return UnitSize.LARGE;
    }
    return UnitSize.IRRELEVANT;
  }

  private DamageType damageType(bwapi.DamageType damageType) {
    if (damageType == bwapi.DamageType.Concussive) {
      return DamageType.CONCUSSIVE;
    }
    if (damageType == bwapi.DamageType.Explosive) {
      return DamageType.EXPLOSIVE;
    }
    return DamageType.IRRELEVANT;
  }

  private int damageOf(WeaponType weapon, int hits, int upgrades) {
    return (weapon.damageAmount() + weapon.damageBonus() * upgrades) * weapon.damageFactor() * hits;
  }
}

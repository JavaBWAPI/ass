package org.bk.ass;

import static java.lang.Math.max;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.function.Consumer;
import org.openbw.bwapi4j.BWMap;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.UnitSizeType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.SpellCaster;

public class BWAPI4JAgentFactory {

  private static final EnumSet<UnitType> SUICIDERS =
      EnumSet.of(
          UnitType.Zerg_Scourge,
          UnitType.Zerg_Infested_Terran,
          UnitType.Terran_Vulture_Spider_Mine,
          UnitType.Protoss_Scarab);
  private static final EnumSet<UnitType> KITERS =
      EnumSet.of(
          UnitType.Terran_Marine, UnitType.Terran_Vulture,
          UnitType.Zerg_Mutalisk, UnitType.Protoss_Dragoon);
  private static EnumMap<UnitType, Integer> stopFrames = new EnumMap<>(UnitType.class);

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

  private final BWMap map;

  public BWAPI4JAgentFactory(BWMap map) {
    this.map = map;
  }

  public BWAPI4JAgentFactory() {
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
        new Agent(unitType.name())
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

  public Agent of(PlayerUnit unit, int groundWeaponUpgrades, int airWeaponUpgrades) {
    int energy = 0;
    if (unit instanceof SpellCaster) {
      energy = ((SpellCaster) unit).getEnergy();
    }

    return fromUnitType(unit.getType(), groundWeaponUpgrades, airWeaponUpgrades)
        .setHealth(unit.getHitPoints())
        .setShields(unit.getShields())
        .setEnergy(energy)
        .setX(unit.getX())
        .setY(unit.getY())
        .setArmor(unit.getArmor())
        // Should be "adjusted" for own cloaked units
        .setDetected(unit.isDetected())
        // By default set unit as user object
        .setUserObject(unit);
  }

  public Agent of(PlayerUnit unit) {
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
    if (map != null && !unit.isFlying()) {
      agent.setElevationLevel(map.getGroundHeight(unit.getTilePosition()));
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

  private DamageType damageType(org.openbw.bwapi4j.type.DamageType damageType) {
    if (damageType == org.openbw.bwapi4j.type.DamageType.Concussive) {
      return DamageType.CONCUSSIVE;
    }
    if (damageType == org.openbw.bwapi4j.type.DamageType.Explosive) {
      return DamageType.EXPLOSIVE;
    }
    return DamageType.IRRELEVANT;
  }

  private int damageOf(WeaponType weapon, int hits, int upgrades) {
    return (weapon.damageAmount() + weapon.damageBonus() * upgrades) * weapon.damageFactor() * hits;
  }
}

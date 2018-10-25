package org.bk.ass;

import static java.lang.Math.max;
import static java.util.Arrays.asList;

import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.function.Consumer;
import org.openbw.bwapi4j.BWMap;
import org.openbw.bwapi4j.Player;
import org.openbw.bwapi4j.type.Race;
import org.openbw.bwapi4j.type.TechType;
import org.openbw.bwapi4j.type.UnitSizeType;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.type.UpgradeType;
import org.openbw.bwapi4j.type.WeaponType;
import org.openbw.bwapi4j.unit.Burrowable;
import org.openbw.bwapi4j.unit.Firebat;
import org.openbw.bwapi4j.unit.Marine;
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
        agents.add(of(UnitType.Terran_Marine));
        agents.add(of(UnitType.Terran_Marine));
        agents.add(of(UnitType.Terran_Marine));
        agents.add(of(UnitType.Terran_Marine));
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

  public Agent of(UnitType unitType) {
    return of(unitType, 0, 0, 0, 0, false, false);
  }

  public Agent of(
      UnitType unitType,
      int groundWeaponUpgrades,
      int airWeaponUpgrades,
      int groundWeaponRangeUpgrade,
      int airWeaponRangeUpgrade,
      boolean speedUpgrade,
      boolean energyUpgrade) {
    return fromUnitType(
        unitType,
        groundWeaponUpgrades,
        airWeaponUpgrades,
        groundWeaponRangeUpgrade,
        airWeaponRangeUpgrade,
        speedUpgrade,
        energyUpgrade)
        .setHealth(unitType.maxHitPoints())
        .setShields(unitType.maxShields())
        .setEnergy(unitType.maxEnergy());
  }

  private Agent fromUnitType(
      UnitType unitType,
      int groundWeaponUpgrades,
      int airWeaponUpgrades,
      int groundWeaponRangeUpgrade,
      int airWeaponRangeUpgrade,
      boolean speedUpgrade,
      boolean energyUpgrade) {
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

    float speed = (float) unitType.topSpeed();
    if (speedUpgrade) {
      if (unitType == UnitType.Protoss_Scout) {
        speed = 6 + 2 / 3f;
      } else {
        speed *= 1.5f;
        float minSpeed = 3 + 1 / 3f;
        if (speed < minSpeed) {
          speed = minSpeed;
        }
      }
    }

    Agent agent =
        new Agent(unitType.name())
            .setFlyer(unitType.isFlyer())
            .setHealer(unitType == UnitType.Terran_Medic)
            .setMaxHealth(unitType.maxHitPoints())
            .setMaxCooldown(max(groundWeapon.damageCooldown(), airWeapon.damageCooldown()))
            .setAirWeapon(
                weapon(
                    airWeaponUpgrades,
                    rangeExtension + airWeaponRangeUpgrade,
                    hitsFactor,
                    airWeapon,
                    maxAirHits))
            .setGroundWeapon(
                weapon(
                    groundWeaponUpgrades,
                    rangeExtension + groundWeaponRangeUpgrade,
                    hitsFactor,
                    groundWeapon,
                    maxGroundHits))
            .setMaxShields(unitType.maxShields())
            .setOrganic(unitType.isOrganic())
            .setRegeneratesHealth(
                unitType.getRace() == Race.Zerg
                    && unitType != UnitType.Zerg_Egg
                    && unitType != UnitType.Zerg_Lurker_Egg
                    && unitType != UnitType.Zerg_Larva)
            .setSuicider(SUICIDERS.contains(unitType))
            .setStopFrames(stopFrames.getOrDefault(unitType, 2))
            .setSize(size(unitType.size()))
            .setArmor(unitType.armor())
            .setKiter(KITERS.contains(unitType))
            .setMaxEnergy(unitType.maxEnergy() + (energyUpgrade ? 50 : 0))
            .setDetected(true)
            .setBurrowedAttacker(unitType == UnitType.Zerg_Lurker)
            .setSpeed(speed);
    if (unitType == UnitType.Terran_Bunker) {
      agent.setOnDeathReplacer(bunkerReplacer);
    }
    return agent;
  }

  private Weapon weapon(
      int weaponUpgrades, int rangeExtension, int hitsFactor, WeaponType weapon, int maxHits) {
    return new Weapon()
        .setMaxRange(weapon.maxRange() + rangeExtension)
        .setMinRange(weapon.minRange())
        .setDamage(damageOf(weapon, maxHits, weaponUpgrades) * hitsFactor)
        .setDamageType(damageType(weapon.damageType()))
        .setSplashType(splashType(weapon))
        .setInnerSplashRadius(weapon.innerSplashRadius())
        .setMedianSplashRadius(weapon.medianSplashRadius())
        .setOuterSplashRadius(weapon.medianSplashRadius());
  }

  public Agent of(
      PlayerUnit unit,
      int groundWeaponUpgrades,
      int airWeaponUpgrades,
      int groundWeaponRangeUpgrade,
      int airWeaponRangeUpgrade,
      boolean speedUpgrade,
      boolean energyUpgrade) {
    int energy = 0;
    if (unit instanceof SpellCaster) {
      energy = ((SpellCaster) unit).getEnergy();
    }

    return fromUnitType(
        unit.getType(),
        groundWeaponUpgrades,
        airWeaponUpgrades,
        groundWeaponRangeUpgrade,
        airWeaponRangeUpgrade,
        speedUpgrade,
        energyUpgrade)
        .setHealth(unit.getHitPoints())
        .setShields(unit.getShields())
        .setEnergy(energy)
        .setX(unit.getX())
        .setY(unit.getY())
        .setArmor(unit.getArmor())
        // Should be "adjusted" for own cloaked units
        .setDetected(unit.isDetected())
        // By default set unit as user object
        .setUserObject(unit)
        .setBurrowed(unit instanceof Burrowable && ((Burrowable) unit).isBurrowed());
  }

  public Agent of(PlayerUnit unit) {
    UnitType unitType = unit.getType();
    WeaponType airWeapon =
        unitType != UnitType.Terran_Bunker ? unitType.airWeapon() : WeaponType.Gauss_Rifle;
    WeaponType groundWeapon =
        unitType != UnitType.Terran_Bunker ? unitType.groundWeapon() : WeaponType.Gauss_Rifle;
    Player player = unit.getPlayer();
    int groundWeaponUpgrades = player.getUpgradeLevel(groundWeapon.upgradeType());
    int airWeaponUpgrades = player.getUpgradeLevel(airWeapon.upgradeType());
    int groundWeaponRangeUpgrade = rangeUpgrade(groundWeapon, player);
    int airWeaponRangeUpgrade = rangeUpgrade(airWeapon, player);

    Agent agent =
        of(
            unit,
            groundWeaponUpgrades,
            airWeaponUpgrades,
            groundWeaponRangeUpgrade,
            airWeaponRangeUpgrade,
            hasSpeedUpgrade(unitType, player),
            hasEnergyUpgrade(unitType, player));
    if (map != null && !unit.isFlying()) {
      agent.setElevationLevel(map.getGroundHeight(unit.getTilePosition()));
    }
    if (unitType == UnitType.Terran_Marine || unitType == UnitType.Terran_Firebat) {
      agent.setCanStim(player.hasResearched(TechType.Stim_Packs));
      if (unit instanceof Marine) {
        agent.setRemainingStimFrames(((Marine) unit).getStimTimer());
      } else {
        agent.setRemainingStimFrames(((Firebat) unit).getStimTimer());
      }
    }
    return agent;
  }

  private int rangeUpgrade(WeaponType weaponType, Player player) {
    if (weaponType == WeaponType.Gauss_Rifle
        && player.getUpgradeLevel(UpgradeType.U_238_Shells) > 0) {
      return 32;
    }
    if (weaponType == WeaponType.Needle_Spines
        && player.getUpgradeLevel(UpgradeType.Grooved_Spines) > 0) {
      return 32;
    }
    if (weaponType == WeaponType.Phase_Disruptor
        && player.getUpgradeLevel(UpgradeType.Singularity_Charge) > 0) {
      return 64;
    }
    if (weaponType == WeaponType.Hellfire_Missile_Pack
        && player.getUpgradeLevel(UpgradeType.Charon_Boosters) > 0) {
      return 96;
    }
    return 0;
  }

  private boolean hasEnergyUpgrade(UnitType unitType, Player player) {
    return unitType == UnitType.Zerg_Queen && player.getUpgradeLevel(UpgradeType.Gamete_Meiosis) > 0
        || unitType == UnitType.Zerg_Defiler
        && player.getUpgradeLevel(UpgradeType.Metasynaptic_Node) > 0
        || unitType == UnitType.Protoss_High_Templar
        && player.getUpgradeLevel(UpgradeType.Khaydarin_Amulet) > 0
        || unitType == UnitType.Protoss_Dark_Archon
        && player.getUpgradeLevel(UpgradeType.Argus_Talisman) > 0
        || unitType == UnitType.Protoss_Arbiter
        && player.getUpgradeLevel(UpgradeType.Khaydarin_Core) > 0
        || unitType == UnitType.Protoss_Corsair
        && player.getUpgradeLevel(UpgradeType.Argus_Jewel) > 0
        || unitType == UnitType.Terran_Wraith
        && player.getUpgradeLevel(UpgradeType.Apollo_Reactor) > 0
        || unitType == UnitType.Terran_Ghost
        && player.getUpgradeLevel(UpgradeType.Moebius_Reactor) > 0
        || unitType == UnitType.Terran_Battlecruiser
        && player.getUpgradeLevel(UpgradeType.Colossus_Reactor) > 0
        || unitType == UnitType.Terran_Science_Vessel
        && player.getUpgradeLevel(UpgradeType.Titan_Reactor) > 0
        || unitType == UnitType.Terran_Medic
        && player.getUpgradeLevel(UpgradeType.Caduceus_Reactor) > 0;
  }

  private boolean hasSpeedUpgrade(UnitType unitType, Player player) {
    return unitType == UnitType.Zerg_Zergling
        && player.getUpgradeLevel(UpgradeType.Metabolic_Boost) > 0
        || unitType == UnitType.Zerg_Hydralisk
        && player.getUpgradeLevel(UpgradeType.Muscular_Augments) > 0
        || unitType == UnitType.Zerg_Overlord
        && player.getUpgradeLevel(UpgradeType.Pneumatized_Carapace) > 0
        || unitType == UnitType.Zerg_Ultralisk
        && player.getUpgradeLevel(UpgradeType.Anabolic_Synthesis) > 0
        || unitType == UnitType.Protoss_Shuttle
        && player.getUpgradeLevel(UpgradeType.Gravitic_Thrusters) > 0
        || unitType == UnitType.Protoss_Observer
        && player.getUpgradeLevel(UpgradeType.Gravitic_Boosters) > 0
        || unitType == UnitType.Protoss_Zealot
        && player.getUpgradeLevel(UpgradeType.Leg_Enhancements) > 0
        || unitType == UnitType.Terran_Vulture
        && player.getUpgradeLevel(UpgradeType.Ion_Thrusters) > 0;
  }

  private SplashType splashType(WeaponType weaponType) {
    if (weaponType == WeaponType.Subterranean_Spines) {
      return SplashType.LINE_SPLASH;
    }
    if (weaponType == WeaponType.Glave_Wurm) {
      return SplashType.BOUNCE;
    }
    org.openbw.bwapi4j.type.ExplosionType explosionType = weaponType.explosionType();
    if (explosionType == org.openbw.bwapi4j.type.ExplosionType.Radial_Splash
        || explosionType == org.openbw.bwapi4j.type.ExplosionType.Enemy_Splash
        || explosionType == org.openbw.bwapi4j.type.ExplosionType.Nuclear_Missile) {
      return SplashType.RADIAL_SPLASH;
    }
    return SplashType.IRRELEVANT;
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

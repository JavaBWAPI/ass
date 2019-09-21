package org.bk.ass.sim;

import org.bk.ass.collection.UnorderedCollection;

import java.util.SplittableRandom;

import static java.lang.Math.*;

public class AgentUtil {
  private static final SplittableRandom rnd = new SplittableRandom();

  // Retrieved from OpenBW
  public static final int INTERCEPTOR_COOLDOWN = 45;
  public static final int REAVER_COOLDOWN = 60;


  private AgentUtil() {
    // Utility class
  }

  public static void moveToward(int frames, Agent agent, Agent target, float distance) {
    float travelled = frames * agent.speed;
    if (distance <= travelled) {
      agent.vx = target.x - agent.x;
      agent.vy = target.y - agent.y;
    } else {
      agent.vx = (int) ((target.x - agent.x) * travelled / distance);
      agent.vy = (int) ((target.y - agent.y) * travelled / distance);
    }
  }

  public static void moveAwayFrom(int frames, Agent agent, Agent target, float distance) {
    float travelled = frames * agent.speed;
    if (distance == 0) {
      double a = rnd.nextDouble(Math.PI * 2);
      agent.vx = (int) (cos(a) * travelled);
      agent.vy = (int) (sin(a) * travelled);
    } else {
      agent.vx = (int) ((agent.x - target.x) * travelled / distance);
      agent.vy = (int) ((agent.y - target.y) * travelled / distance);
    }
  }

  public static int distanceSquared(Agent a, Agent b) {
    return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
  }

  /** Deal splash damage to enemies and allies */
  public static void dealRadialSplashDamage(
      Weapon weapon,
      Agent mainTarget,
      UnorderedCollection<Agent> allies,
      UnorderedCollection<Agent> enemies) {
    for (int i = allies.size() - 1; i >= 0; i--) {
      Agent ally = allies.get(i);
      applySplashDamage(weapon, mainTarget, ally);
    }
    for (int i = enemies.size() - 1; i >= 0; i--) {
      Agent enemy = enemies.get(i);
      applySplashDamage(weapon, mainTarget, enemy);
    }
  }

  private static void applySplashDamage(Weapon weapon, Agent mainTarget, Agent splashTarget) {
    if (splashTarget == mainTarget || splashTarget.isFlyer != mainTarget.isFlyer) {
      return;
    }

    int distanceSquared = distanceSquared(splashTarget, mainTarget);
    if (distanceSquared <= weapon.innerSplashRadiusSquared) {
      applyDamage(splashTarget, weapon.damageType, weapon.damageShifted, weapon.hits);
    } else if (!splashTarget.burrowed) {
      if (distanceSquared <= weapon.medianSplashRadiusSquared) {
        applyDamage(splashTarget, weapon.damageType, weapon.damageShifted / 2, weapon.hits);
      } else if (distanceSquared <= weapon.outerSplashRadiusSquared) {
        applyDamage(splashTarget, weapon.damageType, weapon.damageShifted / 4, weapon.hits);
      }
    }
  }

  /** Deal splash damage to enemies only */
  public static void dealRadialSplashDamage(
      Weapon weapon, Agent mainTarget, UnorderedCollection<Agent> enemies) {
    for (int i = enemies.size() - 1; i >= 0; i--) {
      Agent enemy = enemies.get(i);
      applySplashDamage(weapon, mainTarget, enemy);
    }
  }

  public static void dealLineSplashDamage(
      Agent source, Weapon weapon, Agent mainTarget, UnorderedCollection<Agent> enemies) {
    int dx = mainTarget.x - source.x;
    int dy = mainTarget.y - source.y;
    // Same spot, chose "random" direction
    if (dx == 0 && dy == 0) {
      dx = 1;
    }
    int dxDistSq = dx * dx + dy * dy;
    int rangeWithSplashSquared =
        weapon.maxRangeSquared
            + 2 * weapon.maxRange * weapon.innerSplashRadius
            + weapon.innerSplashRadiusSquared;
    for (int i = enemies.size() - 1; i >= 0; i--) {
      Agent enemy = enemies.get(i);
      if (enemy == mainTarget || enemy.isFlyer != mainTarget.isFlyer) {
        continue;
      }
      int enemyDistSq = distanceSquared(enemy, source);
      if (enemyDistSq <= rangeWithSplashSquared) {
        int dot = (enemy.x - source.x) * dx + (enemy.y - source.y) * dy;
        if (dot >= 0) {
          int projdx = source.x + dot * dx / dxDistSq - enemy.x;
          int projdy = source.y + dot * dy / dxDistSq - enemy.y;
          int projDistSq = projdx * projdx + projdy * projdy;
          if (projDistSq <= weapon.innerSplashRadiusSquared) {
            applyDamage(enemy, weapon.damageType, weapon.damageShifted, weapon.hits);
          }
        }
      }
    }
  }

  public static void dealBounceDamage(
      Weapon weapon, Agent lastTarget, UnorderedCollection<Agent> enemies) {
    int remainingBounces = 2;
    int damage = weapon.damageShifted / 3;
    for (int i = enemies.size() - 1; i >= 0 && remainingBounces > 0; i--) {
      Agent enemy = enemies.get(i);
      if (enemy == lastTarget) {
        continue;
      }

      if (abs(enemy.x - lastTarget.x) <= 96 && abs(enemy.y - lastTarget.y) <= 96) {
        lastTarget = enemy;
        applyDamage(enemy, weapon.damageType, damage, weapon.hits);
        damage /= 3;
        remainingBounces--;
      }
    }
  }

  public static void dealDamage(Agent agent, Weapon wpn, Agent target) {
    int remainingDamage = wpn.damageShifted;

    if (!agent.isMelee) {
      // https://liquipedia.net/starcraft/Dark_Swarm
      if (target.protectedByDarkSwarm) return;

      // http://www.starcraftai.com/wiki/Chance_to_Hit
      if ((agent.elevationLevel >= 0 && agent.elevationLevel < target.elevationLevel)
          || (target.elevationLevel & 1) == 1) {
        remainingDamage = remainingDamage * 136 / 256;
      }
      remainingDamage = remainingDamage * 255 / 256;
    }

    applyDamage(target, wpn.damageType, remainingDamage, wpn.hits);
  }

  private static void applyDamage(Agent target, DamageType damageType, int damage, int hits) {
    int shields = min(target.maxShieldsShifted, target.shieldsShifted) - damage + target.shieldUpgrades;
    if (shields > 0) {
      target.shieldsShifted = shields;
      return;
    } else if (shields < 0) {
      damage = -shields;
      target.shieldsShifted = 0;
    }

    if (damage == 0) {
      return;
    }
    damage =
        reduceDamageByTargetSizeAndDamageType(
            target, damageType, damage - target.armorShifted * hits);

    target.consumeHealth(max(128, damage));
  }

  public static int reduceDamageByTargetSizeAndDamageType(
      Agent target, DamageType damageType, int damageShifted) {
    if (damageType == DamageType.CONCUSSIVE) {
      if (target.size == UnitSize.MEDIUM) {
        damageShifted /= 2;
      } else if (target.size == UnitSize.LARGE) {
        damageShifted /= 4;
      }
    } else if (damageType == DamageType.EXPLOSIVE) {
      if (target.size == UnitSize.SMALL) {
        damageShifted /= 2;
      } else if (target.size == UnitSize.MEDIUM) {
        damageShifted /= 4;
      }
    }
    return damageShifted;
  }

  public static void attack(Agent agent, Weapon selectedWeapon, Agent selectedEnemy, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
    dealDamage(agent, selectedWeapon, selectedEnemy);
    switch (selectedWeapon.splashType) {
      case BOUNCE:
        dealBounceDamage(selectedWeapon, selectedEnemy, enemies);
        break;
      case RADIAL_SPLASH:
        dealRadialSplashDamage(selectedWeapon, selectedEnemy, allies, enemies);
        break;
      case RADIAL_ENEMY_SPLASH:
        dealRadialSplashDamage(selectedWeapon, selectedEnemy, enemies);
        break;
      case LINE_SPLASH:
        dealLineSplashDamage(agent, selectedWeapon, selectedEnemy, enemies);
        break;
      default:
        // No splash
    }
    agent.cooldown = agent.maxCooldown;
    if (agent.remainingStimFrames > 0) {
      agent.cooldown /= 2;
    }
  }
}

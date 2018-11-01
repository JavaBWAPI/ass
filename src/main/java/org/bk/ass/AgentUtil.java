package org.bk.ass;

import static java.lang.Math.abs;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.SplittableRandom;

public class AgentUtil {

  private static final SplittableRandom rnd = new SplittableRandom();

  private AgentUtil() {
    // Utility class
  }

  public static void moveToward(Agent agent, Agent target, int distanceSquared) {
    if (distanceSquared <= agent.speedSquared) {
      agent.x = target.x;
      agent.y = target.y;
    } else {
      float distance = (float) sqrt(distanceSquared);
      agent.vx = (int) ((target.x - agent.x) * agent.speed / distance);
      agent.vy = (int) ((target.y - agent.y) * agent.speed / distance);
    }
  }

  public static void moveAwayFrom(Agent agent, Agent target, int distanceSquared) {
    if (distanceSquared == 0) {
      double a = rnd.nextDouble(Math.PI * 2);
      agent.vx = (int) (cos(a) * agent.speed);
      agent.vy = (int) (sin(a) * agent.speed);
    } else {
      float distance = (float) sqrt(distanceSquared);
      agent.vx = (int) ((agent.x - target.x) * agent.speed / distance);
      agent.vy = (int) ((agent.y - target.y) * agent.speed / distance);
    }
  }

  public static int distanceSquared(Agent a, Agent b) {
    return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
  }

  /**
   * Deal splash damage to enemies and allies
   */
  public static void dealRadialSplashDamage(
      Weapon weapon,
      Agent mainTarget,
      UnorderedCollection<Agent> allies,
      UnorderedCollection<Agent> enemies) {
    for (int i = 0; i < allies.size(); i++) {
      Agent ally = allies.get(i);
      applySplashDamage(weapon, mainTarget, ally);
    }
    for (int i = 0; i < enemies.size(); i++) {
      Agent enemy = enemies.get(i);
      applySplashDamage(weapon, mainTarget, enemy);
    }
  }

  private static void applySplashDamage(Weapon weapon, Agent mainTarget, Agent splashTarget) {
    if (splashTarget == mainTarget
        || splashTarget.burrowed
        || splashTarget.isFlyer != mainTarget.isFlyer) {
      return;
    }

    int distanceSquared = distanceSquared(splashTarget, mainTarget);
    if (distanceSquared <= weapon.outerSplashRadiusSquared) {
      if (distanceSquared <= weapon.medianSplashRadiusSquared) {
        if (distanceSquared <= weapon.innerSplashRadiusSquared) {
          applyDamage(splashTarget, weapon.damageType, weapon.damageShifted, weapon.hits);
        } else {
          applyDamage(splashTarget, weapon.damageType, weapon.damageShifted / 2, weapon.hits);
        }
      } else {
        applyDamage(splashTarget, weapon.damageType, weapon.damageShifted / 4, weapon.hits);
      }
    }
  }

  /**
   * Deal splash damage to enemies only
   */
  public static void dealRadialSplashDamage(
      Weapon weapon, Agent mainTarget, UnorderedCollection<Agent> enemies) {
    for (int i = 0; i < enemies.size(); i++) {
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
    for (int i = 0; i < enemies.size(); i++) {
      Agent enemy = enemies.get(i);
      if (enemy == mainTarget || enemy.burrowed || enemy.isFlyer != mainTarget.isFlyer) {
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
    for (int i = 0; i < enemies.size() && remainingBounces > 0; i++) {
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
    int shields = target.shieldsShifted - damage + target.shieldUpgrades;
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

    target.healthShifted -= max(128, damage);
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
}

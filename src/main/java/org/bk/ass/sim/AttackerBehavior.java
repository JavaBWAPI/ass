package org.bk.ass.sim;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;
import static org.bk.ass.sim.AgentUtil.applyDamage;
import static org.bk.ass.sim.AgentUtil.dealDamage;
import static org.bk.ass.sim.AgentUtil.distanceSquared;
import static org.bk.ass.sim.AgentUtil.moveAwayFrom;
import static org.bk.ass.sim.AgentUtil.moveToward;
import static org.bk.ass.sim.RetreatBehavior.simFlee;

import org.bk.ass.collection.UnorderedCollection;
import org.bk.ass.sim.Simulator.Behavior;

public class AttackerBehavior implements Behavior {

  @Override
  public boolean simUnit(
      int frameSkip,
      Agent agent,
      UnorderedCollection<Agent> allies,
      UnorderedCollection<Agent> enemies) {
    Agent selectedEnemy = null;
    Weapon selectedWeapon = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;

    if (agent.attackTarget != null && agent.attackTarget.healthShifted > 0) {
      int dstSq = distanceSquared(agent, agent.attackTarget);
      selectedWeapon = agent.weaponVs(agent.attackTarget);
      if (dstSq >= selectedWeapon.minRangeSquared && dstSq <= selectedWeapon.maxRangeSquared) {
        selectedEnemy = agent.attackTarget;
        selectedDistanceSquared = dstSq;
      }
    }

    if (selectedEnemy == null) {
      for (int i = enemies.size() - 1; i >= 0; i--) {
        Agent enemy = enemies.get(i);
        Weapon wpn = agent.weaponVs(enemy);
        int prioCmp =
            selectedEnemy == null
                ? 1
                : enemy.attackTargetPriority.compareTo(selectedEnemy.attackTargetPriority);
        if (enemy.healthShifted > 0
            && wpn.damageShifted != 0
            && enemy.detected
            && !enemy.isStasised()
            && prioCmp >= 0) {
          int distanceSq = distanceSquared(agent, enemy);
          if (distanceSq >= wpn.minRangeSquared
              && (distanceSq < selectedDistanceSquared || prioCmp > 0)) {
            selectedDistanceSquared = distanceSq;
            selectedEnemy = enemy;
            selectedWeapon = wpn;

            // If we can hit it this frame, we're done searching
            if (selectedDistanceSquared <= wpn.maxRangeSquared
                && enemy.attackTargetPriority == Agent.TargetingPriority.HIGHEST) {
              break;
            }
          }
        }
      }
    }
    agent.attackTarget = selectedEnemy;

    if (selectedEnemy == null) {
      return !agent.burrowed && simFlee(frameSkip, agent, enemies);
    }

    if (!agent.burrowed) {
      simCombatMove(frameSkip, agent, selectedEnemy, selectedDistanceSquared, selectedWeapon);
    }

    if (agent.burrowedAttacker != agent.burrowed) {
      return false;
    }

    if (agent.cooldown <= 0
        && selectedDistanceSquared <= selectedWeapon.maxRangeSquared) {
      simAttack(agent, allies, enemies, selectedEnemy, selectedWeapon);
    }

    return true;
  }

  private void simAttack(
      Agent agent,
      UnorderedCollection<Agent> allies,
      UnorderedCollection<Agent> enemies,
      Agent selectedEnemy,
      Weapon selectedWeapon) {
    if (agent.canStim
        && agent.stimTimer <= 0
        && agent.healthShifted >= agent.maxHealthShifted / 2) {
      agent.stim();
    }

    attack(agent, selectedWeapon, selectedEnemy, allies, enemies);
  }

  public static void attack(
      Agent agent,
      Weapon weapon,
      Agent selectedEnemy,
      UnorderedCollection<Agent> allies,
      UnorderedCollection<Agent> enemies) {
    agent.sleepTimer = agent.stopFrames;
    dealDamage(agent, weapon, selectedEnemy);
    switch (weapon.splashType) {
      case BOUNCE:
        dealBounceDamage(weapon, selectedEnemy, enemies);
        break;
      case RADIAL_SPLASH:
        dealRadialSplashDamage(weapon, selectedEnemy, allies, enemies);
        break;
      case RADIAL_ENEMY_SPLASH:
        dealRadialSplashDamage(weapon, selectedEnemy, enemies);
        break;
      case LINE_SPLASH:
        dealLineSplashDamage(agent, weapon, selectedEnemy, enemies);
        break;
      default:
        // No splash
    }
    agent.cooldown = weapon.cooldown;
    int mod = 0;
    if (agent.stimTimer > 0) mod++;
    if (agent.cooldownUpgrade) mod++;
    if (agent.ensnareTimer > 0) mod--;
    if (mod < 0) {
      agent.cooldown = max(5, agent.cooldown * 5 / 4);
    }
    if (mod > 0) {
      agent.cooldown /= 2;
    }
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
      if (enemy != lastTarget
          && enemy.healthShifted > 0
          && abs(enemy.x - lastTarget.x) <= 96
          && abs(enemy.y - lastTarget.y) <= 96) {
        lastTarget = enemy;
        applyDamage(enemy, weapon.damageType, damage, weapon.hits);
        damage /= 3;
        remainingBounces--;
      }
    }
  }

  private void simCombatMove(
      int frameSkip,
      Agent agent,
      Agent selectedEnemy,
      int selectedDistanceSquared,
      Weapon selectedWeapon) {
    boolean shouldKite =
        agent.isKiter
            && agent.cooldown > 0
            && selectedEnemy.weaponVs(agent).minRangeSquared <= selectedDistanceSquared
            && selectedEnemy.speed < agent.speed;
    float distance = (float) sqrt(selectedDistanceSquared);
    if (shouldKite) {
      if (distance + agent.speed * frameSkip <= selectedWeapon.maxRange) {
        moveAwayFrom(frameSkip, agent, selectedEnemy, distance);
      }
    } else {
      moveToward(frameSkip, agent, selectedEnemy, distance);
    }
  }
}

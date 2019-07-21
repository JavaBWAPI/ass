package org.bk.ass;

import org.bk.ass.Simulator.Behavior;
import org.bk.ass.collection.UnorderedCollection;

import static java.lang.Math.sqrt;
import static org.bk.ass.AgentUtil.*;
import static org.bk.ass.RetreatBehavior.simFlee;

public class AttackerBehavior implements Behavior {

  // Retrieved from OpenBW
  public static final int STIM_TIMER = 37;
  public static final int STIM_ENERGY_COST_SHIFTED = 10 << 8;

  @Override
  public boolean simUnit(
          Agent agent, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
    if (agent.cooldown > agent.maxCooldown - agent.stopFrames) {
      return true;
    }
    if (agent.isLockeddown || agent.isStasised) return false;

    Agent selectedEnemy = null;
    Weapon selectedWeapon = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;

    if (agent.lastEnemy != null && agent.lastEnemy.healthShifted >= 0) {
      int dstSq = distanceSquared(agent, agent.lastEnemy);
      selectedWeapon = agent.weaponVs(agent.lastEnemy);
      if (dstSq >= selectedWeapon.minRangeSquared && dstSq <= selectedWeapon.maxRangeSquared) {
        selectedEnemy = agent.lastEnemy;
        selectedDistanceSquared = dstSq;
      }
    }

    if (selectedEnemy == null) {
      for (int i = enemies.size() - 1; i >= 0; i--) {
        Agent enemy = enemies.get(i);
        Weapon wpn = agent.weaponVs(enemy);
          if (enemy.healthShifted >= 1
                  && wpn.damageShifted != 0
                  && enemy.detected
                  && !enemy.isStasised) {
          int distanceSquared = distanceSquared(agent, enemy);
          if (distanceSquared >= wpn.minRangeSquared && distanceSquared < selectedDistanceSquared) {
            selectedDistanceSquared = distanceSquared;
            selectedEnemy = enemy;
            selectedWeapon = wpn;

            // If we can hit it this frame, we're done searching
            if (selectedDistanceSquared <= wpn.maxRangeSquared) {
              break;
            }
          }
        }
      }
    }
    agent.lastEnemy = selectedEnemy;

    if (selectedEnemy == null) {
      return !agent.burrowed && simFlee(agent, enemies);
    }

    if (!agent.burrowed) {
      simCombatMove(agent, selectedEnemy, selectedDistanceSquared, selectedWeapon);
    }

    if (agent.burrowedAttacker != agent.burrowed) {
      return false;
    }

    if (agent.cooldown == 0
            && selectedDistanceSquared
            <= Math.max(Simulator.MIN_SIMULATION_RANGE, selectedWeapon.maxRangeSquared)) {
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
        && agent.remainingStimFrames == 0
        && agent.healthShifted >= agent.maxHealthShifted / 2) {
      agent.remainingStimFrames = STIM_TIMER;
      agent.healthShifted -= STIM_ENERGY_COST_SHIFTED;
    }
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

  private void simCombatMove(
      Agent agent, Agent selectedEnemy, int selectedDistanceSquared, Weapon selectedWeapon) {
    boolean shouldKite =
        agent.isKiter
            && agent.cooldown > 0
            && selectedEnemy.weaponVs(agent).minRangeSquared <= selectedDistanceSquared
            && selectedEnemy.speed < agent.speed;
    if (shouldKite) {
      double distance = sqrt(selectedDistanceSquared);
      if (distance + agent.speed < selectedWeapon.maxRange) {
        moveAwayFrom(agent, selectedEnemy, selectedDistanceSquared);
      }
    } else {
      moveToward(agent, selectedEnemy, selectedDistanceSquared);
    }
  }
}

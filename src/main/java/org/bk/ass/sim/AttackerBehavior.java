package org.bk.ass.sim;

import org.bk.ass.collection.UnorderedCollection;
import org.bk.ass.sim.Simulator.Behavior;

import static java.lang.Math.sqrt;
import static org.bk.ass.sim.AgentUtil.*;
import static org.bk.ass.sim.RetreatBehavior.simFlee;

public class AttackerBehavior implements Behavior {

  @Override
  public boolean simUnit(
      int frameSkip,
      Agent agent,
      UnorderedCollection<Agent> allies,
      UnorderedCollection<Agent> enemies) {
    if (agent.cooldown > agent.maxCooldown - agent.stopFrames) {
      return true;
    }

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
            && !enemy.isStasised
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
        && agent.remainingStimFrames <= 0
        && agent.healthShifted >= agent.maxHealthShifted / 2) {
      agent.stim();
    }

    AgentUtil.attack(agent, selectedWeapon, selectedEnemy, allies, enemies);
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
      if (distance + agent.speed <= selectedWeapon.maxRange) {
        moveAwayFrom(frameSkip, agent, selectedEnemy, distance);
      }
    } else {
      moveToward(frameSkip, agent, selectedEnemy, distance);
    }
  }
}

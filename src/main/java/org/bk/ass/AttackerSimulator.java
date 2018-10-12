package org.bk.ass;

import static java.lang.Math.sqrt;
import static org.bk.ass.Util.dealDamage;
import static org.bk.ass.Util.distanceSquared;
import static org.bk.ass.Util.moveAwayFrom;
import static org.bk.ass.Util.moveToward;

public class AttackerSimulator {

  public boolean simUnit(Agent agent, UnorderedList<Agent> enemies) {
    if (agent.cooldown > agent.maxCooldown - agent.stopFrames) {
      return true;
    }

    Agent selectedEnemy = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;
    for (int i = 0; i < enemies.size(); i++) {
      Agent enemy = enemies.get(i);
      Weapon wpn = agent.weaponVs(enemy);
      if (enemy.healthShifted < 1 || wpn.damageShifted == 0 || !enemy.detected) {
        continue;
      }
      int distance = distanceSquared(agent, enemy);
      if (distance < wpn.minRangeSquared) {
        continue;
      }

      if (distance < selectedDistanceSquared) {
        selectedDistanceSquared = distance;
        selectedEnemy = enemy;

        // If we can hit it this frame, we're done searching
        if (selectedDistanceSquared <= wpn.maxRangeSquared) {
          break;
        }
      }
    }

    if (selectedEnemy == null) {
      return simFlee(agent, enemies);
    }

    boolean shouldKite =
        agent.isKiter
            && agent.cooldown > 0
            && selectedEnemy.weaponVs(agent).minRangeSquared <= selectedDistanceSquared;
    Weapon wpn = agent.weaponVs(selectedEnemy);
    if (shouldKite) {
      double distance = sqrt(selectedDistanceSquared);
      if (distance + agent.speed < wpn.maxRange) {
        moveAwayFrom(agent, selectedEnemy, selectedDistanceSquared);
      }
    } else {
      moveToward(agent, selectedEnemy, selectedDistanceSquared);
    }

    if (agent.cooldown == 0 && selectedDistanceSquared <= wpn.maxRangeSquared) {
      dealDamage(agent, wpn, selectedEnemy);
      agent.cooldown = agent.maxCooldown;
    }

    return true;
  }

  private boolean simFlee(Agent agent, UnorderedList<Agent> enemies) {
    Agent selectedEnemy = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;
    for (int i = 0; i < enemies.size(); i++) {
      Agent enemy = enemies.get(i);
      Weapon wpn = enemy.weaponVs(agent);
      if (wpn.damageShifted == 0) {
        continue;
      }
      int distance = distanceSquared(agent, enemy);
      if (distance < wpn.minRangeSquared) {
        continue;
      }

      if (distance < selectedDistanceSquared) {
        selectedDistanceSquared = distance;
        selectedEnemy = enemy;

        // If we can hit it this frame, we're done searching
        if (selectedDistanceSquared <= wpn.maxRangeSquared) {
          break;
        }
      }
    }
    if (selectedEnemy == null) {
      return false;
    }
    moveAwayFrom(agent, selectedEnemy, selectedDistanceSquared);
    return true;
  }
}

package org.bk.ass;

import static java.lang.Math.sqrt;
import static org.bk.ass.Util.dealDamage;
import static org.bk.ass.Util.dealSplashDamage;
import static org.bk.ass.Util.distanceSquared;
import static org.bk.ass.Util.moveAwayFrom;
import static org.bk.ass.Util.moveToward;

public class AttackerSimulator {

  // Retrieved from OpenBW
  public static final int STIM_TIMER = 37;
  public static final int STIM_ENERGY_COST_SHIFTED = 10 << 8;

  public boolean simUnit(Agent agent, UnorderedList<Agent> enemies) {
    if (agent.cooldown > agent.maxCooldown - agent.stopFrames) {
      return true;
    }

    Agent selectedEnemy = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;
    Weapon selectedWeapon = null;
    for (int i = 0; i < enemies.size(); i++) {
      Agent enemy = enemies.get(i);
      Weapon wpn = agent.weaponVs(enemy);
      if (enemy.healthShifted >= 1 && wpn.damageShifted != 0 && enemy.detected) {
        int distance = distanceSquared(agent, enemy);
        if (distance >= wpn.minRangeSquared && distance < selectedDistanceSquared) {
          selectedDistanceSquared = distance;
          selectedEnemy = enemy;
          selectedWeapon = wpn;

          // If we can hit it this frame, we're done searching
          if (selectedDistanceSquared <= wpn.maxRangeSquared) {
            break;
          }
        }
      }
    }

    if (!agent.burrowed) {
      if (selectedEnemy == null) {
        return simFlee(agent, enemies);
      }

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
    } else if (selectedEnemy == null) {
      return false;
    }

    if (agent.burrowedAttacker != agent.burrowed) {
      return false;
    }

    if (agent.cooldown == 0 && selectedDistanceSquared <= selectedWeapon.maxRangeSquared) {
      if (agent.canStim
          && agent.remainingStimFrames == 0
          && agent.healthShifted >= agent.maxHealthShifted / 2) {
        agent.remainingStimFrames = STIM_TIMER;
        agent.healthShifted -= STIM_ENERGY_COST_SHIFTED;
      }
      dealDamage(agent, selectedWeapon, selectedEnemy);
      if (selectedWeapon.explosionType != ExplosionType.IRRELEVANT) {
        dealSplashDamage(selectedWeapon, selectedEnemy, enemies);
      }
      agent.cooldown = agent.maxCooldown;
      if (agent.remainingStimFrames > 0) {
        agent.cooldown /= 2;
      }
    }

    return true;
  }

  private boolean simFlee(Agent agent, UnorderedList<Agent> enemies) {
    Agent selectedEnemy = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;
    for (int i = 0; i < enemies.size(); i++) {
      Agent enemy = enemies.get(i);
      Weapon wpn = enemy.weaponVs(agent);
      if (wpn.damageShifted != 0) {
        int distance = distanceSquared(agent, enemy);
        if (distance >= wpn.minRangeSquared && distance < selectedDistanceSquared) {
          selectedDistanceSquared = distance;
          selectedEnemy = enemy;

          // If we can hit it this frame, we're done searching
          if (selectedDistanceSquared <= wpn.maxRangeSquared) {
            break;
          }
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

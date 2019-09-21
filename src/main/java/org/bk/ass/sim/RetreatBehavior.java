package org.bk.ass.sim;

import org.bk.ass.collection.UnorderedCollection;
import org.bk.ass.sim.Simulator.Behavior;

import static java.lang.Math.sqrt;
import static org.bk.ass.sim.AgentUtil.distanceSquared;
import static org.bk.ass.sim.AgentUtil.moveAwayFrom;

public class RetreatBehavior implements Behavior {

  @Override
  public boolean simUnit(
      Agent agent, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
    return simFlee(agent, enemies);
  }

  static boolean simFlee(Agent agent, UnorderedCollection<Agent> enemies) {
    if (agent.burrowed || agent.isStasised || agent.isLockeddown) return false;
    Agent selectedEnemy = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;
    for (int i = enemies.size() - 1; i >= 0; i--) {
      Agent enemy = enemies.get(i);
      Weapon wpn = enemy.weaponVs(agent);
      if (wpn.damageShifted != 0) {
        int distanceSq = distanceSquared(agent, enemy);
        if (distanceSq >= wpn.minRangeSquared && distanceSq < selectedDistanceSquared) {
          selectedDistanceSquared = distanceSq;
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
    moveAwayFrom(agent, selectedEnemy, (float) sqrt(selectedDistanceSquared));
    return true;
  }
}

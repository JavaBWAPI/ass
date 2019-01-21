package org.bk.ass;

import org.bk.ass.Simulator.Behavior;
import org.bk.ass.collection.UnorderedCollection;

import static org.bk.ass.AgentUtil.distanceSquared;
import static org.bk.ass.AgentUtil.moveAwayFrom;

public class RetreatBehavior implements Behavior {

  @Override
  public boolean simUnit(
          Agent agent, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
    return simFlee(agent, enemies);
  }

  static boolean simFlee(Agent agent, UnorderedCollection<Agent> enemies) {
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

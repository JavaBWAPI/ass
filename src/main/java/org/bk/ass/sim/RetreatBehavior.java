package org.bk.ass.sim;

import static java.lang.Math.sqrt;
import static org.bk.ass.sim.AgentUtil.distanceSquared;
import static org.bk.ass.sim.AgentUtil.moveAwayFrom;

import org.bk.ass.collection.UnorderedCollection;
import org.bk.ass.sim.Simulator.Behavior;

public class RetreatBehavior implements Behavior {

  @Override
  public boolean simUnit(
          int frameSkip, Agent agent, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
    return simFlee(frameSkip, agent, enemies);
  }

  static boolean simFlee(int frames, Agent agent, UnorderedCollection<Agent> enemies) {
    if (agent.burrowed) return false;
    Agent selectedEnemy = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;
    for (int i = enemies.size() - 1; i >= 0; i--) {
      Agent enemy = enemies.get(i);
      Weapon wpn = enemy.weaponVs(agent);
      // Enemy could be dead already, but skipping it generally doesn't make a difference and it will be gone next frame.
      if (wpn.damageShifted != 0) {
        int distanceSq = distanceSquared(agent, enemy);
        if (distanceSq >= wpn.minRangeSquared && distanceSq < selectedDistanceSquared) {
          selectedDistanceSquared = distanceSq;
          selectedEnemy = enemy;

          // If the enemy can hit us this frame, we're done searching
          if (selectedDistanceSquared <= wpn.maxRangeSquared) {
            break;
          }
        }
      }
    }
    if (selectedEnemy == null) {
      return false;
    }
    moveAwayFrom(frames, agent, selectedEnemy, (float) sqrt(selectedDistanceSquared));
    return true;
  }
}

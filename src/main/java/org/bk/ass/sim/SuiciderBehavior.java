package org.bk.ass.sim;

import static org.bk.ass.sim.AgentUtil.dealDamage;
import static org.bk.ass.sim.AgentUtil.distanceSquared;
import static org.bk.ass.sim.AgentUtil.moveToward;

import org.bk.ass.collection.UnorderedCollection;
import org.bk.ass.sim.Simulator.Behavior;

public class SuiciderBehavior implements Behavior {

  @Override
  public boolean simUnit(
      int frameSkip,
      Agent agent,
      UnorderedCollection<Agent> allies,
      UnorderedCollection<Agent> enemies) {
    Agent selectedEnemy = null;
    int selectedDistanceSquared =
        agent.groundSeekRangeSquared > 0 ? agent.groundSeekRangeSquared + 1 : Integer.MAX_VALUE;
    for (int i = enemies.size() - 1; i >= 0; i--) {
      Agent enemy = enemies.get(i);
      Weapon wpn = agent.weaponVs(enemy);
      if (enemy.healthShifted >= 1 && wpn.damageShifted != 0 && enemy.detected) {
        int distanceSq = distanceSquared(agent, enemy);
        if (distanceSq < selectedDistanceSquared
            && (agent.groundSeekRangeSquared == 0 || enemy.seekableTarget)) {
          selectedDistanceSquared = distanceSq;
          selectedEnemy = enemy;

          // If we can hit it this frame, we're done searching
          if (selectedDistanceSquared <= agent.speedSquared) {
            break;
          }
        }
      }
    }

    if (selectedEnemy == null) {
      return false;
    }

    moveToward(frameSkip, agent, selectedEnemy, (float) Math.sqrt(selectedDistanceSquared));

    if (selectedDistanceSquared <= agent.speedSquared) {
      dealDamage(agent, agent.weaponVs(selectedEnemy), selectedEnemy);
      agent.healthShifted = 0;
    }
    return true;
  }
}

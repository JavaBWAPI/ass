package org.bk.ass.sim;

import org.bk.ass.collection.UnorderedCollection;
import org.bk.ass.sim.Simulator.Behavior;

import static org.bk.ass.sim.AgentUtil.*;

public class SuiciderBehavior implements Behavior {

  @Override
  public boolean simUnit(
          int frameSkip, Agent agent, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
    // Don't check for lockdown - I believe there are no suiciders which can be locked down
    if (agent.isStasised) return false;
    Agent selectedEnemy = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;
    for (int i = enemies.size() - 1; i >= 0; i--) {
      Agent enemy = enemies.get(i);
      Weapon wpn = agent.weaponVs(enemy);
      if (enemy.healthShifted >= 1 && wpn.damageShifted != 0 && enemy.detected) {
        int distance = distanceSquared(agent, enemy);
        if (distance < selectedDistanceSquared) {
          selectedDistanceSquared = distance;
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

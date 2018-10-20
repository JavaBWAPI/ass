package org.bk.ass;

import static org.bk.ass.Util.dealDamage;
import static org.bk.ass.Util.distanceSquared;

public class SuiciderSimulator {

  public boolean simUnit(Agent agent, UnorderedList<Agent> enemies) {
    Agent selectedEnemy = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;
    for (int i = 0; i < enemies.size(); i++) {
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

    if (selectedDistanceSquared <= agent.speedSquared) {
      dealDamage(agent, agent.weaponVs(selectedEnemy), selectedEnemy);
      agent.healthShifted = 0;
    }
    return true;
  }
}

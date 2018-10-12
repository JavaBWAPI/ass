package org.bk.ass;

import static org.bk.ass.Util.distanceSquared;
import static org.bk.ass.Util.moveToward;

public class HealerSimulator {

  // Retrieved from OpenBW
  public static final int MEDICS_HEAL_RANGE = 30;

  public boolean simUnit(Agent agent, UnorderedList<Agent> allies) {
    Agent selectedAlly = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;
    for (int i = 0; i < allies.size(); i++) {
      Agent ally = allies.get(i);
      if (!ally.isOrganic || ally.healthShifted >= ally.maxHealthShifted || agent.healedThisFrame) {
        continue;
      }

      int distance = distanceSquared(agent, ally);
      if (distance < selectedDistanceSquared) {
        selectedDistanceSquared = distance;
        selectedAlly = ally;

        // If we can heal it this frame, we're done searching
        if (selectedDistanceSquared <= MEDICS_HEAL_RANGE) {
          break;
        }
      }
    }

    if (selectedAlly == null) {
      return false;
    }

    moveToward(agent, selectedAlly, selectedDistanceSquared);
    selectedAlly.healedThisFrame = true;
    selectedAlly.healthShifted += 150;
    if (selectedAlly.healthShifted > selectedAlly.maxHealthShifted) {
      selectedAlly.healthShifted = selectedAlly.maxHealthShifted;
    }

    return true;
  }
}

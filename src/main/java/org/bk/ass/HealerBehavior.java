package org.bk.ass;

import org.bk.ass.Simulator.Behavior;
import org.bk.ass.collection.UnorderedCollection;

import static org.bk.ass.AgentUtil.distanceSquared;
import static org.bk.ass.AgentUtil.moveToward;

public class HealerBehavior implements Behavior {

  // Retrieved from OpenBW
  public static final int MEDICS_HEAL_RANGE_SQUARED = 30 * 30;

  @Override
  public boolean simUnit(
      Agent agent, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
    if (agent.energyShifted < 256) {
      return true;
    }
    Agent selectedAlly = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;

    if (agent.lastAlly != null
        && !agent.lastAlly.healedThisFrame
        && agent.healthShifted < agent.maxHealthShifted) {
      int dstSq = distanceSquared(agent, agent.lastAlly);
      if (dstSq <= MEDICS_HEAL_RANGE_SQUARED) {
        selectedAlly = agent.lastAlly;
        selectedDistanceSquared = dstSq;
      }
    }

    if (selectedAlly == null) {
      for (int i = allies.size() - 1; i >= 0; i--) {
        Agent ally = allies.get(i);
        if (ally.isOrganic
            && !ally.isStasised
            && ally.healthShifted < ally.maxHealthShifted
            && !agent.healedThisFrame
            && ally != agent) {

          int distance = distanceSquared(agent, ally);
          if (distance < selectedDistanceSquared) {
            selectedDistanceSquared = distance;
            selectedAlly = ally;

            // If we can heal it this frame, we're done searching
            if (selectedDistanceSquared <= MEDICS_HEAL_RANGE_SQUARED) {
              break;
            }
          }
        }
      }
    }
    agent.lastAlly = selectedAlly;

    if (selectedAlly == null) {
      return false;
    }

    moveToward(agent, selectedAlly, selectedDistanceSquared);
    if (selectedDistanceSquared > MEDICS_HEAL_RANGE_SQUARED) {
      return true;
    }
    agent.energyShifted -= 256;
    selectedAlly.healedThisFrame = true;
    selectedAlly.healthShifted += 150;
    if (selectedAlly.healthShifted > selectedAlly.maxHealthShifted) {
      selectedAlly.healthShifted = selectedAlly.maxHealthShifted;
    }

    return true;
  }
}

package org.bk.ass.sim;

import org.bk.ass.collection.UnorderedCollection;
import org.bk.ass.sim.Simulator.Behavior;

import static java.lang.Math.min;
import static org.bk.ass.sim.AgentUtil.distanceSquared;
import static org.bk.ass.sim.AgentUtil.moveToward;

public class HealerBehavior implements Behavior {

  // Retrieved from OpenBW
  public static final int MEDICS_HEAL_RANGE_SQUARED = 30 * 30;

  @Override
  public boolean simUnit(
          int frameSkip, Agent agent, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
    if (agent.energyShifted < 256) {
      return true;
    }
    Agent selectedAlly = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;

    if (agent.restoreTarget != null
        && !agent.restoreTarget.healedThisFrame
        && agent.healthShifted < agent.maxHealthShifted) {
      int dstSq = distanceSquared(agent, agent.restoreTarget);
      if (dstSq <= MEDICS_HEAL_RANGE_SQUARED) {
        selectedAlly = agent.restoreTarget;
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

          int distanceSq = distanceSquared(agent, ally);
          if (distanceSq < selectedDistanceSquared) {
            selectedDistanceSquared = distanceSq;
            selectedAlly = ally;

            // If we can heal it this frame, we're done searching
            if (selectedDistanceSquared <= MEDICS_HEAL_RANGE_SQUARED) {
              break;
            }
          }
        }
      }
    }
    agent.restoreTarget = selectedAlly;

    if (selectedAlly == null) {
      return false;
    }

    moveToward(frameSkip, agent, selectedAlly, (float) Math.sqrt(selectedDistanceSquared));
    if (selectedDistanceSquared > MEDICS_HEAL_RANGE_SQUARED) {
      return true;
    }
    agent.energyShifted -= 256 * frameSkip;
    selectedAlly.healedThisFrame = true;
    selectedAlly.healthShifted = min(selectedAlly.maxHealthShifted, selectedAlly.healthShifted + 150 * frameSkip);

    return true;
  }
}

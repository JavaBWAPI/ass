package org.bk.ass.sim;

import org.bk.ass.collection.UnorderedCollection;
import org.bk.ass.sim.Simulator.Behavior;

import static java.lang.Math.min;
import static java.lang.Math.sqrt;
import static org.bk.ass.sim.AgentUtil.distanceSquared;
import static org.bk.ass.sim.AgentUtil.moveToward;

public class RepairerBehavior implements Behavior {

  // Retrieved from OpenBW
  public static final int SCV_REPAIR_RANGE_SQUARED = 5 * 5;

  @Override
  public boolean simUnit(
          int frameSkip, Agent agent, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
    Agent selectedAlly = null;
    int selectedDistanceSquared = Integer.MAX_VALUE;

    if (agent.restoreTarget != null && agent.healthShifted < agent.maxHealthShifted) {
      int dstSq = distanceSquared(agent, agent.restoreTarget);
      if (dstSq <= SCV_REPAIR_RANGE_SQUARED) {
        selectedAlly = agent.restoreTarget;
        selectedDistanceSquared = dstSq;
      }
    }

    if (selectedAlly == null) {
      for (int i = allies.size() - 1; i >= 0; i--) {
        Agent ally = allies.get(i);
        if (ally.isMechanic
            && !ally.isStasised
            && ally.healthShifted < ally.maxHealthShifted
            && ally != agent) {

          int distanceSq = distanceSquared(agent, ally);
          if (distanceSq < selectedDistanceSquared) {
            selectedDistanceSquared = distanceSq;
            selectedAlly = ally;

            // If we can repair it this frame, we're done searching
            if (selectedDistanceSquared <= SCV_REPAIR_RANGE_SQUARED) {
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

    moveToward(frameSkip, agent, selectedAlly, (float) sqrt(selectedDistanceSquared));
    if (selectedDistanceSquared > SCV_REPAIR_RANGE_SQUARED) {
      return true;
    }
    selectedAlly.healthShifted = min(selectedAlly.maxHealthShifted, selectedAlly.healthShifted + selectedAlly.hpConstructionRate * frameSkip);

    return true;
  }
}

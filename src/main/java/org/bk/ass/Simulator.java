package org.bk.ass;

import java.util.Collection;
import java.util.Collections;

public class Simulator {

  private final UnorderedList<Agent> playerA = new UnorderedList<>();
  private final UnorderedList<Agent> playerB = new UnorderedList<>();

  private final AttackerSimulator attackerSimulator;
  private final HealerSimulator healerSimulator;
  private final SuiciderSimulator suiciderSimulator;

  public Simulator() {
    attackerSimulator = new AttackerSimulator();
    healerSimulator = new HealerSimulator();
    suiciderSimulator = new SuiciderSimulator();
  }

  public Simulator addAgentA(Agent agent) {
    playerA.add(agent);
    return this;
  }

  public Simulator addAgentB(Agent agent) {
    playerB.add(agent);
    return this;
  }

  public Collection<Agent> getAgentsA() {
    return Collections.unmodifiableCollection(playerA);
  }

  public Collection<Agent> getAgentsB() {
    return Collections.unmodifiableCollection(playerB);
  }

  public int simulate() {
    return simulate(96);
  }

  public int simulate(int frames) {
    while (frames-- != 0 && !playerA.isEmpty() && !playerB.isEmpty()) {
      if (!step()) {
        break;
      }
    }
    playerA.clearReferences();
    playerB.clearReferences();
    return frames;
  }

  public void reset() {
    playerA.clear();
    playerB.clear();
  }

  /**
   * Simulate one frame.
   *
   * @return false, if nothing happened in this step and the sim can be aborted.
   */
  private boolean step() {
    boolean simRunning = false;
    for (int i = 0; i < playerA.size(); i++) {
      simRunning |= simUnit(playerA.get(i), playerA, playerB);
    }
    for (int i = 0; i < playerB.size(); i++) {
      simRunning |= simUnit(playerB.get(i), playerB, playerA);
    }
    removeDead(playerA);
    removeDead(playerB);
    updateStats(playerA);
    updateStats(playerB);
    return simRunning;
  }

  private void removeDead(UnorderedList<Agent> agents) {
    int i = 0;
    while (i < agents.size()) {
      if (agents.get(i).healthShifted < 1) {
        Agent agent = agents.removeAt(i);
        agent.onDeathReplacer.accept(agents);
      } else {
        i++;
      }
    }
  }

  private void updateStats(UnorderedList<Agent> agents) {
    for (int i = 0; i < agents.size(); i++) {
      Agent agent = agents.get(i);

      agent.x += agent.vx;
      agent.y += agent.vy;
      agent.vx = 0;
      agent.vy = 0;
      agent.healedThisFrame = false;

      if (agent.cooldown > 0) {
        agent.cooldown--;
      }
      if (agent.remainingStimFrames > 0) {
        agent.remainingStimFrames--;
      }
      if (agent.regeneratesHealth && agent.healthShifted < agent.maxHealthShifted) {
        agent.healthShifted += 4;
        if (agent.healthShifted > agent.maxHealthShifted) {
          agent.healthShifted = agent.maxHealthShifted;
        }
      }
      if (agent.regeneratesShields && agent.shieldsShifted < agent.maxShieldsShifted) {
        agent.shieldsShifted += 7;
        if (agent.shieldsShifted > agent.maxShieldsShifted) {
          agent.shieldsShifted = agent.maxShieldsShifted;
        }
      }
      agent.energyShifted += 8;
      if (agent.energyShifted > agent.maxEnergyShifted) {
        agent.energyShifted = agent.maxEnergyShifted;
      }
    }
  }

  private boolean simUnit(Agent agent, UnorderedList<Agent> allies, UnorderedList<Agent> enemies) {
    if (agent.isSuicider) {
      return suiciderSimulator.simUnit(agent, enemies);
    }
    if (agent.isHealer) {
      return healerSimulator.simUnit(agent, allies);
    }
    return attackerSimulator.simUnit(agent, enemies);
  }
}

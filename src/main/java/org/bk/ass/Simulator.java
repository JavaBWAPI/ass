package org.bk.ass;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

public class Simulator {

  private static final int MAX_MAP_DIMENSION = 8192;
  private static final int TILE_SIZE = 16;
  private static final int COLLISION_MAP_DIMENSION = MAX_MAP_DIMENSION / TILE_SIZE;
  private final UnorderedCollection<Agent> playerA = new UnorderedCollection<>();
  private final UnorderedCollection<Agent> playerB = new UnorderedCollection<>();

  private final AttackerSimulator attackerSimulator;
  private final HealerSimulator healerSimulator;
  private final RepairerSimulator repairerSimulator;
  private final SuiciderSimulator suiciderSimulator;

  private final byte[] collision = new byte[COLLISION_MAP_DIMENSION * COLLISION_MAP_DIMENSION];

  public Simulator() {
    attackerSimulator = new AttackerSimulator();
    healerSimulator = new HealerSimulator();
    repairerSimulator = new RepairerSimulator();
    suiciderSimulator = new SuiciderSimulator();
  }

  public Simulator addAgentA(Agent agent) {
    playerA.add(agent);
    if (!agent.isFlyer) {
      collision[colindex(agent.x, agent.y)]++;
    }
    return this;
  }

  public Simulator addAgentB(Agent agent) {
    playerB.add(agent);
    if (!agent.isFlyer) {
      collision[colindex(agent.x, agent.y)]++;
    }
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
    resetUnits();
    resetCollisionMap();
  }

  public void resetUnits() {
    playerA.clear();
    playerB.clear();
  }

  public void resetCollisionMap() {
    Arrays.fill(collision, (byte) 0);
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

  private void removeDead(UnorderedCollection<Agent> agents) {
    int i = 0;
    while (i < agents.size()) {
      if (agents.get(i).healthShifted < 1) {
        Agent agent = agents.removeAt(i);
        collision[colindex(agent.x, agent.y)]--;
        agent.onDeathReplacer.accept(agents);
      } else {
        i++;
      }
    }
  }

  private void updateStats(UnorderedCollection<Agent> agents) {
    for (int i = 0; i < agents.size(); i++) {
      Agent agent = agents.get(i);

      updatePosition(agent);
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
      if (agent.shieldsShifted < agent.maxShieldsShifted) {
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

  private void updatePosition(Agent agent) {
    int tx = agent.x + agent.vx;
    int ty = agent.y + agent.vy;
    if (tx < 0 || ty < 0 || tx >= MAX_MAP_DIMENSION || ty >= MAX_MAP_DIMENSION) {
      return;
    }

    if (!agent.isFlyer
        && (agent.x / TILE_SIZE != tx / TILE_SIZE || agent.y / TILE_SIZE != ty / TILE_SIZE)) {
      if (collision[colindex(tx, ty)] > TILE_SIZE / 8 - 1) {
        return;
      }
      collision[colindex(agent.x, agent.y)]--;
      collision[colindex(tx, ty)]++;
    }

    agent.x = tx;
    agent.y = ty;
  }

  private int colindex(int tx, int ty) {
    return ty / TILE_SIZE * COLLISION_MAP_DIMENSION + tx / TILE_SIZE;
  }

  private boolean simUnit(
      Agent agent, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
    if (agent.isSuicider) {
      return suiciderSimulator.simUnit(agent, enemies);
    }
    if (agent.isHealer) {
      return healerSimulator.simUnit(agent, allies);
    }
    if (agent.isRepairer && repairerSimulator.simUnit(agent, allies)) {
      return true;
      // Otherwise FIGHT, you puny SCV!
    }
    return attackerSimulator.simUnit(agent, allies, enemies);
  }
}

package org.bk.ass;

import org.bk.ass.collection.UnorderedCollection;

import java.util.Collection;
import java.util.Collections;

/**
 * Used to simulate 2 groups of agents engaging each other. Either use the default constructor which
 * initialized the default behavior or customize the behaviors. Ie. if you want to simulate one
 * group running away while the other uses the default behavior: <br>
 * <code>new Simulator(new {@link RetreatBehavior}(), new {@link RoleBasedBehavior}());</code> <br>
 * General usage guide: <br>
 *
 * <ol>
 *   <li>Create a new Simulator
 *   <li>On each frame, call <code>reset()</code> <em>once</em>
 *   <li>Before each simulation call <code>resetUnits()</code> before adding units
 * </ol>
 */
public class Simulator {

  private static final int MAX_MAP_DIMENSION = 8192;
  private static final int TILE_SIZE = 16;
  private static final int COLLISION_MAP_DIMENSION = MAX_MAP_DIMENSION / TILE_SIZE;
  private final UnorderedCollection<Agent> playerA = new UnorderedCollection<>();
  private final UnorderedCollection<Agent> playerB = new UnorderedCollection<>();

  private final byte[] collision = new byte[COLLISION_MAP_DIMENSION * COLLISION_MAP_DIMENSION];
  private final Behavior playerABehavior;
  private final Behavior playerBBehavior;

  public Simulator() {
    this(new RoleBasedBehavior(), new RoleBasedBehavior());
  }

  public Simulator(Behavior playerABehavior, Behavior playerBBehavior) {
    this.playerABehavior = playerABehavior;
    this.playerBBehavior = playerBBehavior;
  }

  public Simulator addAgentA(Agent agent) {
    checkBounds(agent);
    playerA.add(agent);
    if (!agent.isFlyer) {
      collision[colindex(agent.x, agent.y)]++;
    }
    return this;
  }

  public Simulator addAgentB(Agent agent) {
    checkBounds(agent);
    playerB.add(agent);
    if (!agent.isFlyer) {
      collision[colindex(agent.x, agent.y)]++;
    }
    return this;
  }

  private void checkBounds(Agent agent) {
    if (agent.x < 0 || agent.x >= 8192 || agent.y < 0 || agent.y >= 8192) {
      throw new PositionOutOfBoundsException(
          agent + " should be inside the map! This could be caused by an agent being fogged.");
    }
  }

  public Collection<Agent> getAgentsA() {
    return Collections.unmodifiableCollection(playerA);
  }

  public Collection<Agent> getAgentsB() {
    return Collections.unmodifiableCollection(playerB);
  }

  /**
   * Simulates 4 seconds into the future.
   */
  public int simulate() {
    return simulate(96);
  }

  /**
   * Simulate the given number of frames. If negative, simulation will only stop if one party has no
   * agents left. If units decide to run away, this could be an endless loop - use with care!
   */
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
    FastArrayFill.fillArray(collision, (byte) 0);
  }

  /**
   * Simulate one frame.
   *
   * @return false, if nothing happened in this step and the sim can be aborted.
   */
  private boolean step() {
    boolean simRunning = false;
    for (int i = 0; i < playerA.size(); i++) {
      simRunning |= playerABehavior.simUnit(playerA.get(i), playerA, playerB);
    }
    for (int i = 0; i < playerB.size(); i++) {
      simRunning |= playerBBehavior.simUnit(playerB.get(i), playerB, playerA);
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

  /** Dispatches behaviors based on the role in combat. */
  public static class RoleBasedBehavior implements Behavior {

    private final Behavior attackerSimulator;
    private final Behavior healerSimulator;
    private final Behavior repairerSimulator;
    private final Behavior suiciderSimulator;

    public RoleBasedBehavior(
        Behavior attackerSimulator,
        Behavior healerSimulator,
        Behavior repairerSimulator,
        Behavior suiciderSimulator) {
      this.attackerSimulator = attackerSimulator;
      this.healerSimulator = healerSimulator;
      this.repairerSimulator = repairerSimulator;
      this.suiciderSimulator = suiciderSimulator;
    }

    public RoleBasedBehavior() {
      this(
          new AttackerBehavior(),
          new HealerBehavior(),
          new RepairerBehavior(),
          new SuiciderBehavior());
    }

    @Override
    public boolean simUnit(
        Agent agent, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
      if (agent.isSuicider) {
        return suiciderSimulator.simUnit(agent, allies, enemies);
      }
      if (agent.isHealer) {
        return healerSimulator.simUnit(agent, allies, enemies);
      }
      if (agent.isRepairer && repairerSimulator.simUnit(agent, allies, enemies)) {
        return true;
        // Otherwise FIGHT, you puny SCV!
      }
      return attackerSimulator.simUnit(agent, allies, enemies);
    }
  }

  /**
   * Implementations define what action to take for an agent in regards to its allies and/or
   * enemies.
   */
  public interface Behavior {

    boolean simUnit(
        Agent agent, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies);
  }
}

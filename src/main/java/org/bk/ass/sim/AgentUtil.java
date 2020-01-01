package org.bk.ass.sim;

import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.sin;

import java.util.Collection;
import java.util.SplittableRandom;

public class AgentUtil {
  private static final SplittableRandom rnd = new SplittableRandom();

  // Retrieved from OpenBW
  public static final int INTERCEPTOR_COOLDOWN = 45;
  public static final int REAVER_COOLDOWN = 60;


  private AgentUtil() {
    // Utility class
  }

  public static void moveToward(int frames, Agent agent, Agent target, float distance) {
    agent.updateSpeed();
    float travelled = frames * agent.speed;
    if (distance <= travelled) {
      agent.vx = target.x - agent.x;
      agent.vy = target.y - agent.y;
    } else {
      agent.vx = (int) ((target.x - agent.x) * travelled / distance);
      agent.vy = (int) ((target.y - agent.y) * travelled / distance);
    }
  }

  public static void moveAwayFrom(int frames, Agent agent, Agent target, float distance) {
    agent.updateSpeed();
    float travelled = frames * agent.speed;
    if (distance == 0) {
      double a = rnd.nextDouble(Math.PI * 2);
      agent.vx = (int) (cos(a) * travelled);
      agent.vy = (int) (sin(a) * travelled);
    } else {
      agent.vx = (int) ((agent.x - target.x) * travelled / distance);
      agent.vy = (int) ((agent.y - target.y) * travelled / distance);
    }
  }

  public static int distanceSquared(Agent a, Agent b) {
    return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
  }

  public static void dealDamage(Agent agent, Weapon wpn, Agent target) {
    int remainingDamage = wpn.damageShifted;

    if (!agent.isMelee) {
      // https://liquipedia.net/starcraft/Dark_Swarm
      if (target.protectedByDarkSwarm) return;

      // http://www.starcraftai.com/wiki/Chance_to_Hit
      if ((agent.elevationLevel >= 0 && agent.elevationLevel < target.elevationLevel)
          || (target.elevationLevel & 1) == 1) {
        remainingDamage = remainingDamage * 136 / 256;
      }
      remainingDamage = remainingDamage * 255 / 256;
    }

    agent.attackCounter++;
    applyDamage(target, wpn.damageType, remainingDamage, wpn.hits);
  }

  public static void applyDamage(Agent target, DamageType damageType, int damage, int hits) {
    int shields = min(target.maxShieldsShifted, target.shieldsShifted) - damage + target.shieldUpgrades;
    if (shields > 0) {
      target.shieldsShifted = shields;
      return;
    } else if (shields < 0) {
      damage = -shields;
      target.shieldsShifted = 0;
    }

    if (damage == 0) {
      return;
    }
    damage =
        reduceDamageByTargetSizeAndDamageType(
            target, damageType, damage - target.armorShifted * hits);

    target.consumeHealth(max(128, damage));
  }

  public static int reduceDamageByTargetSizeAndDamageType(
      Agent target, DamageType damageType, int damageShifted) {
    if (damageType == DamageType.CONCUSSIVE) {
      if (target.size == UnitSize.MEDIUM) {
        damageShifted /= 2;
      } else if (target.size == UnitSize.LARGE) {
        damageShifted /= 4;
      }
    } else if (damageType == DamageType.EXPLOSIVE) {
      if (target.size == UnitSize.SMALL) {
        damageShifted /= 2;
      } else if (target.size == UnitSize.MEDIUM) {
        damageShifted /= 4;
      }
    }
    return damageShifted;
  }

  /**
   * Sets random positions for the given agents within the given rectangle.
   * The positions are <em>stable</em>: Calling this with the same arguments twice will not change any position
   * on the second invocation. More precisely, any {@link Agent} with position i in the collection will always
   * get the same position. Be sure to use different rectangles for different {@link Agent} collections, to
   * prevent them from getting assigned the same positions.
   */
  public static void randomizePositions(Collection<Agent> agents, int ax, int ay, int bx, int by) {
    SplittableRandom posRnd = new SplittableRandom(1337L);
    for (Agent agent : agents) {
      agent.x = posRnd.nextInt(ax, bx + 1);
      agent.y = posRnd.nextInt(ay, by + 1);
    }
  }
}

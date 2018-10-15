package org.bk.ass;

import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import java.util.SplittableRandom;

public class Util {

  private static final SplittableRandom rnd = new SplittableRandom();

  private Util() {
    // Utility class
  }

  public static void moveToward(Agent agent, Agent target, int distanceSquared) {
    if (distanceSquared <= agent.speedSquared) {
      agent.x = target.x;
      agent.y = target.y;
    } else {
      double distance = sqrt(distanceSquared);
      agent.vx = (int) ((target.x - agent.x) * agent.speed / distance);
      agent.vy = (int) ((target.y - agent.y) * agent.speed / distance);
    }
  }

  public static void moveAwayFrom(Agent agent, Agent target, int distanceSquared) {
    if (distanceSquared == 0) {
      double a = rnd.nextDouble(Math.PI * 2);
      agent.vx = (int) (cos(a) * agent.speed);
      agent.vy = (int) (sin(a) * agent.speed);
    } else {
      double distance = sqrt(distanceSquared);
      agent.vx = (int) ((agent.x - target.x) * agent.speed / distance);
      agent.vy = (int) ((agent.y - target.y) * agent.speed / distance);
    }
  }

  public static int distanceSquared(Agent a, Agent b) {
    return (a.x - b.x) * (a.x - b.x) + (a.y - b.y) * (a.y - b.y);
  }

  public static void dealDamage(Agent agent, Weapon wpn, Agent target) {
    dealDamage(target, wpn.damageShifted, wpn.damageType, agent.elevationLevel);
  }

  public static void dealDamage(
      Agent target, int damageShifted, DamageType damageType, int attackerElevationLevel) {
    int remainingDamage = damageShifted;

    // http://www.starcraftai.com/wiki/Chance_to_Hit
    if ((attackerElevationLevel >= 0 && attackerElevationLevel < target.elevationLevel)
        || (target.elevationLevel & 1) == 1) {
      remainingDamage = remainingDamage * 136 / 256;
    }
    remainingDamage = remainingDamage * 255 / 256;

    int shields = target.shieldsShifted - remainingDamage + target.shieldUpgrades;
    if (shields > 0) {
      target.shieldsShifted = shields;
      return;
    } else if (shields < 0) {
      remainingDamage = -shields;
      target.shieldsShifted = 0;
    }

    if (remainingDamage == 0) {
      return;
    }
    remainingDamage = reduceDamageByTargetAndDamageType(target, damageType, remainingDamage);

    target.healthShifted -= max(128, remainingDamage);
  }

  public static int reduceDamageByTargetAndDamageType(
      Agent target, DamageType damageType, int damageShifted) {
    damageShifted -= target.armorShifted;

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
}

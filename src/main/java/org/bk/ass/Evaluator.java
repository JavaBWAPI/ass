package org.bk.ass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Used to get a rough guess for combat outcome. Doesn't provide as much detail as the {@link
 * Simulator}. Estimates [0-0.5) =&gt; not so good for force A. Estimates (0.5-1] =&gt; not bad for
 * force A. Avoid using it to estimate an active entanglement (as positioning might be completely
 * ignored).
 */
public class Evaluator {
  private static final double EPS = 1E-10;
  private final Parameters parameters;

  public Evaluator(Parameters parameters) {
    this.parameters = parameters;
  }

  public Evaluator() {
    this(new Parameters());
  }

  /**
   * @return A result in the [0..1] range: 0 if agents of A are obliterated, 1 if agents of B are
   *     obliterated.
   */
  public double evaluate(Collection<Agent> agentsA, Collection<Agent> agentsB) {
    List<Agent> finalAgentsA = new ArrayList<>();
    agentsA.forEach(a -> a.onDeathReplacer.accept(finalAgentsA));
    List<Agent> finalAgentsB = new ArrayList<>();
    agentsB.forEach(a -> a.onDeathReplacer.accept(finalAgentsB));
    finalAgentsA.addAll(agentsA);
    finalAgentsB.addAll(agentsB);
    int damageToA = new DamageBoard(finalAgentsB).sumDamageTo(finalAgentsA);
    int damageToB = new DamageBoard(finalAgentsA).sumDamageTo(finalAgentsB);
    int regenToA = regeneration(finalAgentsA);
    int regenToB = regeneration(finalAgentsB);
    damageToA -= regenToA;
    if (damageToA < 0) {
      damageToA = 0;
    }
    damageToB -= regenToB;
    if (damageToB < 0) {
      damageToB = 0;
    }
    double evalA =
        (double) damageToA
            / (finalAgentsA
            .stream()
            .mapToDouble(a -> a.getHealth() + a.getShields() * parameters.shieldScale)
            .sum()
            + EPS);
    double evalB =
        (double) damageToB
            / (finalAgentsB
            .stream()
            .mapToDouble(a -> a.getHealth() + a.getShields() * parameters.shieldScale)
            .sum()
            + EPS);
    // eval is a rough estimate on how many units where lost.
    // Directly comparing is bad since one might have lost more agents than he had.
    // So we just multiply with the enemy count and compare that instead.
    evalB *= finalAgentsA.size();
    evalA *= finalAgentsB.size();
    return (evalB + EPS / 2) / (evalA + evalB + EPS);
  }

  private int regeneration(Collection<Agent> agents) {
    // Subtract 1 to prevent counting selfheal
    int healables = (int) (agents.stream().filter(it -> it.isOrganic).count() - 1);
    return agents
        .stream()
        .mapToInt(
            a -> {
              int healed = 0;
              if (a.regeneratesHealth) {
                healed = parameters.healthRegen;
              }
              if (a.maxShieldsShifted > 0) {
                healed += parameters.shieldRegen;
              }
              if (a.isHealer) {
                healed += healables * parameters.heal;
              }
              return healed;
            })
        .sum();
  }

  private class DamageBoard {

    private int airDamageNormal;
    private int airConcussiveDamage;
    private int airExplosiveDamage;
    private int airHits;
    private int groundDamageNormal;
    private int groundConcussiveDamage;
    private int groundExplosiveDamage;
    private int groundHits;

    DamageBoard(Collection<Agent> attackers) {
      for (Agent agent : attackers) {
        sumAirDamage(agent);
        sumGroundDamage(agent);
      }
    }

    private void sumGroundDamage(Agent agent) {
      Weapon weapon = agent.groundWeapon;
      groundHits += weapon.hits;
      double damageToApply = calculateDamage(agent, weapon);
      if (weapon.damageType == DamageType.CONCUSSIVE) {
        groundConcussiveDamage += damageToApply;
      } else if (weapon.damageType == DamageType.EXPLOSIVE) {
        groundExplosiveDamage += damageToApply;
      } else {
        groundDamageNormal += damageToApply;
      }
    }

    private void sumAirDamage(Agent agent) {
      Weapon weapon = agent.groundWeapon;
      airHits += weapon.hits;
      double damageToApply = calculateDamage(agent, weapon);
      if (agent.airWeapon.damageType == DamageType.CONCUSSIVE) {
        airConcussiveDamage += damageToApply;
      } else if (agent.airWeapon.damageType == DamageType.EXPLOSIVE) {
        airExplosiveDamage += damageToApply;
      } else {
        airDamageNormal += damageToApply;
      }
    }

    private double calculateDamage(Agent attacker, Weapon weapon) {
      double rangeFactor = 1.0 + weapon.maxRange * parameters.rangeScale;
      double speedFactor = 1.0 + attacker.speed * parameters.speedScale;
      double radialSplashFactor =
          weapon.splashType == SplashType.RADIAL_SPLASH ? parameters.radialSplashFactor : 1.0;
      double lineSplashFactor =
          weapon.splashType == SplashType.LINE_SPLASH ? parameters.lineSplashFactor : 1.0;
      double bounceSplashFactor =
          weapon.splashType == SplashType.BOUNCE ? parameters.radialSplashFactor : 1.0;

      return weapon.damageShifted
          * rangeFactor
          * speedFactor
          * radialSplashFactor
          * lineSplashFactor
          * bounceSplashFactor
          / attacker.maxCooldown;
    }

    int sumDamageTo(Collection<Agent> targets) {
      int damageSum = 0;
      for (Agent target : targets) {
        if (!target.detected) {
          continue;
        }
        if (target.isFlyer) {
          int damage =
              AgentUtil.reduceDamageByTargetSizeAndDamageType(
                  target, DamageType.CONCUSSIVE, airConcussiveDamage);
          damage +=
              AgentUtil.reduceDamageByTargetSizeAndDamageType(
                  target, DamageType.EXPLOSIVE, airExplosiveDamage);
          damage += airDamageNormal;
          damageSum += Math.max(128 * airHits, damage - airHits * target.armorShifted);
        } else {
          int damage =
              AgentUtil.reduceDamageByTargetSizeAndDamageType(
                  target, DamageType.CONCUSSIVE, groundConcussiveDamage);
          damage +=
              AgentUtil.reduceDamageByTargetSizeAndDamageType(
                  target, DamageType.EXPLOSIVE, groundExplosiveDamage);
          damage += groundDamageNormal;
          damageSum += Math.max(128 * airHits, damage - groundHits * target.armorShifted);
        }
      }
      return damageSum;
    }
  }

  public static class Parameters {

    final double shieldScale;
    final double speedScale;
    final double rangeScale;
    final double radialSplashFactor;
    final double lineSplashFactor;
    final double bounceSplashFactor;
    final int heal;
    final int healthRegen;
    final int shieldRegen;

    public Parameters(double[] source) {
      shieldScale = source[0];
      speedScale = source[1];
      rangeScale = source[2];
      radialSplashFactor = source[3];
      lineSplashFactor = source[4];
      bounceSplashFactor = source[5];
      heal = (int) source[6];
      healthRegen = (int) source[7];
      shieldRegen = (int) source[8];
    }

    public Parameters() {
      this(
          new double[]{
              2.812625, 0.504625, 0.053, 1.2545, 2.27975, 1.86025, 976.7565, 513.109625, 586.87675
          });
    }
  }
}

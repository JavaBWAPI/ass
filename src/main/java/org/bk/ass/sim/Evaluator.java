package org.bk.ass.sim;

import static java.lang.Math.max;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.SplittableRandom;

/**
 * Used to get a rough guess for combat outcome. Doesn't provide as much detail as the {@link
 * Simulator}. Estimates [0-0.5) =&gt; not so good for force A. Estimates (0.5-1] =&gt; not bad for
 * force A. Avoid using it to estimate an active entanglement (as positioning might be completely
 * ignored).
 */
public class Evaluator {
  private static final double EPS = 1E-10;
  public static final EvaluationResult EVAL_NO_COMBAT =
      new EvaluationResult(0.5) {
        @Override
        public String toString() {
          return "EvaluationResult{NO COMBAT}";
        }
      };
  private final Parameters parameters;
  private SplittableRandom prng = new SplittableRandom();
  private EvaluatorDeathContext deathContext = new EvaluatorDeathContext();

  public Evaluator(Parameters parameters) {
    this.parameters = parameters;
  }

  public Evaluator() {
    this(new Parameters());
  }

  /**
   * @return A result in the [0..1] range: 0 if agents of A are obliterated, 1 if agents of B are
   *     obliterated. Exactly 0.5 means no damage will be done by either side.
   */
  public EvaluationResult evaluate(Collection<Agent> agentsA, Collection<Agent> agentsB) {
    List<Agent> finalAgentsA = new ArrayList<>();
    deathContext.target = finalAgentsA;
    agentsA.forEach(a -> {
      deathContext.deadUnit = a;
      a.onDeathHandler.accept(deathContext);
    });
    List<Agent> finalAgentsB = new ArrayList<>();
    deathContext.target = finalAgentsB;
    agentsB.forEach(a -> {
      deathContext.deadUnit = a;
      a.onDeathHandler.accept(deathContext);
    });
    finalAgentsA.addAll(agentsA);
    finalAgentsA.forEach(Agent::updateSpeed);
    finalAgentsB.addAll(agentsB);
    finalAgentsB.forEach(Agent::updateSpeed);
    double damageToA = new DamageBoard(finalAgentsB).sumDamageTo(finalAgentsA);
    double damageToB = new DamageBoard(finalAgentsA).sumDamageTo(finalAgentsB);

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
        finalAgentsA.stream()
                .filter(it -> it.groundWeapon.damageShifted > 0 || it.airWeapon.damageShifted > 0)
                .mapToDouble(a -> a.getHealth() + a.getShields() * parameters.shieldScale)
                .sum()
            * damageToB;
    double evalB =
        finalAgentsB.stream()
                .filter(it -> it.groundWeapon.damageShifted > 0 || it.airWeapon.damageShifted > 0)
                .mapToDouble(a -> a.getHealth() + a.getShields() * parameters.shieldScale)
                .sum()
            * damageToA;
    if (evalA == 0 && evalB == 0) {
      return EVAL_NO_COMBAT;
    }

    // eval is a rough factor on how many units survived
    // Since we summed damages above we'll multiply by unit counts to even the odds
    double skewA;
    double skewB;
    double skew = prng.nextDouble(0.00001, 0.00002);
    if (prng.nextBoolean()) {
      skewA = skew;
      skewB = 0;
    } else {
      skewA = 0;
      skewB = skew;
    }
    evalA *= finalAgentsA.size() + skewA;
    evalB *= finalAgentsB.size() + skewB;
    return new EvaluationResult((evalA + EPS) / (evalA + evalB + 2 * EPS));
  }

  public EvalWithAgents optimizeEval(Collection<Agent> agentsA, Collection<Agent> agentsB) {
    EvaluationResult evalToBeat = evaluate(agentsA, agentsB);
    List<Agent> agentsToBeat = new ArrayList<>(agentsA);
    for (Agent a : agentsA) {
      agentsToBeat.remove(a);
      EvaluationResult eval = evaluate(agentsToBeat, agentsB);
      if (eval.value > evalToBeat.value
          || eval.value == evalToBeat.value && evalToBeat == EVAL_NO_COMBAT) {
        evalToBeat = eval;
      } else {
        agentsToBeat.add(a);
      }
    }

    return new EvalWithAgents(evalToBeat.value, agentsToBeat);
  }

  private int regeneration(Collection<Agent> agents) {
    // Subtract 1 to prevent counting selfheal
    int healables = (int) (agents.stream().filter(it -> it.isOrganic).count() - 1);
    return agents.stream()
        .mapToInt(
            a -> {
              int healed = 0;
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
    private int airConcussiveHits;
    private int airExplosiveHits;
    private int airNormalHits;
    private int groundDamageNormal;
    private int groundConcussiveDamage;
    private int groundExplosiveDamage;
    private int groundConcussiveHits;
    private int groundExplosiveHits;
    private int groundNormalHits;
    private int groundSeekingExplosiveHits;
    private int groundSeekingExplosiveDamage;

    DamageBoard(Collection<Agent> attackers) {
      for (Agent agent : attackers) {
        sumAirDamage(agent);
        sumGroundDamage(agent);
      }
    }

    private void sumGroundDamage(Agent agent) {
      Weapon weapon = agent.groundWeapon;
      double damageToApply = calculateDamage(agent, weapon);
      if (weapon.damageType == DamageType.CONCUSSIVE) {
        groundConcussiveHits += weapon.hits;
        groundConcussiveDamage += (int) damageToApply;
      } else if (weapon.damageType == DamageType.EXPLOSIVE) {
        if (agent.groundSeekRangeSquared == 0) {
          groundExplosiveHits += weapon.hits;
          groundExplosiveDamage += damageToApply;
        } else {
          groundSeekingExplosiveHits += weapon.hits;
          groundSeekingExplosiveDamage += damageToApply;
        }
      } else {
        groundNormalHits += weapon.hits;
        groundDamageNormal += (int) damageToApply;
      }
    }

    private void sumAirDamage(Agent agent) {
      Weapon weapon = agent.airWeapon;
      double damageToApply = calculateDamage(agent, weapon);
      if (weapon.damageType == DamageType.CONCUSSIVE) {
        airConcussiveDamage += damageToApply;
        airConcussiveHits += weapon.hits;
      } else if (weapon.damageType == DamageType.EXPLOSIVE) {
        airExplosiveDamage += damageToApply;
        airExplosiveHits += weapon.hits;
      } else {
        airDamageNormal += (int) damageToApply;
        airNormalHits += weapon.hits;
      }
    }

    private double calculateDamage(Agent attacker, Weapon weapon) {
      double rangeFactor = weapon.maxRange * parameters.rangeScale;
      double speedFactor = attacker.burrowedAttacker ? 0.0 : attacker.speed * parameters.speedScale;
      double radialSplashFactor =
          weapon.splashType == SplashType.RADIAL_ENEMY_SPLASH
                  || weapon.splashType == SplashType.RADIAL_SPLASH
              ? parameters.radialSplashScale * weapon.innerSplashRadius
              : 0.0;
      double lineSplashFactor =
          weapon.splashType == SplashType.LINE_SPLASH
              ? parameters.lineSplashScale * weapon.innerSplashRadius
              : 0.0;
      double bounceSplashFactor =
          weapon.splashType == SplashType.BOUNCE ? parameters.bounceSplashFactor : 0.0;

      return weapon.damageShifted
          * (1.0
              + rangeFactor
              + speedFactor
              + radialSplashFactor
              + lineSplashFactor
              + bounceSplashFactor)
          / max(attacker.groundWeapon.cooldown, attacker.airWeapon.cooldown);
    }

    double sumDamageTo(Collection<Agent> targets) {
      double damageSum = 0;
      for (Agent target : targets) {
        if (!target.detected) {
          continue;
        }
        if (target.isFlyer) {
          damageSum +=
              damageTakenBy(
                  target,
                  airConcussiveDamage,
                  airConcussiveHits,
                  airExplosiveDamage,
                  airExplosiveHits,
                  airDamageNormal,
                  airNormalHits);
        } else {
          damageSum +=
              damageTakenBy(
                  target,
                  groundConcussiveDamage,
                  groundConcussiveHits,
                  groundExplosiveDamage,
                  groundExplosiveHits,
                  groundDamageNormal,
                  groundNormalHits);
          if (target.seekableTarget) {
            damageSum +=
                damageTakenBy(
                    target, 0, 0, groundSeekingExplosiveDamage, groundSeekingExplosiveHits, 0, 0);
          }
        }
      }
      return damageSum;
    }

    private int damageTakenBy(
        Agent target,
        int concussiveDamage,
        int concussiveHits,
        int explosiveDamage,
        int explosiveHits,
        int normalDamage,
        int normalHits) {
      int damage =
          max(
              AgentUtil.reduceDamageByTargetSizeAndDamageType(
                  target,
                  DamageType.CONCUSSIVE,
                  concussiveDamage - concussiveHits * target.armorShifted),
              concussiveHits * 128);
      damage +=
          max(
              AgentUtil.reduceDamageByTargetSizeAndDamageType(
                  target,
                  DamageType.EXPLOSIVE,
                  explosiveDamage - explosiveHits * target.armorShifted),
              explosiveHits * 128);
      damage += max(normalDamage - normalHits * target.armorShifted, normalHits * 128);
      return damage;
    }
  }

  public static class EvaluationResult {
    public final double value;

    public EvaluationResult(double value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return "EvaluationResult{" + "value=" + value + '}';
    }
  }

  public static class EvalWithAgents {

    public final double eval;
    public final List<Agent> agents;

    public EvalWithAgents(double eval, List<Agent> agents) {
      this.eval = eval;
      this.agents = Collections.unmodifiableList(agents);
    }
  }

  private static final class EvaluatorDeathContext extends UnitDeathContext {

    Collection<Agent> target;

    @Override
    public void addAgent(Agent agent) {
      target.add(agent);
    }

    @Override
    public void removeAgents(List<Agent> agents) {
      // NOOP in evaluator
    }
  }

  public static class Parameters {

    final double shieldScale;
    final double speedScale;
    final double rangeScale;
    final double radialSplashScale;
    final double lineSplashScale;
    final double bounceSplashFactor;
    final double heal;

    public Parameters(double[] source) {
      shieldScale = source[0];
      speedScale = source[1];
      rangeScale = source[2];
      radialSplashScale = source[3];
      lineSplashScale = source[4];
      bounceSplashFactor = source[5];
      heal = source[6] * 500000;
    }

    public Parameters() {
      this(
          new double[]{4.1073760812937365, 9.406299471853998, 0.3187030930291376, 4.168181253247036,
              9.692917500259298, 6.775061462047722, 7.989573615123854});
    }
  }
}

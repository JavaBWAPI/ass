package org.bk.ass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Used to get a rough guess for combat outcome. Doesn't provide as much detail as the {@link
 * Simulator}. Estimates [0-0.5) => not so good for force A. Estimates (0.5-1] => not bad for force
 * A.
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

  public double evaluate(Collection<Agent> agentsA, Collection<Agent> agentsB) {
    List<Agent> finalAgentsA = new ArrayList<>();
    agentsA.forEach(a -> a.onDeathReplacer.accept(finalAgentsA));
    List<Agent> finalAgentsB = new ArrayList<>();
    agentsB.forEach(a -> a.onDeathReplacer.accept(finalAgentsB));
    finalAgentsA.addAll(agentsA);
    finalAgentsB.addAll(agentsB);
    int damageToA = damageSum(finalAgentsB, finalAgentsA);
    int damageToB = damageSum(finalAgentsA, finalAgentsB);
    int healingToA = healing(finalAgentsA);
    int healingToB = healing(finalAgentsB);
    damageToA -= healingToA;
    if (damageToA < 0) {
      damageToA = 0;
    }
    damageToB -= healingToB;
    if (damageToB < 0) {
      damageToB = 0;
    }
    double evalA = (double) damageToA / finalAgentsA.stream().mapToInt(Agent::getHealth).sum();
    double evalB = (double) damageToB / finalAgentsB.stream().mapToInt(Agent::getHealth).sum();
    return (evalB + EPS / 2) / (evalA + evalB + EPS);
  }

  private int healing(Collection<Agent> agents) {
    return agents
        .stream()
        .mapToInt(
            a -> {
              int healed = 0;
              if (a.regeneratesHealth) {
                healed = parameters.healthRegen;
              }
              if (a.isHealer) {
                healed +=
                    agents
                        .stream()
                        .mapToInt(
                            b -> {
                              if (a == b || !b.isOrganic) {
                                return 0;
                              }
                              return parameters.heal;
                            })
                        .sum();
              }
              return healed;
            })
        .sum();
  }

  private int damageSum(Collection<Agent> from, Collection<Agent> to) {
    return from.stream()
        .mapToInt(
            a ->
                to.stream()
                    .mapToInt(
                        b -> {
                          Weapon weapon = a.weaponVs(b);
                          if (weapon.damageShifted == 0 || !b.detected) {
                            return 0;
                          }
                          double rangeFactor = 1.0 + weapon.maxRange * parameters.rangeScale;
                          double speedFactor = 1.0 + a.speed * parameters.speedScale;
                          return (int)
                              (Util.reduceDamageByTargetAndDamageType(
                                  b, weapon.damageType, weapon.damageShifted)
                                  * rangeFactor
                                  * speedFactor
                                  / a.maxCooldown);
                        })
                    .sum())
        .sum();
  }

  public static class Parameters {

    public int healthRegen = 300;
    public int heal = 200;
    public double rangeScale = 0.05;
    public double speedScale = 0.008;
  }
}

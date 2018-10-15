package org.bk.ass;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Used to get a rough guess for combat outcome. Doesn't provide as much detail as the {@link
 * Simulator}. Estimates [0-0.5) => not so good for force A. Estimates (0.5-1] => not bad for force
 * A. Avoid using it to estimate an active entanglement (as positioning might be completely
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
   * obliterated.
   */
  public double evaluate(Collection<Agent> agentsA, Collection<Agent> agentsB) {
    List<Agent> finalAgentsA = new ArrayList<>();
    agentsA.forEach(a -> a.onDeathReplacer.accept(finalAgentsA));
    List<Agent> finalAgentsB = new ArrayList<>();
    agentsB.forEach(a -> a.onDeathReplacer.accept(finalAgentsB));
    finalAgentsA.addAll(agentsA);
    finalAgentsB.addAll(agentsB);
    int damageToA = damageSum(finalAgentsB, finalAgentsA);
    int damageToB = damageSum(finalAgentsA, finalAgentsB);
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
                          double radialSplashFactor =
                              weapon.explosionType == ExplosionType.RADIAL_SPLASH
                                  ? parameters.radialSplashFactor
                                  : 1.0;
                          return (int)
                              (Util.reduceDamageByTargetAndDamageType(
                                  b, weapon.damageType, weapon.damageShifted)
                                  * rangeFactor
                                  * speedFactor
                                  * radialSplashFactor
                                  / a.maxCooldown);
                        })
                    .sum())
        .sum();
  }

  public static class Parameters {

    public final double shieldScale;
    public final double speedScale;
    public final double rangeScale;
    public final double radialSplashFactor;
    public final int heal;
    public final int healthRegen;
    public final int shieldRegen;

    public Parameters(double[] source) {
      shieldScale = source[0];
      speedScale = source[1];
      rangeScale = source[2];
      radialSplashFactor = source[3];
      heal = (int) source[4];
      healthRegen = (int) source[5];
      shieldRegen = (int) source[6];
    }

    public Parameters() {
      this(new double[]{1.99425, 0.0155, 0.001125, 2.4, 126.375625, 267.651375, 443.103125});
    }
  }
}

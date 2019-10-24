package org.bk.ass.sim;

import org.bk.ass.collection.UnorderedCollection;
import org.bk.ass.sim.Simulator.Behavior;

import static org.bk.ass.sim.AttackerBehavior.*;

public class ApproxAttackBehavior implements Behavior {
    @Override
    public boolean simUnit(int frameSkip, Agent agent, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
        int i = enemies.size() - 1;
        while (agent.cooldown <= 0 && i >= 0) {
            Agent enemy = enemies.get(i);
            Weapon wpn = agent.weaponVs(enemy);
            if (enemy.healthShifted > 0
                    && wpn.damageShifted != 0
                    && enemy.detected
                    && !enemy.isStasised) {
                if (agent.canStim
                        && agent.remainingStimFrames <= 0
                        && agent.healthShifted >= agent.maxHealthShifted / 2) {
                    agent.stim();
                }
                attack(agent, wpn, enemy, allies, enemies);
            } else
                i--;
        }

        return agent.cooldown > 0;
    }

    public static void attack(Agent agent, Weapon weapon, Agent enemy, UnorderedCollection<Agent> allies, UnorderedCollection<Agent> enemies) {
        AgentUtil.dealDamage(agent, weapon, enemy);
        switch (weapon.splashType) {
            case BOUNCE:
                dealBounceDamage(weapon, enemy, enemies);
                break;
            case RADIAL_SPLASH:
                dealRadialSplashDamage(weapon, enemy, allies, enemies);
                break;
            case RADIAL_ENEMY_SPLASH:
                dealRadialSplashDamage(weapon, enemy, enemies);
                break;
            case LINE_SPLASH:
                dealLineSplashDamage(agent, weapon, enemy, enemies);
                break;
            default:
                // No splash
        }

        if (agent.remainingStimFrames <= 0)
            agent.cooldown += agent.maxCooldown;
        else
            agent.cooldown += agent.maxCooldown / 2;
    }
}

package org.bk.ass.sim;

import java.util.List;

public abstract class UnitDeathContext {

  Agent deadUnit;

  public Agent getDeadUnit() {
    return deadUnit;
  }


  abstract void addAgent(Agent agent);

  public abstract void removeAgents(List<Agent> agents);
}

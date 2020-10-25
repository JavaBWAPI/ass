package org.bk.ass.bt.construction;

import bwapi.UnitType;
import java.util.Objects;
import org.bk.ass.manage.Lock;

public class BuildBoard<B, W, P, R> {

  public final UnitType type;
  public Lock<W> workerLock;
  public Lock<P> positionLock;
  public Lock<R> resourceLock;
  public B building;

  public BuildBoard(
      UnitType type,
      Lock<W> workerLock,
      Lock<P> positionLock,
      Lock<R> resourceLock) {
    this.type = Objects.requireNonNull(type);
    this.positionLock = Objects.requireNonNull(positionLock);
    this.workerLock = Objects.requireNonNull(workerLock);
    this.resourceLock = Objects.requireNonNull(resourceLock);
  }
}

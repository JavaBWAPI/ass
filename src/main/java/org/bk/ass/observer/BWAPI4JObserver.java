package org.bk.ass.observer;

import org.openbw.bwapi4j.BW;
import org.openbw.bwapi4j.InteractionHandler;
import org.openbw.bwapi4j.Position;
import org.openbw.bwapi4j.TilePosition;
import org.openbw.bwapi4j.type.UnitType;
import org.openbw.bwapi4j.unit.NuclearMissile;
import org.openbw.bwapi4j.unit.PlayerUnit;
import org.openbw.bwapi4j.unit.Transporter;
import org.openbw.bwapi4j.unit.Unit;

import java.util.List;

public class BWAPI4JObserver {
  private Position myStartLocation;
  private int scrWidth = 640; //Default width ChaosLauncher with WMODE plugin
  private int scrHeight = 480; //Default height ChaosLauncher with WMODE plugin
  private int cameraMoveTime = 150;
  private int cameraMoveTimeMin = 50;
  private int watchScoutWorkerUntil = 7500;
  private int lastMoved = 0;
  private int lastMovedPriority = 0;
  private Position currentCameraPosition;
  private Position cameraFocusPosition;
  private Unit cameraFocusUnit = null;
  private boolean followUnit = false;
  private boolean enabled = false;
  private BW game;
  private InteractionHandler IH;

  public BWAPI4JObserver(BW game) {
    this.game = game;
    this.IH = game.getInteractionHandler();
    myStartLocation = IH.self().getStartLocation().toPosition();
    cameraFocusPosition = myStartLocation;
    currentCameraPosition = myStartLocation;
  }

  public BWAPI4JObserver(BW game, int scrHeight, int scrWidth) {
    this(game);
    this.scrHeight = scrHeight;
    this.scrWidth = scrWidth;
  }

  public void onFrame() {
    if (enabled) {
      moveCameraFallingNuke();
      moveCameraIsUnderAttack();
      moveCameraIsAttacking();
      if (IH.getFrameCount() <= watchScoutWorkerUntil) moveCameraScoutWorker();
      moveCameraArmy();
      moveCameraDrop();
      updateCameraPosition();
    }
  }

  public void moveCameraUnitCompleted(PlayerUnit unit) {
    if (enabled && unit != null) {
      int prio = 1;
      if (shouldMoveCamera(prio) && unit.getPlayer().equals(IH.self()) && !unit.getType().isWorker()) {
        moveCamera(unit, prio);
      }
    }
  }

  public void moveCameraNukeDetect(Position target) {
    int prio = 4;
    if (shouldMoveCamera(prio)) moveCamera(target, prio);
  }

  // Enables or disables the observer
  public void toggle() {
    enabled = !enabled;
  }

  private void moveCameraScoutWorker() {
    int highPrio = 2;
    int lowPrio = 0;
    if (!shouldMoveCamera(lowPrio)) return;
    for (PlayerUnit unit : game.getUnits(IH.self())) {
      if (!unit.exists() || !unit.getType().isWorker() || !unit.isCompleted()) continue;
      if (isNearStartLocation(unit.getPosition())) moveCamera(unit, highPrio);
      else if (!isNearOwnStartLocation(unit.getPosition())) moveCamera(unit, lowPrio);
    }
  }

  private boolean isNearOwnStartLocation(Position position) {
    int distance = 10 * TilePosition.SIZE_IN_PIXELS; // 10*32
    return myStartLocation.getDistance(position) <= distance;
  }

  private boolean isNearStartLocation(Position position) {
    int distance = 1000;
    List<TilePosition> startLocations = game.getBWMap().getStartPositions();
    for (TilePosition it : startLocations) {
      Position startLocation = it.toPosition();
      // if the start position is not our own home, and the start position is closer than distance
      if (!isNearOwnStartLocation(startLocation) && startLocation.getDistance(position) <= distance) return true;
    }
    return false;
  }

  private void updateCameraPosition() {
    double moveFactor = 0.1;
    if (followUnit && game.getBWMap().isValidPosition(cameraFocusUnit.getPosition())) {
      cameraFocusPosition = cameraFocusUnit.getPosition();
    }
    currentCameraPosition = currentCameraPosition.add(new Position((int) (moveFactor * (cameraFocusPosition.getX() - currentCameraPosition.getX())), (int) (moveFactor * (cameraFocusPosition.getY() - currentCameraPosition.getY()))));
    Position currentMovedPosition = currentCameraPosition.subtract(new Position(scrWidth / 2, scrHeight / 2 - 40));
    if (game.getBWMap().isValidPosition(currentCameraPosition)) {
      IH.setScreenPosition(currentMovedPosition);
    }
  }

  private void moveCameraDrop() {
    int prio = 2;
    if (!shouldMoveCamera(prio)) return;
    for (PlayerUnit unit : game.getUnits(IH.self())) {
      if (!unit.exists() || unit.isCompleted() || unit.getType() == UnitType.Terran_Bunker) continue;
      if (unit instanceof Transporter && ((Transporter) unit).getLoadedUnits().size() > 0 && isNearStartLocation(unit.getPosition())) {
        moveCamera(unit, prio);
      }
    }
  }

  private void moveCameraArmy() {
    int prio = 1;
    if (!shouldMoveCamera(prio)) return;
    // Double loop, check if army units are close to each other
    int radius = 50;
    Unit bestPosUnit = null;
    int mostUnitsNearby = 0;
    for (Unit unit : game.getAllUnits()) {
      if (!unit.exists() || !(unit instanceof PlayerUnit) || !isArmyUnit(unit)) continue;
      int nrUnitsNearby = 0;
      for (Unit unit2 : unit.getUnitsInRadius(radius, game.getAllUnits())) {
        if (!unit2.exists() || !(unit2 instanceof PlayerUnit) || !isArmyUnit(unit2)) continue;
        nrUnitsNearby++;
      }
      if (nrUnitsNearby > mostUnitsNearby) {
        mostUnitsNearby = nrUnitsNearby;
        bestPosUnit = unit;
      }
    }
    if (mostUnitsNearby > 1) moveCamera(bestPosUnit, prio);
  }

  private boolean isArmyUnit(Unit unit) {
    UnitType type = unit.getType();
    return !(type.isWorker() || type.isBuilding() || type == UnitType.Terran_Vulture_Spider_Mine
            || type == UnitType.Zerg_Overlord || type == UnitType.Zerg_Larva);
  }

  private void moveCameraIsAttacking() {
    int prio = 3;
    if (!shouldMoveCamera(prio)) return;
    for (PlayerUnit unit : game.getUnits(IH.self())) {
      if (!unit.exists()) continue;
      if (unit.isAttacking()) moveCamera(unit, prio);
    }
  }

  private void moveCamera(Position pos, int priority) {
    if (!shouldMoveCamera(priority)) return;
    // don't register a camera move if the position is the same
    if (!followUnit && cameraFocusPosition == pos) return;
    cameraFocusPosition = pos;
    lastMoved = IH.getFrameCount();
    lastMovedPriority = priority;
    followUnit = false;
  }

  private void moveCamera(Unit unit, int priority) {
    if (!shouldMoveCamera(priority)) return;
    // don't register a camera move if we follow the same unit
    if (followUnit && cameraFocusUnit == unit) return;
    cameraFocusUnit = unit;
    lastMoved = IH.getFrameCount();
    lastMovedPriority = priority;
    followUnit = true;
  }

  private boolean shouldMoveCamera(int priority) {
    boolean isTimeToMove = IH.getFrameCount() - lastMoved >= cameraMoveTime;
    boolean isTimeToMoveIfHigherPrio = IH.getFrameCount() - lastMoved >= cameraMoveTimeMin;
    boolean isHigherPrio = lastMovedPriority < priority;
    // camera should move IF: enough time has passed OR (minimum time has passed AND new prio is higher)
    return isTimeToMove || (isHigherPrio && isTimeToMoveIfHigherPrio);
  }

  private void moveCameraFallingNuke() {
    int prio = 5;
    if (!shouldMoveCamera(prio)) return;
    for (Unit unit : game.getAllUnits()) {
      if (!unit.exists() || unit.getType() != UnitType.Terran_Nuclear_Missile) continue;
      if (((NuclearMissile) unit).getVelocityY() > 0) {
        moveCamera(unit, prio);
        return;
      }
    }
  }

  private void moveCameraIsUnderAttack() {
    int prio = 3;
    if (!shouldMoveCamera(prio)) return;
    for (PlayerUnit unit : game.getUnits(IH.self())) {
      if (!unit.exists()) continue;
      if (unit.isUnderAttack()) moveCamera(unit, prio);
    }
  }
}

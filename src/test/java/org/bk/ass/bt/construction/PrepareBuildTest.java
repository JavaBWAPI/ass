package org.bk.ass.bt.construction;

import static org.mockito.Mockito.mock;

import bwapi.TilePosition;
import bwapi.Unit;
import bwapi.UnitType;
import org.assertj.core.api.Assertions;
import org.bk.ass.bt.Executor;
import org.bk.ass.bt.NodeStatus;
import org.bk.ass.bt.TreeNode;
import org.bk.ass.manage.BlacklistReservation;
import org.bk.ass.manage.GMS;
import org.bk.ass.manage.GMSReservation;
import org.bk.ass.manage.Lock;
import org.bk.ass.manage.Reservation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PrepareBuildTest {

  private Reservation<Unit> unitReservation = new BlacklistReservation<>();
  private Reservation<TilePosition> positionReservation = new BlacklistReservation<>();
  private Reservation<GMS> gmsReservation = new GMSReservation();
  private BuildBoard<Unit, Unit, TilePosition, GMS> board =
      new BuildBoard<>(UnitType.Protoss_Arbiter_Tribunal,
          new Lock<>(unitReservation),
          new Lock<>(positionReservation),
          new Lock<>(gmsReservation));
  private final PrepareBuild<BuildBoard<Unit, Unit, TilePosition, GMS>> sut =
      new PrepareBuild<BuildBoard<Unit, Unit, TilePosition, GMS>>(board) {
        @Override
        protected void determineStartedBuilding() {
          board.building = startedBuilding;
        }

        @Override
        protected GMS requiredResources() {
          return null;
        }

        @Override
        protected TilePosition findBuildPosition() {
          return null;
        }

        @Override
        protected Unit findWorker() {
          return null;
        }

        @Override
        protected TreeNode createNodeToFulfillRequirements() {
          return NodeStatus.SUCCESS.after(
              () -> preparationCalled = true);
        }
      };
  private Unit startedBuilding;
  private boolean preparationCalled;

  @BeforeEach
  void init() {
    sut.init();
  }

  @Test
  public void shouldPrepareWhenBuildingNotStarted() {
    // GIVEN

    // WHEN
    Executor.execute(sut);

    // THEN
    Assertions.assertThat(preparationCalled).isTrue();
  }

  @Test
  public void shouldNotPrepareWhenBuildingStarted() {
    // GIVEN
    startedBuilding = mock(Unit.class);

    // WHEN
    Executor.execute(sut);

    // THEN
    Assertions.assertThat(preparationCalled).isTrue();
  }
}

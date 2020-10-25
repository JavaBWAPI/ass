package org.bk.ass.bt.construction;

import static org.bk.ass.bt.DSL.parallel;
import static org.bk.ass.bt.DSL.selector;
import static org.bk.ass.bt.DSL.sequence;

import java.util.Objects;
import org.bk.ass.bt.AcquireLock;
import org.bk.ass.bt.AcquireLockLater;
import org.bk.ass.bt.BehaviorTree;
import org.bk.ass.bt.LambdaNode;
import org.bk.ass.bt.NodeStatus;
import org.bk.ass.bt.ReleaseLock;
import org.bk.ass.bt.Repeat;
import org.bk.ass.bt.Succeeder;
import org.bk.ass.bt.TreeNode;
import org.bk.ass.manage.Lock;

public abstract class PrepareBuild<BB extends BuildBoard<?, ?, ?, ?>> extends BehaviorTree {

  protected final BB board;

  public PrepareBuild(BB board) {
    Objects.requireNonNull(board);
    this.board = board;
  }

  @Override
  protected TreeNode getRoot() {
    return selector(
        new LambdaNode(this::checkForExistingBuilding),
        parallel(
            // Only prepare if not yet started
            createNodeToFulfillRequirements(),
            selector(
                sequence(
                    new AcquireLock<>(board.positionLock, this::findBuildPosition),
                    new AcquireLock<>(board.workerLock, this::findWorker),
                    selector(
                        createFutureFramesNode(board.resourceLock),
                        new AcquireLockLater<>(board.resourceLock, this::requiredResources),
                        new ReleaseLock<>(board.workerLock)
                    )
                ),
                sequence(
                    createOnPreparationFailedNode(),
                    new Repeat(new Succeeder(
                        new AcquireLock<>(board.resourceLock, this::requiredResources)))
                )
            )
        )
    );
  }


  protected TreeNode createOnPreparationFailedNode() {
    return NodeStatus.SUCCESS.after(() -> {
    });
  }

  protected abstract <R> R requiredResources();

  protected abstract <P> P findBuildPosition();

  protected abstract <W> W findWorker();

  /**
   * Sets the number of frames expected to be able to actually start construction. That is: The
   * worker is at the target location, and the target location is vacant.
   */
  protected TreeNode createFutureFramesNode(Lock<?> resourceLock) {
    return NodeStatus.SUCCESS.after(() -> resourceLock.setFutureFrames(0));
  }

  /**
   * Hook to return a node/tree to build all things to build "this". Does nothing by default.
   */
  protected TreeNode createNodeToFulfillRequirements() {
    return NodeStatus.SUCCESS.after(() -> {
    });
  }

  private NodeStatus checkForExistingBuilding() {
    if (board.positionLock.isSatisfied()) {
      if (board.workerLock.getItem() != null) {
        determineStartedBuilding();
        if (board.building != null) {
          return NodeStatus.SUCCESS;
        }
      }
      board.building = null;
    }
    return NodeStatus.FAILURE;
  }

  /**
   * Sets the started building on the board, or null, if nothing was found.
   */
  protected abstract void determineStartedBuilding();
}

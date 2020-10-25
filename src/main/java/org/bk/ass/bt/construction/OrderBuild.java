package org.bk.ass.bt.construction;

import static org.bk.ass.bt.DSL.condition;
import static org.bk.ass.bt.DSL.selector;
import static org.bk.ass.bt.DSL.sequence;

import java.util.Objects;
import org.bk.ass.bt.BehaviorTree;
import org.bk.ass.bt.NodeStatus;
import org.bk.ass.bt.TreeNode;

public abstract class OrderBuild<BB extends BuildBoard<?, ?, ?, ?>> extends BehaviorTree {

  protected final BB board;

  public OrderBuild(BB board) {
    Objects.requireNonNull(board);
    this.board = board;
  }

  @Override
  protected TreeNode getRoot() {
    return selector(
        condition(this::isConstructionStarted),
        sequence(
            createOnConstructionNotYetStartedNode(),
            createOrderWorkerToBuildNode()
        )
    );
  }

  protected TreeNode createOnConstructionNotYetStartedNode() {
    return NodeStatus.SUCCESS.after(() -> {
    });
  }

  /**
   * Expected to return {@link NodeStatus#RUNNING} while the build has not been started
   */
  protected abstract TreeNode createOrderWorkerToBuildNode();

  protected abstract boolean isConstructionStarted();
}

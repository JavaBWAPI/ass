package org.bk.ass.bt.construction;

import static org.bk.ass.bt.DSL.sequence;

import java.util.Objects;
import org.bk.ass.bt.BehaviorTree;
import org.bk.ass.bt.TreeNode;

public abstract class StartBuild<BB extends BuildBoard<?, ?, ?, ?>> extends BehaviorTree {

  protected final BB board;

  public StartBuild(BB board) {
    super("Start construction of " + board.type);
    Objects.requireNonNull(board);
    this.board = board;
  }

  @Override
  protected TreeNode getRoot() {
    return sequence(
        createPrepareBuildNode(),
        createOrderBuildNode()
    );
  }

  protected abstract PrepareBuild<BB> createOrderBuildNode();

  protected abstract OrderBuild<BB> createPrepareBuildNode();
}

package org.bk.ass.bt.construction;

import static org.bk.ass.bt.DSL.condition;
import static org.bk.ass.bt.DSL.memo;
import static org.bk.ass.bt.DSL.selector;
import static org.bk.ass.bt.DSL.sequence;

import java.util.Objects;
import org.bk.ass.bt.BehaviorTree;
import org.bk.ass.bt.TreeNode;
import org.bk.ass.bt.Wait;

public abstract class Build<BB extends BuildBoard<?, ?, ?, ?>> extends BehaviorTree {

  protected final BB board;

  public Build(BB board) {
    Objects.requireNonNull(board);
    this.board = board;
  }

  @Override
  protected TreeNode getRoot() {
    return memo(
        selector(
            condition(this::isConstructionCompleted),
            sequence(createStartBuildNode(), Wait.INSTANCE)));
  }

  protected abstract StartBuild<BB> createStartBuildNode();

  protected abstract boolean isConstructionCompleted();
}

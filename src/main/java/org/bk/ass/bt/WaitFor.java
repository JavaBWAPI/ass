package org.bk.ass.bt;

import java.util.Objects;
import java.util.function.BooleanSupplier;

public class WaitFor extends TreeNode {
  private final BooleanSupplier check;

  public WaitFor(BooleanSupplier check) {
    Objects.requireNonNull(check, "condition must be set");
    this.check = check;
  }

  @Override
  public void exec() {
    if (check.getAsBoolean()) {
      success();
    } else {
      running();
    }
  }
}

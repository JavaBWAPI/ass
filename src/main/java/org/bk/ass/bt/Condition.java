package org.bk.ass.bt;

import java.util.Objects;
import java.util.function.BooleanSupplier;

/**
 * Node that will succeed if the given check passed, fails otherwise.
 */
public class Condition extends TreeNode {

  private final BooleanSupplier check;

  public Condition(BooleanSupplier check) {
    Objects.requireNonNull(check, "condition must be set");
    this.check = check;
  }

  @Override
  public void exec() {
    if (check.getAsBoolean()) {
      success();
    } else {
      failed();
    }
  }
}

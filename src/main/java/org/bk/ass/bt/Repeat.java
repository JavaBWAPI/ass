package org.bk.ass.bt;

import java.util.Objects;

public class Repeat extends Decorator {
  private final int initialLimit;
  private int remaining;
  private Policy policy;

  public enum Policy {
    SEQUENCE,
    SELECTOR,
    SELECTOR_INVERTED
  }

  public Repeat(Policy policy, TreeNode delegate) {
    super(delegate);
    Objects.requireNonNull(policy, "policy must be set");
    initialLimit = remaining = -1;
    this.policy = policy;
  }

  public Repeat(TreeNode delegate) {
    this(Policy.SEQUENCE, delegate);
  }

  public Repeat(int limit, TreeNode delegate) {
    this(Policy.SEQUENCE, limit, delegate);
  }

  public Repeat(Policy policy, int limit, TreeNode delegate) {
    super(delegate);
    Objects.requireNonNull(policy, "policy must be set");
    if (limit < 0) throw new IllegalArgumentException("limit must be >= 0");
    this.remaining = this.initialLimit = limit;
    this.policy = policy;
  }

  @Override
  protected void updateStatusFromDelegate(NodeStatus status) {
    if (status == NodeStatus.RUNNING) running();
    else if (policy == Policy.SEQUENCE) {
      if (status == NodeStatus.FAILURE) failed();
      else if (repeatAndCheckExhausted()) success();
    } else if (policy == Policy.SELECTOR) {
      if (status == NodeStatus.SUCCESS) success();
      else if (repeatAndCheckExhausted()) failed();
    } else if (policy == Policy.SELECTOR_INVERTED) {
      if (status == NodeStatus.FAILURE) success();
      else if (repeatAndCheckExhausted()) failed();
    }
  }

  private boolean repeatAndCheckExhausted() {
    if (remaining > 0) remaining--;
    if (remaining != 0) {
      super.reset();
      running();
      return false;
    } else return true;
  }

  @Override
  public void reset() {
    super.reset();
    this.remaining = this.initialLimit;
  }
}

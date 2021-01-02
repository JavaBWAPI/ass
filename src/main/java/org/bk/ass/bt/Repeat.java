package org.bk.ass.bt;

import java.util.Objects;

/**
 * Repeatedly ticks a delegate node. If the delegate completes its operation, it will automatically
 * be resetted and re-ticked.
 * <p>
 * An internal {@link Policy} is used to determine the status of the Repeat node based on the result
 * of the delegate.
 * <p>
 * Use {@link Policy#SEQUENCE} to tick a delegate as long as it is succeeding. Use {@link
 * Policy#SELECTOR} to tick a delegate until it succeeds. Use {@link Policy#SELECTOR_INVERTED} to
 * tick a delegate until it fails.
 */
public class Repeat extends Decorator {

  private final int initialLimit;
  private int remaining;
  private final Policy policy;

  public enum Policy {
    /**
     * Repeat will have status {@link NodeStatus#RUNNING} while the delegate has status {@link
     * NodeStatus#SUCCESS} or {@link NodeStatus#RUNNING}. It will fail otherwise.
     */
    SEQUENCE,
    /**
     * Repeat will have status {@link NodeStatus#RUNNING} while the delegate has status {@link
     * NodeStatus#FAILURE} or {@link NodeStatus#RUNNING}. It will succeed otherwise.
     */
    SELECTOR,
    /**
     * Repeat will have status {@link NodeStatus#RUNNING} while the delegate has status {@link
     * NodeStatus#SUCCESS} or {@link NodeStatus#RUNNING}. It will succeed otherwise.
     */
    SELECTOR_INVERTED
  }

  public Repeat(Policy policy, TreeNode delegate) {
    super(delegate);
    Objects.requireNonNull(policy, "policy must be set");
    initialLimit = remaining = -1;
    this.policy = policy;
  }

  /**
   * Initializes with the default policy {@link Policy#SEQUENCE}.
   */
  public Repeat(TreeNode delegate) {
    this(Policy.SEQUENCE, delegate);
  }

  /**
   * Initializes with the default policy {@link Policy#SEQUENCE}.
   */
  public Repeat(int limit, TreeNode delegate) {
    this(Policy.SEQUENCE, limit, delegate);
  }

  /**
   * Allows to configure a limit of repeats. After exhaustion, it will either succeed, if policy is
   * SEQUENCE or fail if the policy is SELECTOR.
   */
  public Repeat(Policy policy, int limit, TreeNode delegate) {
    super(delegate);
    Objects.requireNonNull(policy, "policy must be set");
    if (limit < 0) {
      throw new IllegalArgumentException("limit must be >= 0");
    }
    this.remaining = this.initialLimit = limit;
    this.policy = policy;
  }

  @Override
  protected void updateStatusFromDelegate(NodeStatus status) {
    if (status == NodeStatus.RUNNING) {
      running();
    } else if (policy == Policy.SEQUENCE) {
      if (status == NodeStatus.FAILURE) {
        failed();
      } else if (repeatAndCheckExhausted()) {
        success();
      }
    } else if (policy == Policy.SELECTOR) {
      if (status == NodeStatus.SUCCESS) {
        success();
      } else if (repeatAndCheckExhausted()) {
        failed();
      }
    } else if (policy == Policy.SELECTOR_INVERTED) {
      if (status == NodeStatus.FAILURE) {
        success();
      } else if (repeatAndCheckExhausted()) {
        failed();
      }
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

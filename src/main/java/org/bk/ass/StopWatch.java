package org.bk.ass;

import org.bk.ass.bt.ExecutionContext;
import org.bk.ass.bt.TreeNode;

public class StopWatch {
  private long started = System.nanoTime();
  private long stopped = Long.MIN_VALUE;

  public long stop() {
    long now = System.nanoTime();
    if (stopped < started) {
      stopped = now;
    }
    return stopped;
  }

  public int ms() {
    return (int) ((stop() - started) / 1_000_000);
  }

  public void registerWith(ExecutionContext context, TreeNode node) {
    context.registerExecutionTime(node, ms());
  }
}

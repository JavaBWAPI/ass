package org.bk.ass.bt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Can be used to create tree stack traces and log performance numbers of nodes.
 */
public class ExecutionContext {

  public static final ExecutionContext NOOP =
      new ExecutionContext() {
        @Override
        public void push(TreeNode node) {
        }

        @Override
        public TreeNode pop() {
          return null;
        }

        @Override
        public void registerExecutionTime(TreeNode node, long nanos) {
        }
      };

  private Deque<TreeNode> stack = new ArrayDeque<>();
  private Map<TreeNode, Long> executionTime = new HashMap<>();

  public void push(TreeNode node) {
    stack.addLast(node);
  }

  public TreeNode pop() {
    return stack.removeLast();
  }

  public List<TreeNode> getNodeStack() {
    return new ArrayList<>(stack);
  }

  public void registerExecutionTime(TreeNode node, long nanos) {
    executionTime.compute(node, (n, ov) -> ov == null ? nanos : ov + nanos);
  }

  public Map<TreeNode, Long> getExecutionTime() {
    return new HashMap<>(executionTime);
  }
}

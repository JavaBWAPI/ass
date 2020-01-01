package org.bk.ass.bt;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        public void registerExecutionTime(TreeNode node, int millis) {
        }
      };

  private Deque<TreeNode> stack = new ArrayDeque<>();
  private Map<TreeNode, Integer> executionTime = new HashMap<>();

  public void push(TreeNode node) {
    stack.addLast(node);
  }

  public TreeNode pop() {
    return stack.removeLast();
  }

  public List<TreeNode> getNodeStack() {
    return new ArrayList<>(stack);
  }

  public void registerExecutionTime(TreeNode node, int millis) {
    executionTime.put(node, millis);
  }

  public Map<TreeNode, Integer> getExecutionTime() {
    return new HashMap<>(executionTime);
  }
}

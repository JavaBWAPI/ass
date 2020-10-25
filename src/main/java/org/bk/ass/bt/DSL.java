package org.bk.ass.bt;

import java.util.function.BooleanSupplier;
import org.bk.ass.bt.Parallel.Policy;

public final class DSL {

  private DSL() {
    // Utility class
  }

  public static Selector selector(String name, TreeNode... children) {
    return new Selector(name, children);
  }

  public static Selector selector(TreeNode... children) {
    return new Selector(children);
  }

  public static Sequence sequence(String name, TreeNode... children) {
    return new Sequence(name, children);
  }

  public static Sequence sequence(TreeNode... children) {
    return new Sequence(children);
  }

  public static Condition condition(BooleanSupplier check) {
    return new Condition(check);
  }

  public static Condition condition(String name, BooleanSupplier check) {
    return new Condition(name, check);
  }

  public static Memo memo(String name, TreeNode delegate) {
    return new Memo(name, delegate);
  }

  public static Memo memo(TreeNode delegate) {
    return new Memo(delegate);
  }

  public static Parallel parallel(String name, TreeNode... children) {
    return new Parallel(name, children);
  }

  public static Parallel parallel(TreeNode... children) {
    return new Parallel(children);
  }

  public static Parallel parallel(String name, Policy policy, TreeNode... children) {
    return new Parallel(name, policy, children);
  }

  public static Parallel parallel(Policy policy, TreeNode... children) {
    return new Parallel(policy, children);
  }
}
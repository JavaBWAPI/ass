package org.bk.ass.bt;

public class Wait extends TreeNode {

  public static final Wait INSTANCE = new Wait();

  protected Wait() {

  }

  @Override
  public void init() {
    running();
  }

  @Override
  public void exec() {
    // Nothing to do
  }
}

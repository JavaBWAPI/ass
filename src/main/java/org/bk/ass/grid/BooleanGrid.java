package org.bk.ass.grid;

public class BooleanGrid extends VersionedGrid<Boolean> {

  public BooleanGrid(int width, int height) {
    super(width, height);
  }

  @Override
  public Boolean get(int x, int y) {
    return x >= 0 && y >= 0 && x < getWidth() && y < getHeight() && dataVersion[y][x] == version;
  }

  @Override
  public void set(int x, int y, Boolean value) {
    dataVersion[y][x] = value ? version : 0;
  }

  @Override
  protected Boolean internalGet(int x, int y) {
    throw new IllegalStateException("Should not be called");
  }

  @Override
  protected void internalSet(int x, int y, Boolean value) {
    throw new IllegalStateException("Should not be called");
  }
}

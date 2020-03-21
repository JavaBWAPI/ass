package org.bk.ass.grid;

abstract class VersionedGrid<T> implements Grid<T> {

  protected int version = 1;
  private final int width;
  private final int height;
  protected final int[][] dataVersion;

  public VersionedGrid(int width, int height) {
    if (width <= 1 || height <= 1) {
      throw new IllegalArgumentException("Invalid grid size");
    }
    this.width = width;
    this.height = height;
    dataVersion = new int[height][width];
  }

  @Override
  public int getWidth() {
    return width;
  }

  @Override
  public int getHeight() {
    return height;
  }

  @Override
  public T get(int x, int y) {
    return dataVersion[y][x] == version ? internalGet(x, y) : null;
  }

  protected abstract T internalGet(int x, int y);

  public void set(int x, int y, T value) {
    internalSet(x, y, value);
    dataVersion[y][x] = version;
  }

  protected abstract void internalSet(int x, int y, T value);

  public void updateVersion() {
    version++;
  }
}

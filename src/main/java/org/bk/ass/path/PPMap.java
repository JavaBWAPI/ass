package org.bk.ass.path;

public interface PPMap extends Map {

  short left(int x, int y);

  short down(int x, int y);

  short up(int x, int y);

  short right(int x, int y);

  static PPMap fromBooleanArray(boolean[][] map) {
    return fromMap(Map.fromBooleanArray(map));
  }

  static PPMap fromMap(Map map) {
    short[][] left = new short[map.getHeight()][map.getWidth()];
    short[][] right = new short[map.getHeight()][map.getWidth()];
    short[][] up = new short[map.getHeight()][map.getWidth()];
    short[][] down = new short[map.getHeight()][map.getWidth()];
    for (int y = 0; y < map.getHeight(); y++) {
      short pos = -1;
      for (int x = 0; x < map.getWidth(); x++) {
        left[y][x] = pos;
        if (map.get(x, y)) {
          if (map.get(x - 1, y - 1) && !map.get(x, y - 1)
              || map.get(x - 1, y + 1) && !map.get(x, y + 1)) {
            pos = (short) x;
          }
        } else {
          pos = (short) (-x - 2);
        }
      }
      pos = (short) -map.getWidth();
      for (int x = map.getWidth() - 1; x >= 0; x--) {
        right[y][x] = pos;
        if (map.get(x, y)) {
          if (map.get(x + 1, y - 1) && !map.get(x, y - 1)
              || map.get(x + 1, y + 1) && !map.get(x, y + 1)) {
            pos = (short) x;
          }
        } else {
          pos = (short) -x;
        }
      }
    }
    for (int x = 0; x < map.getWidth(); x++) {
      short pos = -1;
      for (int y = 0; y < map.getHeight(); y++) {
        up[y][x] = pos;
        if (map.get(x, y)) {
          if (map.get(x - 1, y - 1) && !map.get(x - 1, y)
              || map.get(x + 1, y - 1) && !map.get(x + 1, y)) {
            pos = (short) y;
          }
        } else {
          pos = (short) (-y - 2);
        }
      }
      pos = (short) -map.getHeight();
      for (int y = map.getHeight() - 1; y >= 0; y--) {
        down[y][x] = pos;
        if (map.get(x, y)) {
          if (map.get(x - 1, y + 1) && !map.get(x - 1, y)
              || map.get(x + 1, y + 1) && !map.get(x + 1, y)) {
            pos = (short) y;
          }
        } else {
          pos = (short) -y;
        }
      }
    }
    return new PPMap() {
      @Override
      public short left(int x, int y) {
        return left[y][x];
      }

      @Override
      public short down(int x, int y) {
        return down[y][x];
      }

      @Override
      public short up(int x, int y) {
        return up[y][x];
      }

      @Override
      public short right(int x, int y) {
        return right[y][x];
      }

      @Override
      public Boolean get(int x, int y) {
        return map.get(x, y);
      }

      @Override
      public int getHeight() {
        return map.getHeight();
      }

      @Override
      public int getWidth() {
        return map.getWidth();
      }
    };
  }
}

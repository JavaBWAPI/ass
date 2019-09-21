package org.bk.ass.path;

import org.openjdk.jmh.annotations.*;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;

@Measurement(iterations = 3, time = 5)
@Fork(3)
public class JpsBenchmark {

  @State(Scope.Thread)
  public static class MyState {
    List<Position[]> positions;
    PPMap map;
    Jps jps;
    PPJps PPJps;

    @Setup
    public void setup() throws IOException {
      ImageIO.setUseCache(false);
      BufferedImage image = ImageIO.read(JpsTest.class.getResourceAsStream("/dungeon_map.bmp"));
      boolean[][] data = new boolean[image.getHeight()][image.getWidth()];
      for (int x = 0; x < image.getWidth(); x++) {
        for (int y = 0; y < image.getHeight(); y++) {
          data[x][y] = image.getRGB(x, y) == -1;
        }
      }
      map = PPMap.fromBooleanArray(data);
      jps = new Jps(map);
      PPJps = new PPJps(map);

      SplittableRandom rnd = new SplittableRandom(98765);
      positions = new ArrayList<>();
      for (int i = 0; i < 100; i++) {
        Position start;
        do {
          start = new Position(rnd.nextInt(image.getWidth()), rnd.nextInt(image.getHeight()));
        } while (map.get(start.x, start.y));
        Position end;
        do {
          end = new Position(rnd.nextInt(image.getWidth()), rnd.nextInt(image.getHeight()));
        } while (map.get(end.x, end.y));
        positions.add(new Position[] {start, end});
      }
    }
  }

  @Benchmark
  @OperationsPerInvocation(100)
  public List<Result> pathRandomStartToEnd(MyState state) {
    List<Result> results = new ArrayList<>();
    for (Position[] p : state.positions) {
      results.add(state.jps.findPath(p[0], p[1]));
    }
    return results;
  }

  @Benchmark
  @OperationsPerInvocation(100)
  public List<Result> pathRandomStartToEndWithPP(MyState state) {
    List<Result> results = new ArrayList<>();
    for (Position[] p : state.positions) {
      results.add(state.PPJps.findPath(p[0], p[1]));
    }
    return results;
  }
}

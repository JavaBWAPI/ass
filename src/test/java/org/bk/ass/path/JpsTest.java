package org.bk.ass.path;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.SplittableRandom;

import static org.assertj.core.api.Assertions.assertThat;

class JpsTest {

  @Test
  void shouldReturnIdentityIfStartAndEndMatch() {
    // GIVEN
    Jps sut = new Jps(Map.fromBooleanArray(new boolean[][] {{true}}));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(0, 0));

    // THEN
    assertThat(result.path).containsExactly(new Position(0, 0));
  }

  @Test
  void shouldFindNotFindPathWhenBlocked() {
    // GIVEN
    Jps sut = new Jps(Map.fromBooleanArray(new boolean[][] {{true, false, true}}));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(2, 0));

    // THEN
    assertThat(result.path).isEmpty();
  }

  @Test
  void shouldFindVerticalPath() {
    // GIVEN
    Jps sut = new Jps(Map.fromBooleanArray(new boolean[][] {{true, true, true}}));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(0, 2));

    // THEN
    assertThat(result.path).containsExactly(new Position(0, 0), new Position(0, 2));
  }

  @Test
  void shouldFindHorizontalPath() {
    // GIVEN
    Jps sut = new Jps(Map.fromBooleanArray(new boolean[][] {{true}, {true}, {true}}));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(2, 0));

    // THEN
    assertThat(result.path).containsExactly(new Position(0, 0), new Position(2, 0));
  }

  @Test
  void shouldFindDiagonalPath() {
    // GIVEN
    Jps sut =
        new Jps(
            Map.fromBooleanArray(
                new boolean[][] {{true, true, true}, {true, true, true}, {true, true, true}}));

    // WHEN
    Result result = sut.findPath(new Position(2, 2), new Position(0, 0));

    // THEN
    assertThat(result.path).containsExactly(new Position(2, 2), new Position(0, 0));
  }

  @Test
  void shouldFindPathWithObstacle() {
    // GIVEN
    Jps sut =
        new Jps(
            Map.fromBooleanArray(
                new boolean[][] {{true, true, true}, {true, false, false}, {true, true, true}}));

    // WHEN
    Result result = sut.findPath(new Position(2, 2), new Position(0, 0));

    // THEN
    assertThat(result.path)
        .containsExactly(
            new Position(2, 2), new Position(2, 1), new Position(1, 0), new Position(0, 0));
  }

  @Test
  void shouldNotFindPathWhenBlockedButWithCircle() {
    // GIVEN
    Jps sut =
        new Jps(
            Map.fromBooleanArray(
                new boolean[][] {
                  {true, true, true, true, true},
                  {true, false, false, false, true},
                  {true, false, true, false, true},
                  {true, false, false, false, true},
                  {true, true, true, true, true}
                }));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(2, 2));

    // THEN
    assertThat(result.path).isEmpty();
  }

  @Test
  void shouldFindPathWhenCircleHasHole() {
    // GIVEN
    Jps sut =
        new Jps(
            Map.fromBooleanArray(
                new boolean[][] {
                  {true, true, true, true, true},
                  {true, false, false, false, true},
                  {true, false, true, false, true},
                  {true, false, false, true, true},
                  {true, true, true, true, true}
                }));

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(2, 2));

    // THEN
    assertThat(result.length).isBetween(8f, 8.3f);
  }

  @Test
  void shouldFindPathInLargerExample() {
    // GIVEN
    Jps sut =
        new Jps(
            new Map() {
              @Override
              public Boolean get(int x, int y) {
                return y >= 0
                    && y <= 999
                    && (x == 0 && y % 4 == 1
                        || x == 999 && y % 4 == 3
                        || y % 2 == 0 && x >= 0 && x <= 999);
              }

              @Override
              public int getWidth() {
                return 1000;
              }

              @Override
              public int getHeight() {
                return 1000;
              }
            });

    // WHEN
    Result result = sut.findPath(new Position(0, 0), new Position(999, 999));

    // THEN
    assertThat(result.path).hasSize(1499);
  }

  @Test
  void shouldFindPathInDemoMap() throws IOException {
    // GIVEN
    BufferedImage image = ImageIO.read(JpsTest.class.getResourceAsStream("/dungeon_map.bmp"));
    boolean[][] data = new boolean[image.getHeight()][image.getWidth()];
    for (int x = 0; x < image.getWidth(); x++) {
      for (int y = 0; y < image.getHeight(); y++) {
        data[x][y] = image.getRGB(x, y) == -1;
      }
    }
    Map map = Map.fromBooleanArray(data);
    Jps sut = new Jps(map);
    SplittableRandom rnd = new SplittableRandom(123456);
    Result result = null;
    for (int i = 0; i < 500; i++) {
      Position start;
      do {
        start = new Position(rnd.nextInt(image.getWidth()), rnd.nextInt(image.getHeight()));
      } while (!map.get(start.x, start.y));
      Position end;
      do {
        end = new Position(rnd.nextInt(image.getWidth()), rnd.nextInt(image.getHeight()));
      } while (!map.get(end.x, end.y));

      // WHEN
      result = sut.findPath(start, end);
    }

    // THEN
    BufferedImage out =
        new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_3BYTE_BGR);
    Graphics g = out.getGraphics();
    g.drawImage(image, 0, 0, null);
    Position last = null;
    g.setColor(Color.GREEN);
    for (Position p : result.path) {
      if (last != null) {
        g.drawLine(last.x, last.y, p.x, p.y);
      }
      last = p;
    }
    g.setColor(Color.RED);
    for (Position p : result.path) {
      g.drawLine(p.x, p.y, p.x, p.y);
    }
    ImageIO.write(out, "PNG", new File("build/map_with_path.png"));
  }
}

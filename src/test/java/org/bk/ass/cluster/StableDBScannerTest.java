package org.bk.ass.cluster;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SplittableRandom;
import javax.imageio.ImageIO;
import org.bk.ass.path.Position;
import org.bk.ass.query.PositionAndId;
import org.bk.ass.query.PositionQueries;
import org.junit.jupiter.api.Test;

class StableDBScannerTest {

  private final Position[] EXAMPLE_POSITIONS = {new Position(299, 2900), new Position(581, 2875),
      new Position(579, 2309),
      new Position(640, 3104), new Position(612, 2946), new Position(593, 2343),
      new Position(300, 3049), new Position(299, 3050), new Position(299, 3050),
      new Position(424, 3059), new Position(299, 3050), new Position(480, 3104),
      new Position(520, 2281), new Position(448, 2992), new Position(301, 2847),
      new Position(542, 2247), new Position(554, 2149), new Position(1140, 1804),
      new Position(494, 2066), new Position(537, 2162), new Position(523, 2145),
      new Position(504, 2084), new Position(496, 2125)};

  @Test
  void neverReturnNoiseCluster() {
    // GIVEN
    StableDBScanner<String> sut =
        new StableDBScanner<>(Arrays.asList("a", "b"), 1, Collections::singleton);

    // WHEN
    sut.scan(-1);

    // THEN
    assertThat(sut.getClusters()).doesNotContainNull();
  }

  @Test
  void createClusters() {
    // GIVEN
    StableDBScanner<String> sut =
        new StableDBScanner<>(Arrays.asList("a", "b"), 1, o -> Arrays.asList("a", "b"));

    // WHEN
    sut.scan(-1);

    // THEN
    assertThat(sut.getClusters())
        .hasSize(1)
        .extracting("elements")
        .first()
        .asList()
        .containsOnly("a", "b");
  }

  @Test
  void createClustersOnlyForEnoughPoints() {
    // GIVEN
    StableDBScanner<String> sut =
        new StableDBScanner<>(Arrays.asList("a", "b"), 3, o -> Arrays.asList("a", "b"));

    // WHEN
    sut.scan(-1);

    // THEN
    assertThat(sut.getClusters()).hasSize(2);
  }

  @Test
  void stableClustersWithNoiseBasedClusters() {
    // GIVEN
    StableDBScanner<String> sut =
        new StableDBScanner<>(Arrays.asList("a", "b"), 3, Collections::singleton);
    sut.scan(-1);
    Collection firstRun = sut.getClusters();

    // WHEN
    sut.scan(-1);

    // THEN
    assertThat(sut.getClusters()).isEqualTo(firstRun);
  }

  @Test
  void stableClusters() {
    // GIVEN
    StableDBScanner<String> sut =
        new StableDBScanner<>(Arrays.asList("a", "b"), 1, Collections::singleton);
    sut.scan(-1);
    Collection<Cluster<String>> firstRun = sut.getClusters();

    // WHEN
    sut.scan(-1);

    // THEN
    assertThat(sut.getClusters()).isEqualTo(firstRun);
  }

  @Test
  void splitClusters() {
    // GIVEN
    Map<String, List<String>> radius = new HashMap<>();
    radius.put("a", Arrays.asList("a", "b"));
    radius.put("b", Arrays.asList("a", "b"));

    StableDBScanner<String> sut = new StableDBScanner<>(Arrays.asList("a", "b"), 2, radius::get);
    sut.scan(-1);
    Collection<Cluster<String>> firstRun = sut.getClusters();

    radius.put("a", singletonList("a"));
    radius.put("b", singletonList("b"));

    // WHEN
    sut.scan(-1);

    // THEN
    assertThat(sut.getClusters()).containsOnlyOnce(firstRun.iterator().next()).hasSize(2);
  }

  @Test
  void mergeClusters() {
    // GIVEN
    Map<String, List<String>> radius = new HashMap<>();
    radius.put("a", singletonList("a"));
    radius.put("b", singletonList("b"));

    StableDBScanner<String> sut = new StableDBScanner<>(Arrays.asList("a", "b"), 2, radius::get);
    sut.scan(-1);
    Collection<Cluster<String>> firstRun = sut.getClusters();

    radius.put("a", Arrays.asList("a", "b"));
    radius.put("b", Arrays.asList("a", "b"));

    // WHEN
    sut.scan(-1);

    // THEN
    assertThat(sut.getClusters()).hasSize(1).containsAnyElementsOf(firstRun);
  }

  @Test
  void doNotFailIfElementNotPartOfAnyRadius() {
    // GIVEN
    StableDBScanner<String> sut =
        new StableDBScanner<>(singletonList("a"), 1, unused -> Collections.emptyList());

    // WHEN
    sut.scan(-1);

    // THEN
    assertThat(sut.getClusters()).isNotEmpty().doesNotContainNull();
  }

  @Test
  void doNotFailIfElementIsNotPartOfDB() {
    // GIVEN
    StableDBScanner<String> sut =
        new StableDBScanner<>(singletonList("a"), 1, unused -> singleton("b"));

    // WHEN
    sut.scan(-1);

    // THEN
    assertThat(sut.getClusters()).isNotEmpty().doesNotContainNull();
  }

  @Test
  void noElementsInMultipleClusters() {
    // GIVEN
    SplittableRandom rnd = new SplittableRandom(815);
    List<PositionAndId> db = new ArrayList<>();
    for (int i = 0; i < 10000; i++) {
      db.add(new PositionAndId(i, rnd.nextInt(0, 5000), rnd.nextInt(0, 5000)));
    }
    PositionQueries<PositionAndId> positionQueries =
        new PositionQueries<>(db, PositionAndId::toPosition);

    StableDBScanner<PositionAndId> sut =
        new StableDBScanner<>(db, 3, positionAndId -> positionQueries.inRadius(positionAndId, 200));

    // WHEN
    Collection<Cluster<PositionAndId>> clusters = sut.scan(-1).getClusters();

    // THEN
    for (PositionAndId positionAndId : db) {
      assertThat(clusters.stream().filter(it -> it.elements.contains(positionAndId))).hasSize(1);
    }
  }

  @Test
  void exampleClusterTest600() throws IOException {
    // GIVEN

    List<PositionAndId> db = new ArrayList<>();
    for (int i = 0; i < EXAMPLE_POSITIONS.length; i++) {
      db.add(new PositionAndId(i, EXAMPLE_POSITIONS[i].x, EXAMPLE_POSITIONS[i].y));
    }
    PositionQueries<PositionAndId> positionQueries = new PositionQueries<>(db,
        PositionAndId::toPosition);

    StableDBScanner<PositionAndId> sut =
        new StableDBScanner<>(db, 3, positionAndId -> positionQueries.inRadius(positionAndId, 600));

    // WHEN
    Collection<Cluster<PositionAndId>> clusters = sut.scan(-1).getClusters();

    // THEN
    BufferedImage out =
        new BufferedImage(1000, 1400, BufferedImage.TYPE_3BYTE_BGR);
    Graphics g = out.createGraphics();
    SplittableRandom rng = new SplittableRandom(12);
    for (Cluster<PositionAndId> c : clusters) {
      g.setColor(new Color(rng.nextInt()));
      for (PositionAndId positionAndId : c.elements) {
        Position p = positionAndId.toPosition();
        g.fillArc(p.x - 210, p.y - 1810, 20, 20, 0, 360);
      }
    }
    ImageIO.write(out, "PNG", new File("build/cluster600.png"));
    assertThat(clusters).hasSize(2);
  }

  @Test
  void exampleClusterTest200() throws IOException {
    // GIVEN

    List<PositionAndId> db = new ArrayList<>();
    for (int i = 0; i < EXAMPLE_POSITIONS.length; i++) {
      db.add(new PositionAndId(i, EXAMPLE_POSITIONS[i].x, EXAMPLE_POSITIONS[i].y));
    }
    PositionQueries<PositionAndId> positionQueries = new PositionQueries<>(db,
        PositionAndId::toPosition);

    StableDBScanner<PositionAndId> sut =
        new StableDBScanner<>(db, 3, positionAndId -> positionQueries.inRadius(positionAndId, 200));

    // WHEN
    Collection<Cluster<PositionAndId>> clusters = sut.scan(-1).getClusters();

    // THEN
    BufferedImage out =
        new BufferedImage(1000, 1400, BufferedImage.TYPE_3BYTE_BGR);
    Graphics g = out.createGraphics();
    SplittableRandom rng = new SplittableRandom(12);
    for (Cluster<PositionAndId> c : clusters) {
      g.setColor(new Color(rng.nextInt()));
      for (PositionAndId positionAndId : c.elements) {
        Position p = positionAndId.toPosition();
        g.fillArc(p.x - 210, p.y - 1810, 20, 20, 0, 360);
      }
    }
    ImageIO.write(out, "PNG", new File("build/cluster200.png"));
    assertThat(clusters).hasSize(3);
  }
}

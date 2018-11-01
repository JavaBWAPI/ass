package org.bk.ass.cluster;

import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StableDBScannerTest {

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
}

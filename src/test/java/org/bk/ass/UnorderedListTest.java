package org.bk.ass;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class UnorderedListTest {

  private UnorderedList<String> sut = new UnorderedList<>();

  @Test
  public void shouldNotHoldReferencesAfterClearAndRelease() {
    // GIVEN
    sut.add("a");
    sut.add("b");
    sut.clear();

    // WHEN
    sut.clearReferences();

    // THEN
    assertThat(sut.items).containsOnlyNulls();
  }

  @Test
  public void shouldNotHoldReferencesAfterRemoveAndRelease() {
    // GIVEN
    sut.add("a");
    sut.removeAt(0);

    // WHEN
    sut.clearReferences();

    // THEN
    assertThat(sut.items).containsOnlyNulls();
  }

  @Test
  public void shouldAddElements() {
    // GIVEN

    // WHEN
    sut.add("test");

    // THEN
    assertThat(sut).containsExactly("test");
  }

  @Test
  public void shouldRemoveElements() {
    // GIVEN
    String toRemove = "test";
    sut.add(toRemove);
    sut.add("test2");

    // WHEN
    sut.remove(toRemove);

    // THEN
    assertThat(sut).containsExactly("test2");
  }

  @Test
  public void idioticBenchmark1() {
    Assertions.assertTimeout(Duration.ofSeconds(5), () -> {
      String element = "";
      for (int i = 0; i < 300000000; i++) {
        sut.add(element);
      }
      sut.clear();
    });
  }

  @Test
  public void idioticBenchmark2() {
    Assertions.assertTimeout(Duration.ofSeconds(5), () -> {
      String element = "";
      for (int i = 0; i < 300000000; i++) {
        sut.add(element);
      }
      for (int i = 0; i < 300000000; i++) {
        sut.removeAt(0);
      }
    });
  }
}

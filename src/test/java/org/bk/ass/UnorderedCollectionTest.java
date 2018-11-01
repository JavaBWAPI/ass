package org.bk.ass;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class UnorderedCollectionTest {

  private UnorderedCollection<Object> sut = new UnorderedCollection<>();

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
  public void shouldAddAllElements() {
    // GIVEN

    // WHEN
    sut.addAll(Arrays.asList("a", "b", "c"));

    // THEN
    assertThat(sut).containsExactly("a", "b", "c");
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
}

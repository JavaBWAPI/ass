package org.bk.ass;

import static org.assertj.core.api.Assertions.assertThat;

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

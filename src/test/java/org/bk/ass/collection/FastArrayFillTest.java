package org.bk.ass.collection;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

class FastArrayFillTest {

  @Test
  public void shouldFillAllByteArrayElements() {
    // GIVEN
    byte[] data = new byte[1234567];
    Arrays.fill(data, (byte) 1);

    // WHEN
    FastArrayFill.fillArray(data, (byte) 0);

    // THEN
    assertThat(data).doesNotContain(1);
  }

  @Test
  public void shouldFillAllObjectArrayElements() {
    // GIVEN
    Object[] data = new Object[1234567];
    Arrays.fill(data, 4);

    // WHEN
    FastArrayFill.fillArray(data, 0, data.length, null);

    // THEN
    assertThat(data).doesNotContain(4);
  }
}

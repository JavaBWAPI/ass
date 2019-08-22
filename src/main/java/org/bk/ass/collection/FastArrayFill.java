package org.bk.ass.collection;

public class FastArrayFill {

  private FastArrayFill() {
    // Utility class
  }

  public static void fillArray(byte[] array, byte value) {
    if (array.length == 0) {
      return;
    }
    int len = array.length;
    array[0] = value;
    for (int i = 1; i < len; i += i) {
      System.arraycopy(array, 0, array, i, Math.min(len - i, i));
    }
  }

  public static void fillArray(Object[] array, int fromIndex, int toIndex, Object value) {
    int len = toIndex - fromIndex;
    array[fromIndex] = value;
    for (int i = 1; i < len; i += i) {
      System.arraycopy(array, fromIndex, array, fromIndex + i, Math.min(len - i, i));
    }
  }
}

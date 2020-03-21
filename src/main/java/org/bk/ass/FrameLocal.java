package org.bk.ass;

import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * Buffers a computation for the duration of one frame. Can be used to lazily initialize computation
 * heavy fields which will not change its value during a single frame.
 *
 * @param <T>
 */
public class FrameLocal<T> {

  private final IntSupplier frameSupplier;
  private final Supplier<T> initializer;
  private int frame = Integer.MIN_VALUE;
  private T value;

  public FrameLocal(IntSupplier frameSupplier, Supplier<T> initializer) {
    this.initializer = initializer;
    this.frameSupplier = frameSupplier;
  }

  public final T get() {
    int currentFrame = frameSupplier.getAsInt();
    if (currentFrame != frame) {
      frame = currentFrame;
      value = initializer.get();
    }
    return value;
  }
}

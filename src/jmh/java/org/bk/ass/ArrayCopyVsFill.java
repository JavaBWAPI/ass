package org.bk.ass;

// @Measurement(iterations = 3, time = 3)
// @Warmup(iterations = 2, time = 5)
// @Fork(2)

/**
 * If you want to see how much Arrays.fill sucks, enable this test.
 */
public class ArrayCopyVsFill {
    //  @State(Scope.Thread)
    //  public static class MyState {
    //    byte[] collision = new byte[8192 * 8192 / 16 / 16];
    //    Object[] objects = new Object[1024 * 1024];
    //  }
    //
    //  @Benchmark
    //  public int copyByteArray(MyState state) {
    //    FastArrayFill.fillArray(state.collision, (byte) 0);
    //    return state.collision[9];
    //  }
    //
    //  @Benchmark
    //  public int fillByteArray(MyState state) {
    //    Arrays.fill(state.collision, (byte) 0);
    //    return state.collision[9];
    //  }
    //
    //  @Benchmark
    //  public Object copyObjectArray(MyState state) {
    //    FastArrayFill.fillArray(state.objects, null);
    //    return state.objects[9];
    //  }
    //
    //  @Benchmark
    //  public Object fillObjectArray(MyState state) {
    //    Arrays.fill(state.objects, null);
    //    return state.objects[9];
    //  }
}

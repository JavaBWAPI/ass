package org.bk.ass.bt;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;

class ExecutionContextTest {
  @Test
  void shouldFillUpStackTrace() {
    // GIVEN
    AtomicReference<List<TreeNode>> trace = new AtomicReference<>();
    TreeNode leafNode =
        new TreeNode() {
          @Override
          public void exec() {}

          @Override
          public void exec(ExecutionContext executionContext) {
            trace.set(executionContext.getNodeStack());
          }
        };
    Sequence level1Sequence = new Sequence(leafNode);
    Sequence sut = new Sequence(level1Sequence);

    // WHEN
    sut.exec(new ExecutionContext());

    // THEN
    assertThat(trace.get()).containsExactly(level1Sequence, leafNode);
  }
}

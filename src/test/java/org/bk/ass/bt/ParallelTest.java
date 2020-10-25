package org.bk.ass.bt;

import static org.assertj.core.api.Assertions.assertThat;
import static org.bk.ass.bt.DSL.parallel;

import java.util.ArrayList;
import java.util.List;
import org.bk.ass.bt.Parallel.Policy;
import org.junit.jupiter.api.Test;

class ParallelTest {

  @Test
  void shouldSucceedWithSelectorPolicyAndChildWithSuccess() {
    // GIVEN
    Parallel sut =
        new Parallel(
            Policy.SELECTOR,
            new LambdaNode(() -> NodeStatus.RUNNING),
            new LambdaNode(() -> NodeStatus.SUCCESS));

    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.SUCCESS);
  }

  @Test
  void shouldKeepRunningWithSelectorPolicyWithFailedChildButAlsoRunningChild() {
    // GIVEN
    Parallel sut =
        new Parallel(
            Policy.SELECTOR,
            new LambdaNode(() -> NodeStatus.FAILURE),
            new LambdaNode(() -> NodeStatus.RUNNING));
    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }

  @Test
  void shouldFailWithSequencePolicyAndChildWithFailure() {
    // GIVEN
    Parallel sut =
        new Parallel(
            Policy.SEQUENCE,
            new LambdaNode(() -> NodeStatus.RUNNING),
            new LambdaNode(() -> NodeStatus.FAILURE));
    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.FAILURE);
  }

  @Test
  void shouldKeepRunningWithSequencePolicyAndRunningChild() {
    // GIVEN
    Parallel sut =
        new Parallel(
            Policy.SEQUENCE,
            new LambdaNode(() -> NodeStatus.SUCCESS),
            new LambdaNode(() -> NodeStatus.RUNNING));

    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(sut.getStatus()).isEqualTo(NodeStatus.RUNNING);
  }

  @Test
  void orderOfExecutionTest() {
    // GIVEN
    List<Integer> execs = new ArrayList<>();
    Parallel sut = parallel(
        parallel(
            new TreeNode() {
              @Override
              protected void exec() {
                execs.add(4);
                success();
              }
            },
            new TreeNode() {
              @Override
              public double getUtility() {
                return 0.1;
              }

              @Override
              protected void exec() {
                execs.add(3);
                success();
              }
            }
        ),
        parallel(
            new TreeNode() {
              @Override
              public double getUtility() {
                return 1.0;
              }

              @Override
              protected void exec() {
                execs.add(1);
                success();
              }
            },
            new TreeNode() {
              @Override
              public double getUtility() {
                return 0.2;
              }

              @Override
              protected void exec() {
                execs.add(2);
                success();
              }
            }
        )
    );
    sut.init();

    // WHEN
    Executor.execute(sut);

    // THEN
    assertThat(execs).containsExactly(1, 2, 3, 4);
  }
}

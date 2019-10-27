package org.bk.ass.sim;

public class IntEvaluation {

  public final int evalA;
  public final int evalB;

  IntEvaluation(int evalA, int evalB) {
    this.evalA = evalA;
    this.evalB = evalB;
  }

  /**
   * Returns the delta of the evaluations for player a and b (evalA - evalB). Evaluating before and
   * after a simulation, the 2 resulting deltas can be used to determine a positive or negative
   * outcome.
   */
  public int delta() {
    return evalA - evalB;
  }

  public int dot(IntEvaluation other) {
    return evalA * other.evalA - evalB * other.evalB;
  }

  public int cross(IntEvaluation other) {
    return evalA * other.evalB - evalB * other.evalA;
  }

  /**
   * Subtracts another evaluation and returns the result. Evaluating before and after a simulation,
   * this can be used to calculate before - after. This in turn represents the loss each player had
   * in the meantime.
   */
  public IntEvaluation subtract(IntEvaluation other) {
    return new IntEvaluation(evalA - other.evalA, evalB - other.evalB);
  }
}

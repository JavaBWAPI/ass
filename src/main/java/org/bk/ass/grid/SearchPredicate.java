package org.bk.ass.grid;

/**
 * Predicate to be used in searches within ASS.
 */
public interface SearchPredicate<T> {

  /**
   * Returns {@link Result#ACCEPT} to stop searching and accept the result. Returns {@link
   * Result#CONTINUE} to skip the item and continue the search. Returns {@link Result#STOP} to stop
   * the search, potentially without a result. A special case is {@link Result#ACCEPT_CONTINUE},
   * which will remember the result but continue on until being stopped or the search completes.
   * Note: Only the last item that was accepted will be returned.
   */
  Result accept(T item);

  enum Result {
    ACCEPT,
    ACCEPT_CONTINUE,
    CONTINUE,
    STOP
  }
}

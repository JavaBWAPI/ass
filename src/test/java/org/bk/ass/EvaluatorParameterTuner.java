package org.bk.ass;

import static java.util.Objects.requireNonNull;

import io.jenetics.Chromosome;
import io.jenetics.DoubleChromosome;
import io.jenetics.DoubleGene;
import io.jenetics.Genotype;
import io.jenetics.Mutator;
import io.jenetics.SinglePointCrossover;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import org.bk.ass.Evaluator.Parameters;
import org.openbw.bwapi4j.org.apache.commons.lang3.time.StopWatch;
import org.openbw.bwapi4j.test.BWDataProvider;

public class EvaluatorParameterTuner {

  public static void main(String[] args) throws Exception {
    BWDataProvider.injectValues();

    Genotype<DoubleGene> genotype =
        Genotype.of(DoubleChromosome.of(0.0001, 3.0, 5), DoubleChromosome.of(0, 1000, 3));

    Function<Genotype<DoubleGene>, Integer> eval =
        gt -> {
          EvaluatorTest test = new EvaluatorTest();
          test.evaluator =
              new Evaluator(
                  new Parameters(
                      gt.stream()
                          .flatMap(Chromosome::stream)
                          .mapToDouble(DoubleGene::doubleValue)
                          .map(EvaluatorParameterTuner::round)
                          .toArray()));
          return hits(test);
        };

    Engine<DoubleGene, Integer> engine =
        Engine.builder(eval, genotype)
            .alterers(new Mutator<>(0.7), new SinglePointCrossover<>(0.2))
            .build();
    StopWatch stopWatch = new StopWatch();
    stopWatch.start();
    EvolutionResult<DoubleGene, Integer> result =
        engine
            .stream()
            .limit(
                Limits.<Integer>bySteadyFitness(200000)
                    .and(new FitnessThresholdLimit<>(TEST_METHODS.length - 1)))
            .collect(EvolutionResult.toBestEvolutionResult());
    stopWatch.stop();
    System.out.println("GA time: " + stopWatch);
    System.out.println("Best result " + result.getBestFitness() + "/" + TEST_METHODS.length);
    Genotype<DoubleGene> bestGenotype = result.getBestPhenotype().getGenotype();
    System.out.println(
        bestGenotype
            .stream()
            .flatMap(Chromosome::stream)
            .mapToDouble(DoubleGene::doubleValue)
            .map(EvaluatorParameterTuner::round)
            .mapToObj(String::valueOf)
            .collect(Collectors.joining(", ", "new double[] {", "}")));
  }

  private static double round(double v) {
    return Math.round(v * 8000) / 8000.0;
  }

  private static final Method[] TEST_METHODS =
      Arrays.stream(EvaluatorTest.class.getDeclaredMethods())
          .filter(
              m ->
                  m.getReturnType().equals(Void.TYPE)
                      && m.getParameterCount() == 0
                      && !Modifier.isStatic(m.getModifiers()))
          .toArray(Method[]::new);

  private static int hits(EvaluatorTest test) {
    for (int i = 0; i < TEST_METHODS.length; i++) {
      try {
        TEST_METHODS[i].invoke(test);
      } catch (Exception e) {
        return i;
      }
    }
    return TEST_METHODS.length;
  }

  // Fix from https://github.com/jenetics/jenetics/pull/421
  static final class FitnessThresholdLimit<C extends Comparable<? super C>>
      implements Predicate<EvolutionResult<?, C>> {

    private final C _threshold;
    private boolean _proceed = true;

    FitnessThresholdLimit(final C threshold) {
      _threshold = requireNonNull(threshold);
    }

    @Override
    public boolean test(final EvolutionResult<?, C> result) {
      final boolean proceed =
          _proceed && result.getOptimize().compare(_threshold, result.getBestFitness()) >= 0;
      try {
        return _proceed;
      } finally {
        _proceed = proceed;
      }
    }
  }
}

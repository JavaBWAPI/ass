package org.bk.ass.sim;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionResult;
import io.jenetics.engine.Limits;
import io.jenetics.util.IntRange;
import org.bk.ass.sim.Evaluator.Parameters;
import org.openbw.bwapi4j.org.apache.commons.lang3.time.StopWatch;
import org.openbw.bwapi4j.test.BWDataProvider;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EvaluatorParameterTuner {
  private static int PARAMS = 7;

  public static void main(String[] args) throws Exception {
    BWDataProvider.injectValues();

    SplittableRandom prng = new SplittableRandom();
    List<Par> candidates = new ArrayList<>();
    int[] miss = new int[PARAMS];
    for (int i = 0; i < 20; i++) {
      double[] d = new double[PARAMS];
      for (int j = 0; j < d.length; j++) {
        d[j] = prng.nextDouble(0.00001, 10.0);
      }
      Par e = new Par(d);
      if (e.score == TEST_METHODS.length) {
        System.out.println(d);
        return;
      }
      candidates.add(e);
    }
    int best = 0;
    for (int i = 0; i < 100000; i++) {
      int ci = prng.nextInt(candidates.size() * 12 / 10);
      Par sub;
      if (ci < candidates.size()) {
        Par par = candidates.get(ci);

        int index = prng.nextInt(par.d.length);

        sub = IntRange.of(0, 5).stream()
                .mapToObj(d -> {
                  double[] next = Arrays.copyOf(par.d, par.d.length);
                  next[index] = prng.nextDouble(0.0001, 10.0);
                  return new Par(next);
                }).max(Comparator.comparingInt(p -> p.score)).get();
        if (sub.score == par.score) {
          miss[index]++;
          continue;
        }
      } else {
        double[] d = new double[PARAMS];
        for (int j = 0; j < d.length; j++) {
          d[j] = prng.nextDouble(0.0001, 10.0);
        }
        sub = new Par(d);
      }
      int min = 0;
      for (int j = 1; j < candidates.size(); j++) {
        if (candidates.get(j).score + prng.nextDouble() < candidates.get(min).score + prng.nextDouble()) {
          min = j;
        }
      }
      if (sub.score >= candidates.get(min).score) {
        candidates.set(min, sub);
        if (sub.score > best) {
          best = sub.score;
          System.out.println("Best: " + best);
          System.out.println(Arrays.stream(sub.d)
                  .mapToObj(String::valueOf)
                  .collect(Collectors.joining(", ", "new double[] {", "}")));
        }
      }
      if (sub.score == TEST_METHODS.length) {
        System.out.println(Arrays.stream(sub.d)
                .mapToObj(String::valueOf)
                .collect(Collectors.joining(", ", "new double[] {", "}")));
        return;
      }
    }

    System.out.println("NOT FOUND");
    System.out.println(Arrays.toString(miss));
    System.exit(0);

    Genotype<DoubleGene> genotype =
            Genotype.of(DoubleChromosome.of(0.0001, 3.0, 6), DoubleChromosome.of(1, 1000, 1));

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
        engine.stream()
            .limit(
                Limits.<Integer>bySteadyFitness(200000)
                    .and(Limits.byFitnessThreshold(TEST_METHODS.length - 1)))
            .collect(EvolutionResult.toBestEvolutionResult());
    stopWatch.stop();
    System.out.println("GA time: " + stopWatch);
    System.out.println("Best result " + result.getBestFitness() + "/" + TEST_METHODS.length);
    Genotype<DoubleGene> bestGenotype = result.getBestPhenotype().getGenotype();
    System.out.println(
        bestGenotype.stream()
            .flatMap(Chromosome::stream)
            .mapToDouble(DoubleGene::doubleValue)
            .map(EvaluatorParameterTuner::round)
            .mapToObj(String::valueOf)
            .collect(Collectors.joining(", ", "new double[] {", "}")));
  }

  private static double round(double v) {
    return Math.round(v * 1000) / 1000.0;
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
    int result = 0;
    for (int i = 0; i < TEST_METHODS.length; i++) {
      try {
        TEST_METHODS[i].invoke(test);
        result++;
      } catch (Exception e) {
        // Ignore
      }
    }
    return result;
  }

  private static class Par {
    final double[] d;
    final int score;

    private Par(double[] d) {
      this.d = d;
      this.score = eval();
    }

    private int eval() {
      EvaluatorTest test = new EvaluatorTest();
      test.evaluator = new Evaluator(new Parameters(d));
      return hits(test);
    }

    @Override
    public String toString() {
      return score + " : " + Arrays.toString(d);
    }
  }
}

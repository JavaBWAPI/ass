package org.bk.ass.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bk.ass.UnorderedCollection;
import org.bk.ass.query.UnitFinder;

/**
 * DBScan which processes the DB in chunks. Element clusters are generally stable: After an
 * iteration the cluster of an element is usually the same as before. An exception is if a cluster
 * is split into multiple clusters. In that case, only a part of the elements are retained in the
 * previous cluster. <br> Make sure to call {@link #scan(int)} to actually perform clustering.
 * Calling {@link #updateDB(UnitFinder, int)} or {@link #updateDB(Collection, Function)} will reset
 * the clustering process. Make sure to always cluster all elements or to check if the clustering is
 * done with {@link #isComplete()}.<br> The clustering is ongoing, each call to {@link #scan(int)}
 * will continue or restart the clustering.
 */
public class StableDBScanner<U> {

  private final Collection<U> db;
  private final int minPoints;
  private Function<U, Collection<U>> inRadiusFinder;

  private final UnorderedCollection<WrappedElement<U>> remainingDbEntries =
      new UnorderedCollection<>();
  private final UnorderedCollection<WrappedElement<U>> horizon = new UnorderedCollection<>();
  private Map<U, WrappedElement<U>> elementToWrapper = new HashMap<>();
  private Map<U, Cluster<U>> elementToCluster = Collections.emptyMap();
  private ClusterSurrogate<U> currentCluster;

  public StableDBScanner(UnitFinder<U> unitFinder, int minPoints, int radius) {
    this(unitFinder, minPoints, u -> unitFinder.inRadius(u, radius));
  }

  public StableDBScanner(
      Collection<U> db, int minPoints, Function<U, Collection<U>> inRadiusFinder) {
    this.db = db;
    this.minPoints = minPoints;
    this.inRadiusFinder = inRadiusFinder;
  }

  public Cluster<U> getClusterOf(U element) {
    return elementToCluster.get(element);
  }

  public Collection<Cluster<U>> getClusters() {
    return new HashSet<>(elementToCluster.values());
  }

  public void updateDB(Collection<U> db, Function<U, Collection<U>> inRadiusFinder) {
    this.inRadiusFinder = inRadiusFinder;
    this.db.clear();
    this.db.addAll(db);
  }

  public void updateDB(UnitFinder<U> unitFinder, int radius) {
    updateDB(unitFinder, u -> unitFinder.inRadius(u, radius));
  }

  /**
   * @return true, if the last {@link #scan(int)} assigned all elements of DB to clusters
   */
  public final boolean isComplete() {
    return remainingDbEntries.isEmpty();
  }

  /**
   * Scans the current DB and assigns elements to clusters.
   *
   * @param maxMarkedElements maximum number of elements to add to a cluster in this run, -1 to
   * assign all elements
   */
  public StableDBScanner<U> scan(int maxMarkedElements) {
    if (isComplete()) {
      reset();
    }

    while ((!remainingDbEntries.isEmpty() || !horizon.isEmpty()) && maxMarkedElements != 0) {
      while (!horizon.isEmpty() && maxMarkedElements-- != 0) {
        WrappedElement<U> q = horizon.removeAt(0);
        if (q.cluster == null) {
          setCluster(q, currentCluster);
        }
        if (q.marked) {
          continue;
        }
        setCluster(q, currentCluster);
        List<WrappedElement<U>> qn = elementsWithinRadius(q);
        if (qn.size() >= minPoints) {
          horizon.addAll(qn);
        }
      }

      if (horizon.isEmpty() && !remainingDbEntries.isEmpty()) {
        WrappedElement<U> p = remainingDbEntries.removeAt(0);
        if (!p.marked) {
          List<WrappedElement<U>> n = elementsWithinRadius(p);
          if (n.size() >= minPoints) {
            currentCluster = p.cluster == null ? new ClusterSurrogate<>() : p.cluster;
            // Remove associations of this cluster, elements will be re-associated
            currentCluster.elements.forEach(w -> w.cluster = null);
            currentCluster.elements.clear();
            horizon.addAll(n);
          } // else leave unmarked, don't force it to be NOISE
        }
      }
    }

    if (remainingDbEntries.isEmpty()) {
      noiseToNewClusters();
      updateElementClusters();
    }
    return this;
  }

  private void updateElementClusters() {
    elementToCluster =
        elementToWrapper
            .entrySet()
            .stream()
            .collect(
                Collectors.toMap(
                    Entry::getKey,
                    e -> {
                      ClusterSurrogate<U> clusterSurrogate = e.getValue().cluster;
                      if (clusterSurrogate == null) {
                        Cluster<U> cluster = new Cluster<>();
                        cluster.elements.add(e.getKey());
                        return cluster;
                      }
                      Cluster<U> cluster = clusterSurrogate.cluster;
                      cluster.elements.clear();
                      clusterSurrogate.elements.forEach(w -> cluster.elements.add(w.element));
                      return cluster;
                    }));
  }

  private void noiseToNewClusters() {
    for (WrappedElement<U> it : elementToWrapper.values()) {
      if (!it.marked) {
        if (it.cluster == null) {
          ClusterSurrogate<U> clusterSurrogate = new ClusterSurrogate<>();
          clusterSurrogate.elements.add(it);
          it.cluster = clusterSurrogate;
        } else {
          // Split up! This one gets the original cluster, the rest gets reassigned
          for (WrappedElement<U> other : it.cluster.elements) {
            if (other != it) {
              other.cluster = null;
            }
          }
          it.cluster.elements = Collections.singleton(it);
        }
      }
    }
  }

  private void reset() {
    for (WrappedElement<U> wrappedElement : elementToWrapper.values()) {
      wrappedElement.marked = false;
    }
    for (U element : db) {
      remainingDbEntries.add(elementToWrapper.computeIfAbsent(element, WrappedElement::new));
    }
    elementToWrapper.keySet().retainAll(db);
  }

  private List<WrappedElement<U>> elementsWithinRadius(WrappedElement<U> q) {
    return inRadiusFinder
        .apply(q.element)
        .stream()
        .map(u -> elementToWrapper.get(u))
        .filter(Objects::nonNull) // Yet unknown elements might be returned, ignore them
        .collect(Collectors.toList());
  }

  private void setCluster(WrappedElement<U> element, ClusterSurrogate<U> newCluster) {
    element.marked = true;
    element.cluster = newCluster;
    newCluster.elements.add(element);
  }

  private static class ClusterSurrogate<U> {

    Collection<WrappedElement<U>> elements = new ArrayList<>();
    Cluster<U> cluster = new Cluster<>();
  }

  private static class WrappedElement<U> {

    U element;
    ClusterSurrogate<U> cluster;
    boolean marked;

    WrappedElement(U element) {
      this.element = element;
    }
  }
}

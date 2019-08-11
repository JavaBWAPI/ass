package org.bk.ass.cluster;

import org.bk.ass.collection.UnorderedCollection;
import org.bk.ass.query.PositionQueries;

import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * DBScan which processes the DB in chunks. Element clusters are generally stable: After an
 * iteration the cluster of an element is usually the same as before. An exception is if a cluster
 * is split into multiple clusters. In that case, only a part of the elements are retained in the
 * previous cluster. <br>
 * Make sure to call {@link #scan(int)} to actually perform clustering. Calling {@link
 * #updateDB(PositionQueries, int)} or {@link #updateDB(Collection, Function)} will reset the clustering
 * process. Make sure to always cluster all elements or to check if the clustering is done with
 * {@link #isComplete()}.<br>
 * The clustering is ongoing, each call to {@link #scan(int)} will continue or restart the
 * clustering.<br>
 * When updating the DB be aware that elements are compared with <code>==</code> and not <code>
 * equals</code>.
 */
public class StableDBScanner<U> {

  private final Collection<U> db;
  private final int minPoints;
  private Function<U, Collection<U>> inRadiusFinder;

  private final UnorderedCollection<WrappedElement<U>> remainingDbEntries =
      new UnorderedCollection<>();
  private final UnorderedCollection<WrappedElement<U>> horizon = new UnorderedCollection<>();
  private Map<U, WrappedElement<U>> elementToWrapper = Collections.emptyMap();
  private Map<U, Cluster<U>> elementToCluster = Collections.emptyMap();
  private ClusterSurrogate<U> currentCluster;

  public StableDBScanner(PositionQueries<U> positionQueries, int minPoints, int radius) {
    this(positionQueries, minPoints, u -> positionQueries.inRadius(u, radius));
  }

  public StableDBScanner(int minPoints) {
    this(new ArrayList<>(), minPoints, unused -> Collections.emptyList());
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

  public StableDBScanner<U> updateDB(Collection<U> db, Function<U, Collection<U>> inRadiusFinder) {
    this.inRadiusFinder = inRadiusFinder;
    this.db.clear();
    this.db.addAll(db);
    return this;
  }

  public StableDBScanner<U> updateDB(PositionQueries<U> positionQueries, int radius) {
    return updateDB(positionQueries, u -> positionQueries.inRadius(u, radius));
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
   *     assign all elements
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
        q.cluster.elements.remove(q);
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
      remainingDbEntries.clearReferences();
    }
    return this;
  }

  private void updateElementClusters() {
    elementToCluster =
            elementToWrapper.entrySet().stream()
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
          it.cluster.elements.clear();
          it.cluster.elements.add(it);
        }
      }
    }
  }

  private void reset() {
    Map<U, WrappedElement<U>> newElementToWrapper = new IdentityHashMap<>();
    for (U element : db) {
      WrappedElement<U> wrappedElement = elementToWrapper.get(element);
      if (wrappedElement == null) {
        wrappedElement = new WrappedElement<>(element);
      } else {
        wrappedElement.marked = false;
      }
      remainingDbEntries.add(wrappedElement);
      newElementToWrapper.put(element, wrappedElement);
    }
    elementToWrapper = newElementToWrapper;
  }

  private List<WrappedElement<U>> elementsWithinRadius(WrappedElement<U> q) {
    return inRadiusFinder.apply(q.element).stream()
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

    final Collection<WrappedElement<U>> elements = new ArrayList<>();
    final Cluster<U> cluster = new Cluster<>();
  }

  private static class WrappedElement<U> {

    final U element;
    ClusterSurrogate<U> cluster;
    boolean marked;

    WrappedElement(U element) {
      this.element = element;
    }
  }
}

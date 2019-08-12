package org.bk.ass.query;

import org.bk.ass.path.Position;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.bk.ass.query.Distances.EUCLIDEAN_DISTANCE;

/**
 * Collection with fast 2D lookups.
 *
 * @param <U>
 */
public class PositionQueries<U> extends AbstractCollection<U> {
  private int size;
  private Node root;
  private Function<U, Position> positionExtractor;
  private DistanceProvider distanceProvider;

  public PositionQueries(Collection<U> elements, Function<U, Position> positionExtractor) {
    this(elements, positionExtractor, EUCLIDEAN_DISTANCE);
  }

  public PositionQueries(
      Collection<U> elements,
      Function<U, Position> positionExtractor,
      DistanceProvider distanceProvider) {
    this.distanceProvider = distanceProvider;
    if (elements == null) throw new IllegalArgumentException("elements must not be null");
    if (positionExtractor == null)
      throw new IllegalArgumentException("positionExtractor must not be null");
    this.positionExtractor = positionExtractor;
    root = new TreeBuilder(elements.toArray()).makeTree(0, elements.size() - 1, true);
    this.size = elements.size();
  }

  public U nearest(int x, int y) {
    return new NearestSearcher(x, y).search();
  }

  public U nearest(int x, int y, Predicate<U> criteria) {
    return new NearestSearcher(x, y, criteria).search();
  }

  public Collection<U> inArea(int ax, int ay, int bx, int by) {
    if (ax > bx || ay > by)
      throw new IllegalArgumentException("ax should be <= bx and ay should be <= by");
    return new RectAreaSearcher(ax, ay, bx, by).search();
  }

  public Collection<U> inArea(int ax, int ay, int bx, int by, Predicate<U> criteria) {
    return new RectAreaSearcher(ax, ay, bx, by, criteria).search();
  }

  public Collection<U> inRadius(U u, int radius) {
    if (radius <= 0) throw new IllegalArgumentException("radius should be > 0");
    Position position = positionExtractor.apply(u);
    return new RadiusAreaSearcher(position.x, position.y, radius).search();
  }

  public Collection<U> inRadius(int x, int y, int radius) {
    return new RadiusAreaSearcher(x, y, radius).search();
  }

  public Collection<U> inRadius(int x, int y, int radius, Predicate<U> criteria) {
    return new RadiusAreaSearcher(x, y, radius, criteria).search();
  }

  @Override
  public Iterator<U> iterator() {
    List<U> items = new ArrayList<>();
    root.addValuesTo(items);
    return items.iterator();
  }

  @Override
  public int size() {
    return size;
  }

  /**
   * Removes the given element, the internal data structure will not be changed - queries will
   * therefore not be faster.
   */
  @Override
  public boolean remove(Object o) {
    Node current = root;
    boolean xDim = true;
    while (current != null) {
      if (current.values != null && current.values.length > 0) {
        Object[] replacement = new Object[current.values.length - 1];
        int i;
        for (i = 0; i < replacement.length && !Objects.equals(current.values[i], o); i++) {
          replacement[i] = current.values[i];
        }
        if (i == replacement.length) return false;
        for (; i < replacement.length; i++) {
          replacement[i] = current.values[i + 1];
        }
        current.values = replacement;
        size--;
        return true;
      }
      Position pos = positionExtractor.apply((U) o);
      if (xDim && pos.x <= current.p || !xDim && pos.y <= current.p) {
        current = current.left;
      } else {
        current = current.right;
      }
      xDim = !xDim;
    }
    return false;
  }

  /**
   * Removes the given elements, the internal data structure will not be changed - queries will
   * therefore not be faster.
   */
  @Override
  public boolean removeAll(Collection<?> c) {
    boolean removed = false;
    for (Object o : c) {
      removed |= remove(o);
    }
    return removed;
  }

  class NearestSearcher {
    private Predicate<U> criteria;
    private Object best;
    private int bestDst = Integer.MAX_VALUE;
    private int x;
    private int y;

    NearestSearcher(int x, int y) {
      this.x = x;
      this.y = y;
    }

    NearestSearcher(int x, int y, Predicate<U> criteria) {
      this(x, y);
      this.criteria = criteria;
    }

    U search() {
      nearestX(root);
      return (U) best;
    }

    private void nearestX(Node node) {
      if (node.values != null) {
        updateBestFrom(node.values);
      } else if (x <= node.p) {
        nearestY(node.left);
        if (node.p - x < bestDst) nearestY(node.right);
      } else {
        nearestY(node.right);
        if (x - node.p <= bestDst) nearestY(node.left);
      }
    }

    private void nearestY(Node node) {
      if (node.values != null) {
        updateBestFrom(node.values);
      } else if (y <= node.p) {
        nearestX(node.left);
        if (node.p - y < bestDst) nearestX(node.right);
      } else {
        nearestX(node.right);
        if (y - node.p <= bestDst) nearestX(node.left);
      }
    }

    private void updateBestFrom(Object[] values) {
      for (Object v : values) {
        U u = (U) v;
        if (criteria != null && !criteria.test(u)) continue;
        Position pos = positionExtractor.apply(u);
        int dst = distanceProvider.distance(pos.x, pos.y, x, y);
        if (dst < bestDst) {
          bestDst = dst;
          best = v;
        }
      }
    }
  }

  class RectAreaSearcher extends AreaSearcher {
    RectAreaSearcher(int ax, int ay, int bx, int by) {
      super(ax, ay, bx, by);
    }

    RectAreaSearcher(int ax, int ay, int bx, int by, Predicate<U> criteria) {
      super(ax, ay, bx, by, criteria);
    }

    @Override
    boolean accept(Position pos) {
      return pos.x >= ax && pos.y >= ay && pos.x <= bx && pos.y <= by;
    }
  }

  class RadiusAreaSearcher extends AreaSearcher {

    private final int x;
    private final int y;
    private final int radius;

    RadiusAreaSearcher(int x, int y, int radius) {
      this(x, y, radius, null);
    }

    RadiusAreaSearcher(int x, int y, int radius, Predicate<U> criteria) {
      super(x - radius, y - radius, x + radius, y + radius, criteria);
      this.x = x;
      this.y = y;
      this.radius = radius;
    }

    @Override
    boolean accept(Position pos) {
      return distanceProvider.distance(pos.x, pos.y, x, y) <= radius;
    }
  }

  abstract class AreaSearcher {
    final int ax;
    final int ay;
    final int bx;
    final int by;
    private List<U> result = new ArrayList<>();
    private Predicate<U> criteria;

    AreaSearcher(int ax, int ay, int bx, int by) {
      this.ax = ax;
      this.ay = ay;
      this.bx = bx;
      this.by = by;
    }

    AreaSearcher(int ax, int ay, int bx, int by, Predicate<U> criteria) {
      this(ax, ay, bx, by);
      this.criteria = criteria;
    }

    Collection<U> search() {
      searchX(root);
      return result;
    }

    private void searchX(Node node) {
      if (node.values != null) {
        updateResultFrom(node.values);
        return;
      }
      if (ax <= node.p) {
        searchY(node.left);
      }
      if (bx > node.p) {
        searchY(node.right);
      }
    }

    private void updateResultFrom(Object[] values) {
      for (Object v : values) {
        U u = (U) v;
        if (criteria != null && !criteria.test(u)) continue;
        Position pos = positionExtractor.apply(u);
        if (accept(pos)) result.add(u);
      }
    }

    abstract boolean accept(Position pos);

    private void searchY(Node node) {
      if (node.values != null) {
        updateResultFrom(node.values);
        return;
      }
      if (ay <= node.p) {
        searchX(node.left);
      }
      if (by > node.p) {
        searchX(node.right);
      }
    }
  }

  static class Pivot {
    final int value;
    final int index;

    Pivot(int value, int index) {
      this.value = value;
      this.index = index;
    }
  }

  class TreeBuilder {
    private final Object[] data;

    TreeBuilder(Object[] data) {
      this.data = data;
    }

    private Position pos(int i) {
      return positionExtractor.apply((U) data[i]);
    }

    private Pivot partitionX(int start, int end) {
      int mid = (start + end) / 2;
      if (pos(mid).x < pos(start).x) swap(mid, start);
      if (pos(end).x < pos(start).x) swap(start, end);
      if (pos(mid).x < pos(end).x) swap(mid, end);
      int pivot = pos(end).x;
      while (true) {
        while (pos(start).x <= pivot) start++;
        while (pos(end).x > pivot) end--;
        if (start >= end) return new Pivot(pivot, end);
        swap(start, end);
        start++;
        end--;
      }
    }

    private Pivot partitionY(int start, int end) {
      int mid = (start + end) / 2;
      if (pos(mid).y < pos(start).y) swap(mid, start);
      if (pos(end).y < pos(start).y) swap(start, end);
      if (pos(mid).y < pos(end).y) swap(mid, end);
      int pivot = pos(end).y;
      while (true) {
        while (pos(start).y < pivot) start++;
        while (pos(end).y > pivot) end--;
        if (start >= end) return new Pivot(pivot, end);
        swap(start, end);
        start++;
        end--;
      }
    }

    private void swap(int a, int b) {
      Object tmp = data[a];
      data[a] = data[b];
      data[b] = tmp;
    }

    private Node makeTree(int start, int end, boolean xDim) {
      // 15 elements seems to be the sweet spot for ~1000 positions
      if (end - start < 15) return new Node(Arrays.copyOfRange(data, start, end + 1));
      Pivot pivot;
      if (xDim) {
        pivot = partitionX(start, end);
      } else {
        pivot = partitionY(start, end);
      }
      Node mNode = new Node(pivot.value);
      mNode.left = makeTree(start, pivot.index, !xDim);
      mNode.right = makeTree(pivot.index + 1, end, !xDim);
      return mNode;
    }
  }

  static class Node {
    Object[] values;
    final int p;
    Node left;

    Node right;

    Node(int p) {
      this.p = p;
    }

    Node(Object[] values) {
      p = -1;
      this.values = values;
    }

    <U> void addValuesTo(List<U> dst) {
      if (values != null) dst.addAll((List<U>) Arrays.asList(values));
      else {
        left.addValuesTo(dst);
        right.addValuesTo(dst);
      }
    }
  }
}

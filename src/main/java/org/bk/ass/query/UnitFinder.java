package org.bk.ass.query;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.AbstractCollection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Helper class to make area and range queries for "positions". While using an instance all
 * positions and ids *must not* change. That usually means a new instance will have to be created
 * for each frame it is going to be used. <br>
 * It should generally be created once at the start of a frame to be used for all queries. <br>
 * Example for usages: <br>
 *
 * <ul>
 *   <li>Initialize with workers only, to find the closest worker for some position.
 *   <li>Initialize with enemy attackers to find the closest threat or all threats within a
 *       range/area.
 * </ul>
 *
 * @param <U> The "unit" type, basically anything that has an id and a position
 */
public class UnitFinder<U> extends AbstractCollection<U> {

  private final TreeMap<PositionAndId, U> map = new TreeMap<>();
  private final Function<U, PositionAndId> positionAndIdExtractor;
  private final DistanceProvider distanceProvider;
  private static final DistanceProvider EUCLIDEAN_DISTANCE =
      (ax, ay, bx, by) -> (int) Math.sqrt((bx - ax) * (bx - ax) + (by - ay) * (by - ay));
  /**
   * When using this, all radius queries need to be made with the squared radius
   */
  public static final DistanceProvider EUCLIDEAN_DISTANCE_SQUARED =
      (ax, ay, bx, by) -> (bx - ax) * (bx - ax) + (by - ay) * (by - ay);
  /** Use this to query using the distance approximation used in OpenBW */
  public static final DistanceProvider BW_DISTANCE_APPROXIMATION =
      (ax, ay, bx, by) -> {
        int min = Math.abs(ax - bx);
        int max = Math.abs(ay - by);
        int minCalc;
        if (max < min) {
          minCalc = max;
          max = min;
          min = minCalc;
        }

        if (min < max >> 2) {
          return max;
        } else {
          minCalc = 3 * min >> 3;
          return (minCalc >> 5) + minCalc + max - (max >> 4) - (max >> 6);
        }
      };

  /**
   * @param units the units to search in
   * @param positionAndIdExtractor helper to extract {@link PositionAndId} from a unit
   */
  public UnitFinder(Collection<U> units, Function<U, PositionAndId> positionAndIdExtractor) {
    this(units, positionAndIdExtractor, EUCLIDEAN_DISTANCE);
  }

  public UnitFinder(Function<U, PositionAndId> positionAndIdExtractor) {
    this(Collections.emptyList(), positionAndIdExtractor);
  }

  /**
   * @param units the units to search in
   * @param positionAndIdExtractor helper to extract {@link PositionAndId} from a unit
   * @param distanceProvider used to determine the distance between two points
   * @see {@link #EUCLIDEAN_DISTANCE_SQUARED} and {@link #BW_DISTANCE_APPROXIMATION}
   */
  public UnitFinder(
      Collection<U> units,
      Function<U, PositionAndId> positionAndIdExtractor,
      DistanceProvider distanceProvider) {
    this.positionAndIdExtractor = positionAndIdExtractor;
    this.distanceProvider = distanceProvider;
    addAll(units);
  }

  @Override
  public boolean add(U u) {
    U previousValue = map.put(positionAndIdExtractor.apply(u), u);
    if (previousValue != null) {
      throw new IllegalStateException("Multiple units with same id and position found!");
    }
    return true;
  }

  @Override
  public boolean remove(Object o) {
    return map.remove(positionAndIdExtractor.apply((U) o)) != null;
  }

  @Override
  public void clear() {
    map.clear();
  }

  /**
   * Returns a collection of all units in the area with top left (ax, ay) and bottom right (bx, by)
   * including those on the boundary.
   */
  public Collection<U> inArea(int ax, int ay, int bx, int by) {
    return subMapOfArea(ax, ay, bx, by).map(Entry::getValue).collect(Collectors.toList());
  }

  private Stream<Entry<PositionAndId, U>> subMapOfArea(int ax, int ay, int bx, int by) {
    return map.subMap(
        new PositionAndId(-1, ax, ay), true, new PositionAndId(Integer.MAX_VALUE, bx, by), true)
        .entrySet()
        .stream()
        .filter(u -> u.getKey().y <= by && u.getKey().y >= ay);
  }

  /**
   * Returns all units with a radius <= the given radius (as determined by the given {@link
   * DistanceProvider}).
   */
  public Collection<U> inRadius(int x, int y, int radius) {
    ArrayList<U> result = new ArrayList<>();
    subMapOfArea(x - radius, y - radius, x + radius, y + radius)
        .forEach(
            entry -> {
              PositionAndId pos = entry.getKey();
              if (distanceProvider.distance(pos.x, pos.y, x, y) <= radius) {
                result.add(entry.getValue());
              }
            });
    return result;
  }

  /**
   * Returns the closest unit to the given position or {@link Optional#empty()} if no units are
   * present.
   */
  public Optional<U> closestTo(int x, int y) {
    PositionAndId query = new PositionAndId(-1, x, y);
    int squareHSize = Integer.MAX_VALUE;
    Entry<PositionAndId, U> lowerBound = map.lowerEntry(query);
    if (lowerBound != null) {
      int lx = lowerBound.getKey().x;
      int ly = lowerBound.getKey().y;
      squareHSize = max(abs(lx - x), abs(ly - y));
    }
    Entry<PositionAndId, U> higherBound = map.higherEntry(query);
    if (higherBound != null) {
      int hx = higherBound.getKey().x;
      int hy = higherBound.getKey().y;
      squareHSize = min(squareHSize, max(abs(hx - x), abs(hy - y)));
    }
    if (squareHSize == Integer.MAX_VALUE) {
      return Optional.empty();
    }
    return subMapOfArea(x - squareHSize, y - squareHSize, x + squareHSize, y + squareHSize)
        .min(Comparator.comparing(e -> distanceProvider.distance(e.getKey().x, e.getKey().y, x, y)))
        .map(Entry::getValue);
  }

  @Override
  public Iterator<U> iterator() {
    return map.values().iterator();
  }

  @Override
  public int size() {
    return map.size();
  }
}

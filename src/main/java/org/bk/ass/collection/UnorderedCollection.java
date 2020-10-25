package org.bk.ass.collection;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A collection implementation that does not guarantee the order of elements stays constant of
 * manipulation. All operations generally use identity and not equality!
 */
public class UnorderedCollection<T> extends AbstractCollection<T> {

  T[] items;
  private int size;

  public UnorderedCollection() {
    this(16);
  }

  public UnorderedCollection(int capacity) {
    items = (T[]) new Object[capacity];
  }

  public UnorderedCollection(Collection<T> source) {
    this((int) (source.size() * 7L / 4));
    size = source.size();
    items = (T[]) Arrays.copyOf(source.toArray(), size);
  }

  public void clearReferences() {
    if (size < items.length) FastArrayFill.fillArray(items, size, items.length, null);
  }

  @Override
  public boolean add(T value) {
    if (size == items.length) {
      resize((int) (size * 7L / 4));
    }
    items[size++] = value;
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends T> elementsToAdd) {
    if (elementsToAdd.isEmpty()) {
      return false;
    }
    int targetSize = elementsToAdd.size() + size;
    if (targetSize >= items.length) {
      resize(targetSize * 7 / 4);
    }
    Object[] src = elementsToAdd.toArray();
    System.arraycopy(src, 0, items, size, src.length);
    size += src.length;
    return true;
  }

  @Override
  public boolean remove(Object value) {
    for (int i = 0; i < size; i++) {
      if (items[i] == value) {
        swapRemove(i);
        return true;
      }
    }
    return false;
  }

  public T swapRemove(int index) {
    if (index >= size) {
      throw new IndexOutOfBoundsException("index " + index + " must be >= 0 and < " + size);
    }
    T removed = items[index];
    T last = items[--size];
    items[index] = last;
    return removed;
  }

  public T removeMax(Comparator<T> comparator) {
    if (size == 0) {
      throw new NoSuchElementException();
    }
    int maxIndex = -1;
    T max = null;
    for (int i = size - 1; i >= 0; i--) {
      T current = items[i];
      if (max == null || comparator.compare(current, max) > 0) {
        max = current;
        maxIndex = i;
      }
    }
    return swapRemove(maxIndex);
  }

  private void resize(int newCapacity) {
    int capacityToUse = max(8, newCapacity);
    T[] newItems = (T[]) new Object[capacityToUse];
    System.arraycopy(items, 0, newItems, 0, min(size, newItems.length));
    items = newItems;
  }

  @Override
  public boolean isEmpty() {
    return size == 0;
  }

  @Override
  public boolean contains(Object value) {
    for (int i = 0; i < size; i++) {
      if (items[i] == value) {
        return true;
      }
    }
    return false;
  }

  @Override
  public Iterator<T> iterator() {
    return new UnorderedListIterator();
  }

  @Override
  public Object[] toArray() {
    return Arrays.copyOf(items, size);
  }

  @Override
  public <R> R[] toArray(R[] a) {
    return (R[]) items;
  }

  public T get(int index) {
    if (index >= size) {
      throw new IndexOutOfBoundsException("index " + index + " must be > 0 and < " + size);
    }
    return items[index];
  }

  @Override
  public int size() {
    return size;
  }

  @Override
  public void clear() {
    size = 0;
  }

  private class UnorderedListIterator implements Iterator<T> {
    private int index;

    @Override
    public void remove() {
      index--;
      UnorderedCollection.this.swapRemove(index);
    }

    @Override
    public boolean hasNext() {
      return index < size;
    }

    @Override
    public T next() {
      if (index >= size) {
        throw new NoSuchElementException();
      }
      return items[index++];
    }
  }
}

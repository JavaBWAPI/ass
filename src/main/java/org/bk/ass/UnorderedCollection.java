package org.bk.ass;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import static java.lang.Math.max;
import static java.lang.Math.min;

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
    addAll(source);
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
    for (T e : elementsToAdd) {
      items[size++] = e;
    }
    return true;
  }

  @Override
  public boolean remove(Object value) {
    for (int i = 0; i < size; i++) {
      if (items[i] == value) {
        removeAt(i);
        return true;
      }
    }
    return false;
  }

  public T removeAt(int index) {
    if (index >= size) {
      throw new IndexOutOfBoundsException("index " + index + " must be >= 0 and < " + size);
    }
    T removed = items[index];
    T last = items[--size];
    items[index] = last;
    return removed;
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
    return items;
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

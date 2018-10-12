package org.bk.ass;

import static java.lang.Math.max;
import static java.lang.Math.min;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

public class UnorderedList<T> implements Collection<T> {

  T[] items;
  private int size;

  public UnorderedList() {
    this(16);
  }

  public UnorderedList(int capacity) {
    items = (T[]) new Object[capacity];
  }

  public void clearReferences() {
    Arrays.fill(items, size, items.length, null);
  }

  public boolean add(T value) {
    if (size == items.length) {
      resize((int) (size * 7L / 4));
    }
    items[size++] = value;
    return true;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return false;
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
    if (index < 0 || index >= size) {
      throw new IndexOutOfBoundsException("index " + index + " must be > 0 and < " + size);
    }
    T last = items[--size];
    T removed = items[index];
    items[index] = last;
    return removed;
  }

  private void resize(int newCapacity) {
    int capaToUse = max(8, newCapacity);
    T[] newItems = (T[]) new Object[capaToUse];
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
  public <T1> T1[] toArray(T1[] a) {
    return (T1[]) items;
  }

  public T get(int index) {
    if (index < 0 || index >= size) {
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

package org.bk.ass.grid;

public interface Grid<T> {

    int getWidth();

    int getHeight();

    T get(int x, int y);
}

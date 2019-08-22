package org.bk.ass.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Simplest form of a cluster, just contains the clustered elements and a user defined object. */
public class Cluster<U> {

  private Object userObject;
  List<U> elements = new ArrayList<>();

  public void setUserObject(Object userObject) {
    this.userObject = userObject;
  }

  public Object getUserObject() {
    return userObject;
  }

  public List<U> getElements() {
    return Collections.unmodifiableList(elements);
  }
}

/*
 * Copyright 2022 StarTree Inc
 *
 * Licensed under the StarTree Community License (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.startree.ai/legal/startree-community-license
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT * WARRANTIES OF ANY KIND,
 * either express or implied.
 * See the License for the specific language governing permissions and limitations under
 * the License.
 */
package ai.startree.thirdeye.plugins.postprocessor;

/**
 * Source:
 * https://algs4.cs.princeton.edu/93intersection/IntervalST.java.html
 * https://algs4.cs.princeton.edu/93intersection/
 *
 * Modified to respect ThirdEye start/end semantic.
 * */

/******************************************************************************
 *
 *  Interval search tree implemented using a randomized BST.
 *
 *  Duplicate policy:  if an interval is inserted that already
 *                     exists, the new value overwrite the old one
 *
 *
 ******************************************************************************/

// note cyril - duplicated from internal library

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class IntervalSearchTree<V> {

  private Node root;   // root of the BST

  // BST helper node data type
  private class Node {

    Interval1D interval;  // key
    Set<V> values; // associated data - there can be multiple objects on the same interval
    Node left;  // left subtrees
    Node right; // right subtrees
    int N;  // size of subtree rooted at this node
    long max;  // max endpoint in subtree rooted at this node

    Node(Interval1D interval, V value) {
      this.interval = interval;
      this.values = new HashSet<>();
      values.add(value);
      this.N = 1;
      this.max = interval.getMax();
    }
  }

  /***************************************************************************
   *  BST search
   ***************************************************************************/

  public boolean contains(Interval1D interval) {
    return (get(interval) != null);
  }

  // return value associated with the given key
  // if no such value, return null
  public Set<V> get(Interval1D interval) {
    return get(root, interval);
  }

  private Set<V> get(Node x, Interval1D interval) {
    if (x == null) {
      return null;
    }
    int cmp = interval.compareTo(x.interval);
    if (cmp < 0) {
      return get(x.left, interval);
    } else if (cmp > 0) {
      return get(x.right, interval);
    } else {
      return x.values;
    }
  }

  /***************************************************************************
   *  randomized insertion
   ***************************************************************************/
  public void put(Interval1D interval, V value) {
    Set<V> existingValues = get(interval);
    if (existingValues != null) {
      existingValues.add(value);
    }
    root = randomizedInsert(root, interval, value);
  }

  // make new node the root with uniform probability
  private Node randomizedInsert(Node x, Interval1D interval, V value) {
    if (x == null) {
      return new Node(interval, value);
    }
    if (Math.random() * size(x) < 1.0) {
      return rootInsert(x, interval, value);
    }
    int cmp = interval.compareTo(x.interval);
    if (cmp < 0) {
      x.left = randomizedInsert(x.left, interval, value);
    } else {
      x.right = randomizedInsert(x.right, interval, value);
    }
    fix(x);
    return x;
  }

  private Node rootInsert(Node x, Interval1D interval, V value) {
    if (x == null) {
      return new Node(interval, value);
    }
    int cmp = interval.compareTo(x.interval);
    if (cmp < 0) {
      x.left = rootInsert(x.left, interval, value);
      x = rotR(x);
    } else {
      x.right = rootInsert(x.right, interval, value);
      x = rotL(x);
    }
    return x;
  }

  /***************************************************************************
   *  deletion
   ***************************************************************************/
  private Node joinLR(Node a, Node b) {
    if (a == null) {
      return b;
    }
    if (b == null) {
      return a;
    }

    if (Math.random() * (size(a) + size(b)) < size(a)) {
      a.right = joinLR(a.right, b);
      fix(a);
      return a;
    } else {
      b.left = joinLR(a, b.left);
      fix(b);
      return b;
    }
  }

  // remove and return value associated with given interval;
  // if no such interval exists return null
  public Set<V> remove(Interval1D interval) {
    Set<V> values = get(interval);
    root = remove(root, interval);
    return values;
  }

  private Node remove(Node h, Interval1D interval) {
    if (h == null) {
      return null;
    }
    int cmp = interval.compareTo(h.interval);
    if (cmp < 0) {
      h.left = remove(h.left, interval);
    } else if (cmp > 0) {
      h.right = remove(h.right, interval);
    } else {
      h = joinLR(h.left, h.right);
    }
    fix(h);
    return h;
  }

  /***************************************************************************
   *  Interval searching
   ***************************************************************************/

  // return the first interval found in data structure that intersects the given interval;
  // also returns the values associated with the interval found
  // return null if no such interval exists
  // running time is proportional to log N
  public Map.Entry<Interval1D, Set<V>> search(Interval1D interval) {
    return search(root, interval);
  }

  // look in subtree rooted at x
  public Map.Entry<Interval1D, Set<V>> search(Node x, Interval1D interval) {
    while (x != null) {
      if (interval.intersects(x.interval)) {
        return Map.entry(x.interval, x.values);
      } else if (x.left == null) {
        x = x.right;
      } else if (x.left.max < interval.getMin()) {
        x = x.right;
      } else {
        x = x.left;
      }
    }
    return null;
  }

  // return *all* intervals in data structure that intersect the given interval
  // running time is proportional to R log N, where R is the number of intersections
  public Map<Interval1D, Set<V>> searchAll(Interval1D interval) {
    Map<Interval1D, Set<V>> matchingIntervals = new HashMap<>();
    searchAll(root, interval, matchingIntervals);
    return matchingIntervals;
  }

  // look in subtree rooted at x - returns intervals that intersects
  public boolean searchAll(Node x, Interval1D interval, Map<Interval1D, Set<V>> matchingIntervals) {
    boolean found1 = false;
    boolean found2 = false;
    boolean found3 = false;
    if (x == null) {
      return false;
    }
    if (interval.intersects(x.interval)) {
      matchingIntervals.put(x.interval, x.values);
      found1 = true;
    }
    if (x.left != null && x.left.max >= interval.getMin()) {
      found2 = searchAll(x.left, interval, matchingIntervals);
    }
    if (found2 || x.left == null || x.left.max < interval.getMin()) {
      found3 = searchAll(x.right, interval, matchingIntervals);
    }
    return found1 || found2 || found3;
  }

  /***************************************************************************
   *  useful binary tree functions
   ***************************************************************************/

  // return number of nodes in subtree rooted at x
  public int size() {
    return size(root);
  }

  private int size(Node x) {
    if (x == null) {
      return 0;
    } else {
      return x.N;
    }
  }

  // height of tree (empty tree height = 0)
  public int height() {
    return height(root);
  }

  private int height(Node x) {
    if (x == null) {
      return 0;
    }
    return 1 + Math.max(height(x.left), height(x.right));
  }

  /***************************************************************************
   *  helper BST functions
   ***************************************************************************/

  // fix auxilliar information (subtree count and max fields)
  private void fix(Node x) {
    if (x == null) {
      return;
    }
    x.N = 1 + size(x.left) + size(x.right);
    x.max = max3(x.interval.getMax(), max(x.left), max(x.right));
  }

  private long max(Node x) {
    if (x == null) {
      return Integer.MIN_VALUE;
    }
    return x.max;
  }

  // precondition: a is not null
  private long max3(long a, long b, long c) {
    return Math.max(a, Math.max(b, c));
  }

  // right rotate
  private Node rotR(Node h) {
    Node x = h.left;
    h.left = x.right;
    x.right = h;
    fix(h);
    fix(x);
    return x;
  }

  // left rotate
  private Node rotL(Node h) {
    Node x = h.right;
    h.right = x.left;
    x.left = h;
    fix(h);
    fix(x);
    return x;
  }

  /***************************************************************************
   *  Debugging functions that test the integrity of the tree
   ***************************************************************************/

  // check integrity of subtree count fields
  public boolean check() {
    return checkCount() && checkMax();
  }

  // check integrity of count fields
  private boolean checkCount() {
    return checkCount(root);
  }

  private boolean checkCount(Node x) {
    if (x == null) {
      return true;
    }
    return checkCount(x.left) && checkCount(x.right) && (x.N == 1 + size(x.left) + size(x.right));
  }

  private boolean checkMax() {
    return checkMax(root);
  }

  private boolean checkMax(Node x) {
    if (x == null) {
      return true;
    }
    return x.max == max3(x.interval.getMax(), max(x.left), max(x.right));
  }
}

package ai.startree.thirdeye.spi.util;

import java.util.Objects;

public class Pair<K, V> {

  private final K k;
  private final V v;

  public Pair(K k, V v) {
    this.k = k;
    this.v = v;
  }

  public static <K, V> Pair<K, V> createPair(K k, V v) {
    return new Pair<K, V>(k, v);
  }

  public K getFirst() {
    return k;
  }

  public V getSecond() {
    return v;
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equals(k, pair.k) &&
        Objects.equals(v, pair.v);
  }

  @Override
  public int hashCode() {
    return Objects.hash(k, v);
  }

  @Override
  public String toString() {
    return String.format("(%s, %s)", k.toString(), v.toString());
  }
}

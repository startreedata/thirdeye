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
package ai.startree.thirdeye.spi.datalayer;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class TemplatableMap<K, V> implements Map<K, V> {

  private Map<K, Templatable<V>> delegate = new HashMap<>();

  public Map<K, Templatable<V>> delegate() {
    return delegate;
  }

  public static <K, V> TemplatableMap<K, V> fromDelegate(final Map<? extends K, ? extends  Templatable<V>> delegate) {
    final TemplatableMap<K, V> map = new TemplatableMap<>();
    map.delegate = new HashMap<>(delegate);
    return map;
  }

  public static <K, V> TemplatableMap<K, V> copyOf(final Map<K, V> m) {
    final TemplatableMap<K, V> map = new TemplatableMap<>();
    map.putAll(m);
    return map;
  }

  public TemplatableMap() {

  }

  public TemplatableMap(Map<K, V> map) {
    putAll(map);
  }

  @Override
  public int size() {
    return delegate.size();
  }

  @Override
  public boolean isEmpty() {
    return delegate.isEmpty();
  }

  @Override
  public boolean containsKey(final Object key) {
    return delegate.containsKey(key);
  }

  @Override
  public boolean containsValue(final Object value) {
    return delegate.containsValue(value);
  }

  @Override
  public V get(final Object key) {
    final Templatable<V> templatable = delegate.get(key);
    if (templatable == null) {
      return null;
    }
    return templatable.value();
  }

  @Override
  public V put(final K key, final V value) {
    // fixme cyril here maybe write in correct union field?
    final Templatable<V> oldValue = delegate.put(key, new Templatable<V>().setValue(value));
    if (oldValue == null) {
      return null;
    }
    return oldValue.value();
  }

  @Override
  public V remove(final Object key) {
    final Templatable<V> removed = delegate.remove(key);
    if (removed == null) {
      return null;
    }
    return removed.value();
  }

  @Override
  public void putAll(final Map<? extends K, ? extends V> m) {
    // not optimized
    for (var e : m.entrySet()) {
      put(e.getKey(), e.getValue());
    }
  }

  @Override
  public void clear() {

  }

  @Override
  public Set<K> keySet() {
    return delegate.keySet();
  }

  @Override
  public Collection<V> values() {
    return delegate.values().stream().map(Templatable::value).collect(Collectors.toList());
  }

  @Override
  public Set<Entry<K, V>> entrySet() {
    Set<Entry<K, V>> set = new HashSet<>();
    for (Entry<K, Templatable<V>> e : delegate.entrySet()) {
      set.add(new AbstractMap.SimpleEntry<>(e.getKey(), e.getValue().value()));
    }
    return set;
  }
}

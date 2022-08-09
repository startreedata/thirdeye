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

import java.util.HashMap;
import java.util.Map;

/**
 * Makes it easier to manipulate a map of {@link Templatable}.
 * */
public class TemplatableMap<K, V> extends HashMap<K, Templatable<V>> {

  public TemplatableMap() {
    super();
  }

  public TemplatableMap(TemplatableMap<K, V> map) {
    super();
    putAll(map);
  }

  public static <K, V> TemplatableMap<K, V> ofValue(K key, V value) {
    final TemplatableMap<K, V> m = new TemplatableMap<>();
    m.putValue(key, value);
    return m;
  }

  public static <K, V> TemplatableMap<K, V> fromValueMap(Map<K, V> map) {
    final TemplatableMap<K, V> templatableMap = new TemplatableMap<>();
    for (K k: map.keySet()) {
      templatableMap.put(k, new Templatable<V>().setValue(map.get(k)));
    }
    return templatableMap;
  }

  /**
   * Returns a copy of the map with Templatables unwrapped to their value.
   * @throws IllegalStateException if the Map contains values for which properties were not applied. ie for which templatedValue is not null and value is null.
   * */
  public Map<K, V> valueMap() {
    final HashMap<K, V> valueMap = new HashMap<>();
    for (K k : this.keySet()) {
      final Templatable<V> templatable = this.get(k);
      checkPropertyIsApplied(templatable);
      valueMap.put(k, templatable.value());
    }
    return valueMap;
  }

  public V getValue(Object key) {
    Templatable<V> templatable = get(key);
    checkPropertyIsApplied(templatable);
    return templatable.value();
  }

  public V putValue(K key, V value) {
    final Templatable<V> old = put(key, new Templatable<V>().setValue(value));
    if (old == null) {
      return null;
    }
    return old.value();
  }

  private void checkPropertyIsApplied(final Templatable<V> templatable) {
    if (templatable.value() == null && templatable.templatedValue() != null) {
      throw new IllegalStateException("Cannot return value map. Properties not resolved");
    }
  }
}

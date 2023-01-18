/*
 * Copyright 2023 StarTree Inc
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
package ai.startree.thirdeye.spi.detection.dimension;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stores key-value pairs of dimension name and value. The paris are sorted by their dimension names
 * in ascending order.
 *
 * To reduces the length of string to be stored in database, this class implements SortedMap<String,
 * String> for
 * converting to/from Json string in Map format, i.e., instead of storing
 * {"sortedDimensionMap":{"country":"US",
 * "page_name":"front_page"}}, we only need to store {"country":"US","page_name":"front_page"}.
 */
@Deprecated
public class DimensionMap implements SortedMap<String, String>, Comparable<DimensionMap>,
    Serializable {

  private static final Logger LOG = LoggerFactory.getLogger(DimensionMap.class);
  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  // Dimension name to dimension value pairs, which are sorted by dimension names
  private SortedMap<String, String> sortedDimensionMap = new TreeMap<>();

  /**
   * Constructs an empty dimension map.
   */
  public DimensionMap() {
  }

  /**
   * Constructs a dimension map from a json string; if the given string is not in Json format, then
   * this method falls
   * back to parse Java's map string format, which is {key1=value1,key2=value2}.
   *
   * @param value the json string that represents this dimension map.
   */
  public DimensionMap(String value) {
    Preconditions.checkNotNull(value); // We do not allow null pointers flying around the code.
    if (!Strings.isNullOrEmpty(value)) { // Empty string produces empty dimension map.
      try {
        sortedDimensionMap = OBJECT_MAPPER.readValue(value, TreeMap.class);
      } catch (IOException e) {
        try { // Fall back to Java's map string, which is {key=value} (including curly brackets).
          value = value.substring(1, value.length() - 1); // Remove curly brackets
          String[] keyValuePairs = value.split(",");
          for (String pair : keyValuePairs) {
            String[] entry = pair.split("=");
            sortedDimensionMap.put(entry[0].trim(), entry[1].trim());
          }
        } catch (Exception finalE) {
          throw new IllegalArgumentException(String
              .format("Failed to initialize dimension map from this string: \"{%s}\": %s", value,
                  ExceptionUtils.getStackTrace(finalE)));
        }
      }
    }
  }

  // Check if this dimension map contains filters configured in that
  public boolean contains(Map<String, String> that) {
    if (that == null || that.size() == 0) {
      return this.size() == 0;
    }

    for (Entry<String, String> filter : that.entrySet()) {
      if (this.get(filter.getKey()) == null || !filter.getValue()
          .equalsIgnoreCase(this.get(filter.getKey()))) {
        return false;
      }
    }

    return true;
  }

  /**
   * Returns Json string representation for Map class, which is in the form of
   * {"key1":"value1","key2"="value2"}.
   *
   * @return Json string representation for Map class.
   */
  public String toJson() throws JsonProcessingException {
    return OBJECT_MAPPER.writeValueAsString(this);
  }

  /**
   * Returns a JSON string representation of this dimension map for {@link
   * ai.startree.thirdeye.datalayer.dao.GenericPojoDao}
   * to persistent the map to backend database.
   *
   * It returns the generic string representation of this dimension map if any exception occurs when
   * generating the JSON
   * string. In that case, the constructor {@link DimensionMap(String)} will be invoked during the
   * construction of that
   * dimension map.
   *
   * @return a JSON string representation of this dimension map for {@link
   *     ai.startree.thirdeye.datalayer.dao.GenericPojoDao}
   *     to persistent the map to backend database.
   */
  @Override
  public String toString() {
    try {
      return this.toJson();
    } catch (JsonProcessingException e) {
      return super.toString();
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DimensionMap that = (DimensionMap) o;
    return Objects.equals(sortedDimensionMap, that.sortedDimensionMap);
  }

  @Override
  public int hashCode() {
    return Objects.hash(sortedDimensionMap);
  }

  /**
   * Returns the compared result of the string representation of dimension maps.
   *
   * Examples:
   * 1. a={}, b={"K"="V"} --> a="", b="KV" --> a < b
   * 2. a={"K"="V"}, b={"K"="V"} --> a="KV", b="KV" --> a = b
   * 3. a={"K2"="V1"}, b={"K1"="V1","K2"="V2"} --> a="K2V1", b="K1V1K2V2" --> a > b
   *
   * @param o the dimension to compare to
   */
  @Override
  public int compareTo(DimensionMap o) {
    Iterator<Map.Entry<String, String>> thisIte = sortedDimensionMap.entrySet().iterator();
    Iterator<Map.Entry<String, String>> thatIte = o.sortedDimensionMap.entrySet().iterator();
    while (thisIte.hasNext()) {
      // o is a smaller map
      if (!thatIte.hasNext()) {
        return 1;
      }

      Map.Entry<String, String> thisEntry = thisIte.next();
      Map.Entry<String, String> thatEntry = thatIte.next();
      // Compare dimension name first
      int diff = ObjectUtils.compare(thisEntry.getKey(), thatEntry.getKey());
      if (diff != 0) {
        return diff;
      }
      // Compare dimension value afterwards
      diff = ObjectUtils.compare(thisEntry.getValue(), thatEntry.getValue());
      if (diff != 0) {
        return diff;
      }
    }

    // o is a larger map
    if (thatIte.hasNext()) {
      return -1;
    }

    return 0;
  }

  @Override
  public Comparator<? super String> comparator() {
    return sortedDimensionMap.comparator();
  }

  @Override
  public SortedMap<String, String> subMap(String fromKey, String toKey) {
    return sortedDimensionMap.subMap(fromKey, toKey);
  }

  @Override
  public SortedMap<String, String> headMap(String toKey) {
    return sortedDimensionMap.headMap(toKey);
  }

  @Override
  public SortedMap<String, String> tailMap(String fromKey) {
    return sortedDimensionMap.tailMap(fromKey);
  }

  @Override
  public String firstKey() {
    return sortedDimensionMap.firstKey();
  }

  @Override
  public String lastKey() {
    return sortedDimensionMap.lastKey();
  }

  @Override
  public int size() {
    return sortedDimensionMap.size();
  }

  @Override
  public boolean isEmpty() {
    return sortedDimensionMap.isEmpty();
  }

  @Override
  public boolean containsKey(Object key) {
    return sortedDimensionMap.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return sortedDimensionMap.containsValue(value);
  }

  @Override
  public String get(Object key) {
    return sortedDimensionMap.get(key);
  }

  @Override
  public String put(String key, String value) {
    return sortedDimensionMap.put(key, value);
  }

  @Override
  public String remove(Object key) {
    return sortedDimensionMap.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends String> m) {
    sortedDimensionMap.putAll(m);
  }

  @Override
  public void clear() {
    sortedDimensionMap.clear();
  }

  @Override
  public Set<String> keySet() {
    return sortedDimensionMap.keySet();
  }

  @Override
  public Collection<String> values() {
    return sortedDimensionMap.values();
  }

  @Override
  public Set<Entry<String, String>> entrySet() {
    return sortedDimensionMap.entrySet();
  }
}

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
package ai.startree.thirdeye.anomaly.grouping;

import java.util.Objects;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * Group key is wrapper class that is used to retrieve the specified group of anomalies from
 * AlertGrouper.
 *
 * @param <T> The class type of the actual entity to identify different groups. For instance,
 *     the actual key could be a
 *     DimensionMap.
 */
public class AlertGroupKey<T> {

  // The default empty group key that is used to represent the rolled up group, which collects the anomalies from
  // groups that contains only one anomaly.
  public static final AlertGroupKey EMPTY_KEY = new AlertGroupKey<Objects>();

  // The actual object to distinguish group key from each other.
  private final T key;

  /**
   * Constructs an empty group key, i.e., the key is null.
   */
  public AlertGroupKey() {
    this.key = null;
  }

  /**
   * Constructs a group key with the given key.
   *
   * @param key the actual object that is used to distinguish group key from each other.
   */
  public AlertGroupKey(T key) {
    this.key = key;
  }

  /**
   * Returns the actual object that is used to distinguish group key from each other.
   *
   * @return the actual object that is used to distinguish group key from each other.
   */
  T getKey() {
    return key;
  }

  /**
   * Returns raw key's toString method if it is not null.
   *
   * @return an empty string if the raw key is null.
   */
  public String toGroupName() {
    if (key != null) {
      return key.toString();
    } else {
      return "";
    }
  }

  /**
   * Returns an empty group key, whose raw key is null.
   *
   * @return an empty group key.
   */
  @SuppressWarnings("unchecked")
  public static final <T> AlertGroupKey<T> emptyKey() {
    return (AlertGroupKey<T>) EMPTY_KEY;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AlertGroupKey<?> alertGroupKey = (AlertGroupKey<?>) o;
    return Objects.equals(getKey(), alertGroupKey.getKey());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getKey());
  }

  @Override
  public String toString() {
    return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
  }
}
